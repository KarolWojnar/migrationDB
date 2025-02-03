package org.migrationDB;

public class Main {
    public static void main(String[] args) {
        MigrationApp app = new MigrationApp();
        // make all migrations
        app.runMigrations();
        // undo migrations to v4
        app.undoMigration("4");
    }

}