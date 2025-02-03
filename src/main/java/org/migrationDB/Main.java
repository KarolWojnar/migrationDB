package org.migrationDB;

import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Core.MigrationExecutor;
import org.migrationDB.Repository.VersionRepository;
import org.migrationDB.Service.FileService;
import org.migrationDB.Service.MigrationHistoryService;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection dbConnection = new DatabaseConnection(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/test",
                "root",
                "",
                "migrations/"
        );

        VersionRepository vr = new VersionRepository();
        MigrationHistoryService mhs = new MigrationHistoryService();
        FileService fs = new FileService();
        MigrationExecutor me = new MigrationExecutor(mhs, vr, fs);
        me.makeMigration(dbConnection);
    }
}