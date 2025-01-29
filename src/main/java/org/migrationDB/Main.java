package org.migrationDB;

import org.migrationDB.DatabseService.DatabaseConnection;
import org.migrationDB.Migrations.MigrationExecutor;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection dbConnection = new DatabaseConnection(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/test",
                "root",
                "",
                "migrations/"
        );

        MigrationExecutor.makeMigration(dbConnection);
    }
}