package org.migrationDB.Core;

import org.migrationDB.MigrationApp;

public class CLIMigration {
    public void validateCliActions(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String action = null;
        String version = null;
        String path = null;

        for (String arg : args) {
            if (arg.startsWith("--action=")) {
                action = arg.substring(9);
            } else if (arg.startsWith("--version=")) {
                version = arg.substring(10);
            } else if (arg.startsWith("--path=")) {
                path = arg.substring(7);
            }
        }

        validArgs(action, version, path);
    }

    private void validArgs(String action, String version, String path) {
        if ((action == null) || (path == null)) {
            printUsage();
            return;
        }

        if (action.equals("migrate")) {
            MigrationApp migration = new MigrationApp(path);
            migration.runMigrations();
            migration.showHistory();
        } else if (action.equals("undo")) {
            MigrationApp migration = new MigrationApp(path);
            if (version == null) {
                migration.undoMigration();
            } else {
                migration.undoMigration(version);
            }
            migration.showHistory();
        } else {
            System.out.println("Invalid action. Use 'migrate' or 'undo'.");
            printUsage();
        }
    }

    private void printUsage() {
        System.out.println("Usage: java -jar migration-tool.jar --path=<path_to_migrations> --action=<migrate|undo> [--version=<version>]");
        System.out.println("Options:");
        System.out.println("  --path     Path to migrations folder (required).");
        System.out.println("  --action   Action to perform: 'migrate' or 'undo'.");
        System.out.println("  --version  Version to undo (required for 'undo' action).");
    }
}
