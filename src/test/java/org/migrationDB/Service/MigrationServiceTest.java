package org.migrationDB.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MigrationServiceTest {

    @Test
    void splitQueries() {
        MigrationService migrationService = new MigrationService();
        String fileScripts = "CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(255));\n" +
                "CREATE TABLE IF NOT EXISTS products (id SERIAL PRIMARY KEY, name VARCHAR(255));";
        assertEquals(2, migrationService.splitQueries(fileScripts).size());
    }
}