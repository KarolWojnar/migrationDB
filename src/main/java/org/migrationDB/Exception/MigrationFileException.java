package org.migrationDB.Exception;

public class MigrationFileException extends MigrationException{
    public MigrationFileException(String message, Throwable cause) {
        super(message, cause);
    }
    public MigrationFileException(String message) {
        super(message);
    }
}
