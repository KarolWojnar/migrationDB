package org.migrationDB.Migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VersionSchema {
    private final static String VERSION_TABLE = "version_control";
    private static final Logger log = LoggerFactory.getLogger(VersionSchema.class);


    public static List<MigrationSchema> getCurrentVersion(Connection conn) throws SQLException {
        if (!tableExists(conn)) {
            log.info("Creating version control table.");
            createVersionTable(conn);
            return new ArrayList<>();
        }

        final String query = "SELECT * FROM " + VERSION_TABLE + " WHERE script_name LIKE 'V%'";
        List<MigrationSchema> migrations = new ArrayList<>();

        try (Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                migrations.add(mapResultToObject(rs));
            }
            return migrations;
        }
    }

    private static MigrationSchema mapResultToObject(ResultSet rs) throws SQLException {
        return new MigrationSchema(
                rs.getInt("id"),
                rs.getInt("version"),
                rs.getString("script_name"),
                rs.getString("checksum"),
                rs.getString("created_at"),
                rs.getBoolean("success")
        );
    }

    private static boolean tableExists(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, VERSION_TABLE, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void createVersionTable(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE " + VERSION_TABLE + " (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, version INT NOT NULL, script_name VARCHAR(255)," +
                            " checksum VARCHAR(255) NOT NULL, success BOOLEAN, created_at TIMESTAMP)"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error creating version table. ", e);
        }
    }
}
