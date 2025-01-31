package org.migrationDB.Exception;

public class DatabaseConnectionException extends MigrationException{
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    public DatabaseConnectionException(String message) {
        super(message);
    }
}
