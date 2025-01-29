package org.migrationDB.Migrations;

import org.migrationDB.DatabseService.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class MigrationExecutor {

    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);

    public static void makeMigration(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            int currentVersion = VersionSchema.getCurrentVersion(conn);
            log.info(String.valueOf(currentVersion));
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong during connecting. ", e);
        }
    }
}
