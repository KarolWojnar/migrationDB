package org.migrationDB.Migrations;

import org.migrationDB.DatabaseService.DatabaseConnection;
import org.migrationDB.DatabaseService.ExecuteQuery;
import org.migrationDB.Exception.MigrationException;
import org.migrationDB.Exception.MigrationFileException;
import org.migrationDB.Exception.MigrationSqlException;
import org.migrationDB.Service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MigrationExecutor {
    private static final Logger log = LoggerFactory.getLogger(VersionSchema.class);

    public void makeMigration(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            List<MigrationSchema> migrationSchemas = VersionSchema.getCurrentVersion(conn);
            if (!migrationSchemas.isEmpty()) {
               MigrationsCompatibility.checkCompatibility(dbConnection, migrationSchemas);
            }

            List<String> sortedFilesName = FileService.getFilesFromDirectory(dbConnection.getPathToMigrationFiles(), migrationSchemas);
            assert sortedFilesName != null;

            List<String> versionedFiles = sortedFilesName.stream()
                    .filter(name -> name.startsWith("V"))
                    .toList();
            List<String> repeatableFiles = sortedFilesName.stream()
                    .filter(name -> name.startsWith("R"))
                    .toList();

            if (!versionedFiles.isEmpty()) {
                readScriptsAndExecute(conn, dbConnection.getPathToMigrationFiles(), versionedFiles);
            } else {
                log.info("No migrations to execute.");
            }

            if (!repeatableFiles.isEmpty()) {
                readScriptsAndExecute(conn, dbConnection.getPathToMigrationFiles(), repeatableFiles);
            }

        } catch (SQLException e) {
            throw new MigrationSqlException("Something went wrong during connecting. ", e);
        }
    }

    private void readScriptsAndExecute(Connection conn, String pathToDirectory, List<String> sortedFilesName) {
        for (String fileName : sortedFilesName) {
            String fileScripts = null;
            try {
                fileScripts = FileService.readScriptsFromFile(fileName, pathToDirectory);
            } catch (IOException e) {
                throw new MigrationFileException("Can't read file: " + fileName, e);
            }
            try {
                conn.setAutoCommit(false);

                executeQueries(conn, fileScripts);

                if (fileName.startsWith("R") && ExecuteQuery.checkIsInDatabase(conn, fileName)) {
                    log.info("Updating repeatable migration...");
                    ExecuteQuery.updateRepeatable(conn, fileName, CheckSumCalculator.calculateCheckSum(fileScripts));
                } else {
                    log.info("Recording migration...");
                    ExecuteQuery.recordMigrationFile(conn, fileName, fileScripts);
                }

                log.info("Migration completed successfully: {}", fileName);
                conn.commit();

            }
            catch (SQLException e) {
                try {
                    conn.rollback();
                    log.error("Migration failed: {}", fileName, e);
                } catch (SQLException ex) {
                    throw new MigrationSqlException("Something went wrong during rollback", ex);
                }
                throw new MigrationException("Something went wrong during migration", e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    log.error("Something went wrong during setAutoCommit(true)", e);
                }
            }
        }
    }

    private void executeQueries(Connection conn, String fileScripts) {
        String[] queries = fileScripts.split(";");
        for (String query : queries) {
            query = query.trim();
            if (!query.isEmpty()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(query);
                } catch (SQLException e) {
                    throw new MigrationSqlException("Error executing query: " + query, e);
                }
            }
        }
    }
}
