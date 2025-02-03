package org.migrationDB.Repository;

import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Exception.MigrationSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VersionRepository {
    private final static String VERSION_TABLE = "version_control";
    private static final Logger log = LoggerFactory.getLogger(VersionRepository.class);


    public List<MigrationSchema> getCurrentVersion(Connection conn) {
        if (!tableExists(conn)) {
            createVersionTable(conn);
            return new ArrayList<>();
        }

        final String query = "SELECT * FROM " + VERSION_TABLE;
        List<MigrationSchema> migrations = new ArrayList<>();

        try (Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                migrations.add(mapResultToObject(rs));
            }
            return migrations;
        } catch (SQLException e) {
            throw new MigrationSqlException("Error during reading meta data.", e);
        }
    }

    private MigrationSchema mapResultToObject(ResultSet rs) {
        try {
            return new MigrationSchema(
                    rs.getInt("id"),
                    rs.getInt("version"),
                    rs.getString("script_name"),
                    rs.getString("checksum"),
                    rs.getString("created_at"),
                    rs.getBoolean("success")
            );
        } catch (SQLException e) {
            throw new MigrationSqlException("Error while mapping result.", e);
        }
    }

    private boolean tableExists(Connection conn) {
        DatabaseMetaData meta = null;
        try {
            meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, conn.getSchema(), VERSION_TABLE, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new MigrationSqlException("Error during reading meta data.", e);
        }
    }

    private void createVersionTable(Connection conn) {
        try {
            log.info("Creating version control table.");

            String database = conn.getMetaData().getDatabaseProductName();
            String dbVersion = "AUTO_INCREMENT";
            switch (database) {
                case "PostgreSQL":
                    dbVersion = "GENERATED ALWAYS AS IDENTITY";
                    conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS public");
                    break;
                case "SQL Server":
                    dbVersion = "IDENTITY";
                    break;
                case "Oracle":
                    dbVersion = "GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1)";
                    break;
                default:
                    break;
            }

            conn.createStatement().executeUpdate(
                    "CREATE TABLE " + VERSION_TABLE + " (" +
                            "id INT " + dbVersion + " PRIMARY KEY, " +
                            "version INT NOT NULL, " +
                            "script_name VARCHAR(255) NOT NULL, " +
                            "checksum VARCHAR(255) NOT NULL, " +
                            "success BOOLEAN DEFAULT FALSE, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        } catch (SQLException e) {
            throw new MigrationSqlException("Error creating version table.", e);
        }
    }
}
