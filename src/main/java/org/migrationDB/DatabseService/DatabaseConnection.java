package org.migrationDB.DatabseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);
    private final String url;
    private final String username;
    private final String password;
    private final String driver;
    private final String pathToMigrationFiles;

    public DatabaseConnection(String driver, String url, String username, String password, String pathToMigrationFiles) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
        this.pathToMigrationFiles = pathToMigrationFiles;
    }

    public String getPathToMigrationFiles() {
        return pathToMigrationFiles;
    }


    public Connection connect() throws SQLException {
        try {
            if (driver != null && !driver.isEmpty()) {
                Class.forName(driver);
            }
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found. ", e);
        }

        log.info("Connecting to database {}", url);
        return DriverManager.getConnection(url, username, password);
    }


}
