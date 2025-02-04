package org.migrationDB;

import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Core.MigrationExecutor;
import org.migrationDB.Core.MigrationHandler;
import org.migrationDB.Core.UndoHandler;
import org.migrationDB.Repository.VersionRepository;
import org.migrationDB.Service.FileService;
import org.migrationDB.Service.MigrationService;

public class MigrationApp {

    private final MigrationExecutor migrationExecutor;
    private final DatabaseConnection dbConnection;

    public MigrationApp(String path) {
        this.dbConnection = new DatabaseConnection(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/test",
                "root",
                "",
                path
        );
        VersionRepository versionRepository = new VersionRepository();
        MigrationService migrationService = new MigrationService();
        FileService fileService = new FileService();

        MigrationHandler migrationHandler = new MigrationHandler(migrationService, fileService);
        UndoHandler undoHandler = new UndoHandler(migrationService, fileService);

        this.migrationExecutor = new MigrationExecutor(versionRepository, migrationHandler, undoHandler);
    }

    public void runMigrations() {
        migrationExecutor.makeMigration(dbConnection);
    }

    public void showHistory() {
        migrationExecutor.showHistory(dbConnection);
    }

    public void undoMigration(String version) {
        migrationExecutor.undoMigration(dbConnection, version);
    }
    public void undoMigration() {
        migrationExecutor.undoMigration(dbConnection, "0");
    }
}
