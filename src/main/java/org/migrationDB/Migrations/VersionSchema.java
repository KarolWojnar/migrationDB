package org.migrationDB.Migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionSchema {
    private final static String VERSION_TABLE = "version_control";
    private static final Logger log = LoggerFactory.getLogger(VersionSchema.class);


    public static int getCurrentVersion(Connection conn) throws SQLException {
        if (!tableExists(conn)) {
            log.info("Creating version control table.");
            return createVersionTable(conn);
        }
        return findLatestVersion(conn);
    }

    private static int findLatestVersion(Connection conn) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT MAX(id) FROM " + VERSION_TABLE);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding latest version. ", e);
        }
    }

    private static boolean tableExists(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, VERSION_TABLE, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static int createVersionTable(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE " + VERSION_TABLE + " (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, version INT NOT NULL, script_name VARCHAR(255)," +
                            " checksum VARCHAR(255) NOT NULL, success BOOLEAN, create_at TIMESTAMP)"
            );
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating version table. ", e);
        }
    }
}
