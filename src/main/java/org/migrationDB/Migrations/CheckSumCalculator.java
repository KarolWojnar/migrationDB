package org.migrationDB.Migrations;

import org.migrationDB.Exception.MigrationException;
import org.migrationDB.Exception.MigrationFileException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumCalculator {

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
