package org.migrationDB.Core;

import org.migrationDB.Data.InitModel;
import org.migrationDB.MigrationApp;

public class CLIMigration {
    public void validateCliActions(String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }

        String action = null;
        String version = null;
        String path = null;
        String driver = null;
        String url = null;
        String user = null;
        String password = "";

        for (String arg : args) {
            if (arg.startsWith("--action=")) {
                action = arg.substring(9);
            } else if (arg.startsWith("--version=")) {
                version = arg.substring(10);
            } else if (arg.startsWith("--path=")) {
                path = arg.substring(7);
            } else if (arg.startsWith("--driver=")) {
                driver = arg.substring(9);
            } else if (arg.startsWith("--url=")) {
                url = arg.substring(6);
            } else if (arg.startsWith("--user=")) {
                user = arg.substring(7);
            } else if (arg.startsWith("--password=")) {
                password = arg.substring(11);
            }
        }

        validArgsAndExecute(action, version, path, driver, url, user, password);
    }

    private void validArgsAndExecute(String action, String version, String path, String driver, String url, String user, String password) {
        if ((action == null) || (path == null) || (url == null) || (driver == null)) {
            printUsage();
            return;
        }

        InitModel initModel = new InitModel(driver, url, user, password, path);

        if (action.equals("migrate")) {
            MigrationApp migration = new MigrationApp(initModel);
            migration.runMigrations();
            migration.showHistory();
        } else if (action.equals("undo")) {
            MigrationApp migration = new MigrationApp(initModel);
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
        System.out.println("Usage: java -jar migration-tool.jar [options]");
        System.out.println("Options:");
        System.out.println("  --path     Path to migrations folder (required).");
        System.out.println("  --action   Action to perform: 'migrate' or 'undo'.");
        System.out.println("  --version  Version to undo (required for 'undo' action).");
        System.out.println("  --driver   Database driver (required).");
        System.out.println("  --url      Database URL (required).");
        System.out.println("  --user     Database user (optional).");
        System.out.println("  --password Database password (optional).");
        System.out.println("Example:");
        System.out.println("  java -jar migration-tool.jar --path=/path/to/migrations --action=migrate --driver=com.mysql.cj.jdbc.Driver --url=jdbc:mysql://localhost:3306/db_name --user=root --password=password");
    }
}