package org.migrationDB.DatabseService;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ExecuteQuery {
    private final static String VERSION_TABLE = "version_control";

    public static Map<String, String> findNameAndCheckSumById(Connection conn, int i) {
        try {
            String query = "SELECT script_name, checksum FROM " + VERSION_TABLE + " WHERE id = ?";
            try (var stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, i);
                try (var rs = stmt.executeQuery()) {
                    Map<String, String> result = new HashMap<>();
                    if (rs.next()) {
                        result.put("script_name", rs.getString(1));
                        result.put("checksum", rs.getString(2));
                        return result;
                    } else {
                        throw new RuntimeException("No script found with id " + i);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong during executing query. ", e);
        }
    }
}
