package org.migrationDB.Core;

import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Exception.DatabaseConnectionException;
import org.migrationDB.Exception.MigrationSqlException;
import org.migrationDB.Service.FileService;
import org.migrationDB.Service.MigrationService;
import org.migrationDB.Service.MigrationsCompatibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class MigrationHandler {
    private final MigrationService migrationService;
    private final FileService fileService;
    private final static Logger log = LoggerFactory.getLogger(MigrationHandler.class);

    public MigrationHandler(MigrationService migrationService, FileService fileService) {
        this.migrationService = migrationService;
        this.fileService = fileService;
    }

    public void migrate(DatabaseConnection dbConnection, List<MigrationSchema> migrationSchemas, Connection conn) {
        int maxVersion = migrationSchemas.stream()
                .filter(migrationSchema -> migrationSchema.type().equals("V"))
                .mapToInt(MigrationSchema::version)
                .max()
                .orElse(0);

        List<String> sortedFilesName = fileService.getFilesFromDirectory(dbConnection.getPathToMigrationFiles(), migrationSchemas);
        assert sortedFilesName != null;

        List<String> versionedFiles = sortedFilesName.stream()
                .filter(name -> name.startsWith("V"))
                .toList();

        MigrationsCompatibilityService.checkMaxVersion(maxVersion, versionedFiles);

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
    }

    private void readScriptsAndExecute(Connection conn, String pathToDirectory, List<String> sortedFilesName) {
        for (String fileName : sortedFilesName) {
            String fileScripts = null;
            try {
                conn.setAutoCommit(false);
                fileScripts = fileService.readScriptsFromFile(fileName, pathToDirectory);
                migrationService.executeQueries(conn, fileScripts);

                if (fileName.startsWith("R") && migrationService.checkIsInDatabase(conn, fileName)) {
                    log.info("Updating repeatable migration...");
                    migrationService.updateRepeatable(conn, fileName, MigrationsCompatibilityService.calculateCheckSum(fileScripts));
                } else {
                    log.info("Recording migration...");
                    migrationService.recordMigrationFile(conn, fileName, fileScripts);
                }

                conn.commit();
                log.info("Migration completed successfully: {}", fileName);

            } catch (SQLException e) {
                try {
                    if (fileScripts != null) {
                        dropCreatedTables(conn, fileScripts);
                    }
                    conn.rollback();
                    log.error("Migration failed: {}. Rolling back changes.", fileName);
                } catch (SQLException ex) {
                    throw new MigrationSqlException("Error during rollback.", ex);
                }
                throw new DatabaseConnectionException("Error during migration execution.", e);
            }
        }
    }

    private void dropCreatedTables(Connection conn, String fileScripts) {
        List<String> tablesName = extractTablesName(fileScripts);
        if (tablesName.isEmpty()) {
            return;
        }
        tablesName.sort((s1, s2) -> s2.length() - s1.length());
        for (String tableName : tablesName) {
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS " + tableName);
            } catch (SQLException e) {
                throw new MigrationSqlException("Error during dropping tables.", e);
            }
        }
    }

    private List<String> extractTablesName(String fileScripts) {
        List<String> names = new LinkedList<>();
        String[] lines = fileScripts.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("CREATE TABLE")) {
                String tableName = line.split("\\s+")[2];
                names.add(tableName);
            }
        }
        return names;
    }
}
