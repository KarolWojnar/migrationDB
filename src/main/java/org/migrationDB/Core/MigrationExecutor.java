package org.migrationDB.Core;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Exception.DatabaseConnectionException;
import org.migrationDB.Repository.VersionRepository;
import org.migrationDB.Service.MigrationHistoryService;
import org.migrationDB.Exception.MigrationFileException;
import org.migrationDB.Exception.MigrationSqlException;
import org.migrationDB.Service.FileService;
import org.migrationDB.Service.MigrationsCompatibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class MigrationExecutor {
    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);
    private final MigrationHistoryService migrationHistoryService;
    private final VersionRepository versionRepository;
    private final FileService fileService;

    public MigrationExecutor(MigrationHistoryService migrationHistoryService, VersionRepository versionRepository, FileService fileService) {
        this.migrationHistoryService = migrationHistoryService;
        this.versionRepository = versionRepository;
        this.fileService = fileService;
    }

    public void makeMigration(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            List<MigrationSchema> migrationSchemas = versionRepository.getCurrentVersion(conn);
            if (!migrationSchemas.isEmpty()) {
               MigrationsCompatibilityService.checkCompatibility(dbConnection, migrationSchemas);
            }

            List<String> sortedFilesName = fileService.getFilesFromDirectory(dbConnection.getPathToMigrationFiles(), migrationSchemas);
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
            dbConnection.shutdown();
            throw new MigrationSqlException("Something went wrong during connecting. ", e);
        }
    }

    private void readScriptsAndExecute(Connection conn, String pathToDirectory, List<String> sortedFilesName) {
        for (String fileName : sortedFilesName) {
            String fileScripts = null;
            try {
                conn.setAutoCommit(false);
                fileScripts = fileService.readScriptsFromFile(fileName, pathToDirectory);
                executeQueries(conn, fileScripts);

                if (fileName.startsWith("R") && migrationHistoryService.checkIsInDatabase(conn, fileName)) {
                    log.info("Updating repeatable migration...");
                    migrationHistoryService.updateRepeatable(conn, fileName, MigrationsCompatibilityService.calculateCheckSum(fileScripts));
                } else {
                    log.info("Recording migration...");
                    migrationHistoryService.recordMigrationFile(conn, fileName, fileScripts);
                }

                conn.commit();
                log.info("Migration completed successfully: {}", fileName);

            } catch (IOException e) {
                throw new MigrationFileException("Can't read file: " + fileName, e);
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

    private void executeQueries(Connection conn, String fileScripts) throws SQLException {
        List<String> queries = splitQueries(fileScripts);
        for (String query : queries) {
            query = query.trim();
            if (!query.isEmpty()) {
                try (Statement st = conn.createStatement()) {
                    st.execute(query);
                }
            }
        }
    }

    private List<String> splitQueries(String fileScripts) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(fileScripts);
            return statements.stream().map(Object::toString).toList();
        } catch (JSQLParserException e) {
            throw new MigrationSqlException("Error during parsing SQL queries.", e);
        }
    }
}
