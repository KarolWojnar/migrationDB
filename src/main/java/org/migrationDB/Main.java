package org.migrationDB;

import org.migrationDB.Core.CLIMigration;

public class Main {

    public static void main(String[] args) {
        CLIMigration cli = new CLIMigration();
        cli.validateCliActions(args);
    }
}