package org.migrationDB.Service;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.migrationDB.Data.MigrationSchema;
import org.migrationDB.Exception.MigrationSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MigrationService {
    private final static String VERSION_TABLE = "version_control";
    private final static Logger log = LoggerFactory.getLogger(MigrationService.class);

    public void recordMigrationFile(Connection conn, String fileName, String fileScripts) {
        int version = Integer.parseInt(fileName.split("__")[0].substring(1));
        String type = fileName.split("__")[0].substring(0, 1);
        String checkSum = MigrationsCompatibilityService.calculateCheckSum(fileScripts);
        String query = "INSERT INTO " + VERSION_TABLE + " (version, type, script_name, checksum, success) VALUES (?, ?, ?, ?, ?)";
        try {
            QueryExecutorService.executeQuery(conn, query, version, type, fileName, checkSum, true);
        } catch (SQLException e) {
            throw new MigrationSqlException("Error recording migration file.", e);
        }
        log.info("New migration {} saved.", fileName);
    }

    public void updateRepeatable(Connection conn, String scriptName, String currentFileCheckSum) {
        String query = "UPDATE " + VERSION_TABLE + " SET checksum = ? WHERE script_name = ?";
        try {
            QueryExecutorService.executeQuery(conn, query, currentFileCheckSum, scriptName);
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

    public void deleteMigration(Connection conn, String version) {
        try {
            String query = "DELETE FROM " + VERSION_TABLE + " WHERE version = ? AND type = 'V' AND success = true ";
            QueryExecutorService.executeQuery(conn, query, version);
        } catch (SQLException e) {
            throw new MigrationSqlException("Error deleting migration.", e);
        }
    }

    public List<MigrationSchema> getVersionMigrationName(Connection conn, String version) {
        String query = "SELECT * FROM " + VERSION_TABLE + " WHERE type = ? AND version >= ? AND success = true ORDER BY version DESC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "V");
            ps.setString(2, version);
            ResultSet rs = ps.executeQuery();
            List<MigrationSchema> migrationNames = new ArrayList<>();
            while (rs.next()) {
                migrationNames.add(
                        new MigrationSchema(
                                rs.getInt("id"),
                                rs.getInt("version"),
                                rs.getString("type"),
                                rs.getString("script_name"),
                                rs.getString("checksum"),
                                rs.getString("created_at"),
                                rs.getBoolean("success")
                        )
                );
            }
            return migrationNames;
        } catch (SQLException e) {
            throw new MigrationSqlException("Error getting migration name.", e);
        }
    }

    public void executeQueries(Connection conn, String fileScripts) throws SQLException {
        List<String> queries = splitQueries(fileScripts);
        for (String query : queries) {
            query = query.trim();
            if (!query.isEmpty()) {
                try (Statement st = conn.createStatement()) {
                    st.execute(query);
                }
            }
        }
    }

    private List<String> splitQueries(String fileScripts) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(fileScripts);
            return statements.stream().map(Object::toString).toList();
        } catch (JSQLParserException e) {
            throw new MigrationSqlException("Error during parsing SQL queries.", e);
        }
    }
}
