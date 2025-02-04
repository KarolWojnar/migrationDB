package org.migrationDB;

import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Core.MigrationExecutor;

public class MigrationLibrary {
    private final MigrationExecutor migrationExecutor;
    private final DatabaseConnection dbConnection;


    public MigrationLibrary(MigrationExecutor migrationExecutor, DatabaseConnection dbConnection) {
        this.migrationExecutor = migrationExecutor;
        this.dbConnection = dbConnection;
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
