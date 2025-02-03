package org.migrationDB.Data;

public record MigrationSchema(
        int id,
        int version,
        String script_name,
        String checksum,
        String created_at,
        boolean success) {
}
