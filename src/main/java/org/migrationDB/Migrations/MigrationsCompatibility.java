package org.migrationDB.Migrations;

import org.migrationDB.DatabseService.DatabaseConnection;
import org.migrationDB.DatabseService.ExecuteQuery;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.Map;

public class MigrationsCompatibility {

    public static void checkCompatibility(Connection conn, DatabaseConnection dbConnection, int hId) throws NoSuchAlgorithmException {
        for (int i = 1; i <= hId; i++) {
            Map<String, String> result = ExecuteQuery.findNameAndCheckSumById(conn, i);
            String filePath = dbConnection.getPathToMigrationFiles() + result.get("script_name");
            try (InputStream inputStream = MigrationsCompatibility.class.getClassLoader().getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new RuntimeException("Can't find file " + filePath);
                }
                String currentFileCheckSum = CheckSumCalculator.calculateCheckSum(inputStream);
                if (!currentFileCheckSum.equals(result.get("checksum"))) {
                    throw new RuntimeException("Migrated files has been changed: " + filePath);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Can't find file.", e);
            } catch (IOException e) {
                throw new RuntimeException("Can't load file.", e);
            }
        }
    }
}
