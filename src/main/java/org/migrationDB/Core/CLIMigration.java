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
        for (String arg : args) {
            if (arg.startsWith("--action=")) {
                action = arg.substring(9);
            } else if (arg.startsWith("--version=")) {
                version = arg.substring(10);
            }
        }

        validArgs(action, version);
    }

    private void validArgs(String action, String version) {
        if (action == null) {
            printUsage();
            return;
        }

        if (action.equals("migrate")) {
            MigrationApp migration = new MigrationApp();
            migration.runMigrations();
        } else if (action.equals("undo")) {
            if (version == null) {
                System.out.println("Version is required for undo action.");
                printUsage();
                return;
            }
            MigrationApp migration = new MigrationApp();
            migration.undoMigration(version);
        } else {
            System.out.println("Invalid action. Use 'migrate' or 'undo'.");
            printUsage();
        }
    }

    private void printUsage() {
        System.out.println("Usage: java -jar migration-tool.jar --path=<path_to_migrations> --action=<migrate|undo> [--version=<version>]");
        System.out.println("  --action   Action to perform: 'migrate' or 'undo'.");
        System.out.println("  --version  Version to undo (required for 'undo' action).");
    }
}
