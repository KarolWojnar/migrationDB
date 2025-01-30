package org.migrationDB.Migrations;

import org.migrationDB.DatabaseService.DatabaseConnection;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MigrationsCompatibility {

    public static void checkCompatibility(DatabaseConnection dbConnection, List<MigrationSchema> migrationSchemas) throws NoSuchAlgorithmException {
        for (MigrationSchema ms : migrationSchemas) {
            String filePath = dbConnection.getPathToMigrationFiles() + ms.script_name();
            try (InputStream inputStream = MigrationsCompatibility.class.getClassLoader().getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Can't find file " + filePath);
                }
                String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String currentFileCheckSum = CheckSumCalculator.calculateCheckSum(fileContent);
                if (!currentFileCheckSum.equals(ms.checksum()) && ms.script_name().startsWith("V")) {
                    throw new RuntimeException("Migrated file has been changed: " + filePath);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Can't find file.", e);
            } catch (IOException e) {
                throw new RuntimeException("Can't load file.", e);
            }
        }
    }
}
