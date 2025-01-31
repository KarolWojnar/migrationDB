package org.migrationDB.Migrations;

import org.migrationDB.DatabaseService.DatabaseConnection;
import org.migrationDB.Exception.CheckSumMismatchException;
import org.migrationDB.Exception.MigrationFileException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MigrationsCompatibility {

    public static void checkCompatibility(DatabaseConnection dbConnection, List<MigrationSchema> migrationSchemas) {
        for (MigrationSchema ms : migrationSchemas) {
            String filePath = dbConnection.getPathToMigrationFiles() + ms.script_name();
            try (InputStream inputStream = MigrationsCompatibility.class.getClassLoader().getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new MigrationFileException("Can't find file " + filePath);
                }
                String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String currentFileCheckSum = CheckSumCalculator.calculateCheckSum(fileContent);
                if (!currentFileCheckSum.equals(ms.checksum()) && ms.script_name().startsWith("V")) {
                    throw new CheckSumMismatchException("Migrated file has been changed: " + filePath);
                }
            } catch (IOException e) {
                throw new MigrationFileException("Can't read file " + filePath, e);
            }
        }
    }
}
