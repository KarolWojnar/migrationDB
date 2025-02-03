package org.migrationDB.Core;

import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Exception.MigrationException;
import org.migrationDB.Exception.MigrationSqlException;
import org.migrationDB.Service.FileService;
import org.migrationDB.Service.MigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UndoHandler {

    private final MigrationService migrationService;
    private final FileService fileService;
    private final static Logger log = LoggerFactory.getLogger(UndoHandler.class);

    public UndoHandler(MigrationService migrationService, FileService fileService) {
        this.migrationService = migrationService;
        this.fileService = fileService;
    }

    public void undo(DatabaseConnection dbConnection, Connection conn, String version) {
        List<MigrationSchema> versionMigrationName = migrationService.getVersionMigrationName(conn, version);
        if (versionMigrationName.isEmpty()) {
            throw new MigrationException("Nothing to undo.");
        }
        for (MigrationSchema migrationObj : versionMigrationName) {
            String undoVersion = "U" + migrationObj.version() + "__";
            String undoName = fileService.getUndoFileNameByVersion(dbConnection.getPathToMigrationFiles(), undoVersion);
            String undoScripts = fileService.readScriptsFromFile(undoName, dbConnection.getPathToMigrationFiles());
            log.info("Undoing migration: {}", migrationObj.version());
            execUndoMigration(conn, undoScripts, String.valueOf(migrationObj.version()), migrationObj.script_name());
        }
    }

    private void execUndoMigration(Connection conn, String undoScripts, String version, String versionMigrationName) {
        try {
            conn.setAutoCommit(false);

            migrationService.executeQueries(conn, undoScripts);
            log.info("Undo completed successfully: {}", versionMigrationName);
            migrationService.deleteMigration(conn, version);

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
                throw new MigrationSqlException("Error during executing undo migration.", e);
            } catch (SQLException ex) {
                throw new MigrationSqlException("Error during rollback.", ex);
            }
        }

    }
}
