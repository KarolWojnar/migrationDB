package org.migrationDB.Data;

public record InitModel(
        String driver,
        String url,
        String user,
        String password,
        String pathToScripts
) {
}