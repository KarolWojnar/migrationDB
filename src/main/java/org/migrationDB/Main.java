package org.migrationDB;

public class Main {

    public static void main(String[] args) {
        MigrationApp migrationApp = new MigrationApp();
        // history migrations
        migrationApp.showHistory();
        // run migrations
        migrationApp.runMigrations();
        migrationApp.showHistory();
        // undo migrations
        migrationApp.undoMigration("4"); // optional version as string
        migrationApp.showHistory();
    }
}