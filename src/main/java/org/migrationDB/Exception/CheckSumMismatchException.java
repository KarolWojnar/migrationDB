package org.migrationDB.Exception;

public class CheckSumMismatchException extends MigrationException{
    public CheckSumMismatchException(String fileName) {
        super("Checksum mismatch for file: " + fileName);
    }
}
