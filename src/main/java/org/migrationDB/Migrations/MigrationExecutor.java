package org.migrationDB.Migrations;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.migrationDB.DatabaseService.DatabaseConnection;
import org.migrationDB.DatabaseService.ExecuteQuery;
import org.migrationDB.Service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MigrationExecutor {
    private static final Logger log = LoggerFactory.getLogger(VersionSchema.class);

    public static void makeMigration(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            List<MigrationSchema> migrationSchemas = VersionSchema.getCurrentVersion(conn);
            if (!migrationSchemas.isEmpty()) {
                MigrationsCompatibility.checkCompatibility(dbConnection, migrationSchemas);
            }

            List<String> sortedFilesName = FileService.getFilesFromDirectory(dbConnection.getPathToMigrationFiles(), migrationSchemas);

            assert sortedFilesName != null;
            if (!sortedFilesName.isEmpty()) {
                readScriptsAndExecute(conn, dbConnection.getPathToMigrationFiles(), sortedFilesName);

            } else {
                log.info("No migrations to execute.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong during connecting. ", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readScriptsAndExecute(Connection conn, String pathToDirectory, List<String> sortedFilesName) {
        for (String fileName : sortedFilesName) {
            String fileScripts = FileService.readScriptsFromFile(fileName, pathToDirectory);
            try {
                conn.setAutoCommit(false);

                try (Reader reader = new StringReader(fileScripts)) {
                    ScriptRunner scriptRunner = new ScriptRunner(conn);
                    scriptRunner.setStopOnError(true);
                    scriptRunner.runScript(reader);
                }

                ExecuteQuery.recordMigrationFile(conn, fileName, fileScripts);
                conn.commit();

            } catch (SQLException | NoSuchAlgorithmException | IOException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Something went wrong during rollback", ex);
                }
                throw new RuntimeException("Migration failed: " + e.getMessage());
            }
        }
    }
}
