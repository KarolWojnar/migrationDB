package org.migrationDB.Service;

import org.migrationDB.Exception.MigrationSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.sql.ResultSet;

public class MigrationHistoryService {
    private final static String VERSION_TABLE = "version_control";
    private final static Logger log = LoggerFactory.getLogger(MigrationHistoryService.class);

    public void recordMigrationFile(Connection conn, String fileName, String fileScripts) {
        int version = Integer.parseInt(fileName.split("__")[0].substring(1));
        String checkSum = MigrationsCompatibilityService.calculateCheckSum(fileScripts);
        String query = "INSERT INTO " + VERSION_TABLE + " (version, script_name, checksum, success) VALUES (?, ?, ?, ?)";
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

    public void updateRepeatable(Connection conn, String scriptName, String currentFileCheckSum) {
        String query = "UPDATE " + VERSION_TABLE + " SET checksum = ? WHERE script_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, currentFileCheckSum);
            ps.setString(2, scriptName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new MigrationSqlException("Error updating repeatable migration.", e);
        }
    }

    public boolean checkIsInDatabase(Connection conn, String fileName) {
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
