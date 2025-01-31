package org.migrationDB.DatabaseService;

import org.migrationDB.Exception.MigrationSqlException;
import org.migrationDB.Migrations.CheckSumCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.sql.ResultSet;

public class ExecuteQuery {
    private final static String VERSION_TABLE = "version_control";
    private static final Logger log = LoggerFactory.getLogger(ExecuteQuery.class);

    public static void recordMigrationFile(Connection conn, String fileName, String fileScripts) {
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
            log.info("New migration {} saved.", fileName);
        } catch (SQLException e) {
            throw new MigrationSqlException("Error recording migration file.", e);
        }
    }

    public static void updateRepeatable(Connection conn, String scriptName, String currentFileCheckSum) {
        String query = "UPDATE " + VERSION_TABLE + " SET checksum = ? WHERE script_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, currentFileCheckSum);
            ps.setString(2, scriptName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new MigrationSqlException("Error updating repeatable migration.", e);
        }
    }

    public static boolean checkIsInDatabase(Connection conn, String fileName) {
        String query = "SELECT COUNT(*) FROM " + VERSION_TABLE + " WHERE script_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, fileName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new MigrationSqlException("Error checking if migration is in database.", e);
        }
        return false;
    }
}
