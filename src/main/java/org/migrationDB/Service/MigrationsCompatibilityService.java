package org.migrationDB.Service;

import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Exception.CheckSumMismatchException;
import org.migrationDB.Exception.MigrationException;
import org.migrationDB.Exception.MigrationFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MigrationsCompatibilityService {

    public static void checkCompatibility(DatabaseConnection dbConnection, List<MigrationSchema> migrationSchemas) {
        for (MigrationSchema ms : migrationSchemas) {
            String filePath = dbConnection.getPathToMigrationFiles() + ms.script_name();
            try (InputStream inputStream = MigrationsCompatibilityService.class.getClassLoader().getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new MigrationFileException("Can't find file " + filePath);
                }
                String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String currentFileCheckSum = calculateCheckSum(fileContent);
                if (!currentFileCheckSum.equals(ms.checksum()) && ms.script_name().startsWith("V")) {
                    throw new CheckSumMismatchException("Migrated file has been changed: " + filePath);
                }
            } catch (IOException e) {
                throw new MigrationFileException("Can't read file " + filePath, e);
            }
        }
    }

    public static String calculateCheckSum(String fileContent) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new MigrationException("Can't calculate checksum", e);
        }
        byte[] hashBytes = digest.digest(fileContent.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static String calculateCheckSumFromFile(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return calculateCheckSum(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MigrationFileException("Can't read file " + file.getName(), e);
        }
    }
}
