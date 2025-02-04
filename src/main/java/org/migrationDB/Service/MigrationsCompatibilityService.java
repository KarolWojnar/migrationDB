package org.migrationDB.Service;

import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Config.DatabaseConnection;
import org.migrationDB.Exception.CheckSumMismatchException;
import org.migrationDB.Exception.MigrationException;
import org.migrationDB.Exception.MigrationFileException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MigrationsCompatibilityService {

    public static void checkCompatibility(DatabaseConnection dbConnection, List<MigrationSchema> migrationSchemas) {
        for (MigrationSchema ms : migrationSchemas) {
            String filePath = dbConnection.getPathToMigrationFiles() + ms.script_name();
            Path path = Paths.get(filePath);
            try {
                String fileContent = Files.readString(path, StandardCharsets.UTF_8);
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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
        } catch (NoSuchAlgorithmException e) {
            throw new MigrationException("Can't calculate checksum", e);
        }

    }

    public static void checkMaxVersion(int maxVersion, List<String> versionedFiles) {
        for (String file : versionedFiles) {
            int version = Integer.parseInt(file.split("__")[0].substring(1));
            if (version < maxVersion) {
                throw new MigrationFileException("Newer version (" + maxVersion + ") of migration is already migrated.");
            }
        }
    }
}
