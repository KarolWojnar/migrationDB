package org.migrationDB.DatabseService;

import org.migrationDB.Migrations.CheckSumCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class ExecuteQuery {
    private final static String VERSION_TABLE = "version_control";
    private static final Logger log = LoggerFactory.getLogger(ExecuteQuery.class);

    public static void executeScript(Connection conn, String script) {
        try (Statement statement = conn.createStatement()) {
            if (!script.trim().isEmpty()) {
                statement.execute(script); // todo: make it more safe
            }
        } catch (SQLException e) {
            log.error("Error in script: {}", script);
            throw new RuntimeException(e);
        }
    }

    public static void recordMigrationFile(Connection conn, String fileName, String fileScripts) throws NoSuchAlgorithmException, IOException, SQLException {
        int version = Integer.parseInt(fileName.split("__")[0].substring(1));
        String checkSum = CheckSumCalculator.calculateCheckSum(fileScripts);
        String query = String.format("INSERT INTO %s (version, script_name, checksum, success) VALUES (?, ?, ?, ?)",
                VERSION_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, version);
            ps.setString(2, fileName);
            ps.setString(3, checkSum);
            ps.setBoolean(4, true);
            ps.executeUpdate();
        }
    }
}
