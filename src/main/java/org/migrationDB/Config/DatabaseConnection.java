package org.migrationDB.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.migrationDB.Exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);
    private final HikariDataSource dataSource;
    private String pathToMigrationFiles = "migrations/";

    public DatabaseConnection(String driver, String url, String username, String password, String pathToMigrationFiles) {
        validateParameters(driver, url, username, password);
        HikariConfig config = setHikariConf(url, username, password, driver);
        this.dataSource = new HikariDataSource(config);
        this.pathToMigrationFiles = pathToMigrationFiles;
    }

    public DatabaseConnection(String driver, String url, String username, String password) {
        validateParameters(driver, url, username, password);
        HikariConfig config = setHikariConf(url, username, password, driver);
        this.dataSource = new HikariDataSource(config);
    }

    private HikariConfig setHikariConf(String url, String username, String password, String driver) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);

        return config;
    }

    public String getPathToMigrationFiles() {
        return pathToMigrationFiles;
    }


    public Connection connect() {
        log.info("Connecting to database...");
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Error connecting to database", e);
        }
    }

    private void validateParameters(String driver, String url, String username, String password) {
        if (driver == null || url == null || username == null || password == null) {
            throw new DatabaseConnectionException("Database connection parameters cannot be null");
        }
    }

}
