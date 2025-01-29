package org.migrationDB.Migrations;

import org.migrationDB.DatabseService.DatabaseConnection;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

public class MigrationExecutor {

    public static void makeMigration(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            int highestId = VersionSchema.getCurrentVersion(conn);
            if (highestId != 0) {
                MigrationsCompatibility.checkCompatibility(conn, dbConnection, highestId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong during connecting. ", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
