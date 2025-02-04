package org.migrationDB.Core;

import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Exception.DatabaseConnectionException;
import org.migrationDB.Repository.VersionRepository;
import org.migrationDB.Exception.MigrationSqlException;
import org.migrationDB.Service.MigrationsCompatibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MigrationExecutor {
    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);
    private final VersionRepository versionRepository;
    private final MigrationHandler migrationHandler;
    private final UndoHandler undoHandler;

    public MigrationExecutor(VersionRepository versionRepository, MigrationHandler migrationHandler, UndoHandler undoHandler) {
        this.migrationHandler = migrationHandler;
        this.undoHandler = undoHandler;
        this.versionRepository = versionRepository;
    }

    public void makeMigration(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            List<MigrationSchema> migrationSchemas = versionRepository.getCurrentVersion(conn);
            if (!migrationSchemas.isEmpty()) {
               MigrationsCompatibilityService.checkCompatibility(dbConnection, migrationSchemas);
            }
            migrationHandler.migrate(dbConnection, migrationSchemas, conn);
        } catch (SQLException e) {
            throw new MigrationSqlException("Something went wrong during connecting. ", e);
        }
    }


    public void undoMigration(DatabaseConnection dbConnection, String version) {
        try (Connection conn = dbConnection.connect()) {
            if (!versionRepository.tableExists(conn)) {
                throw new DatabaseConnectionException("Nothing to undo.");
            }

            undoHandler.undo(dbConnection, conn, version);

        } catch (SQLException e) {
            throw new DatabaseConnectionException("Error during undo migration.", e);
        }

    }


    public void showHistory(DatabaseConnection dbConnection) {
        try (Connection conn = dbConnection.connect()) {
            if (!versionRepository.tableExists(conn)) {
                log.info("Nothing to show.");
                return;
            }
            List<MigrationSchema> migrationSchemas = versionRepository.getCurrentVersion(conn);
            System.out.println("History of migrations\n------------------------------------------------------------");
            migrationSchemas.forEach(
                    migrationSchema -> System.out.println(migrationSchema.created_at() + ", \t" + migrationSchema.script_name())
            );
            System.out.println("------------------------------------------------------------");
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Error during showing history.", e);
        }
    }
}
