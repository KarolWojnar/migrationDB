package org.migrationDB.Service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    @Test
    void shouldSortFilesByVersion() {
        FileService fs = new FileService();
        List<String> files = List.of("V2__test.sql", "V12__init.sql", "V3__data.sql");
        List<String> sorted = fs.sortFilesByVersion(files);

        assertEquals("V12__init.sql", sorted.get(2));
        assertEquals("V3__data.sql", sorted.get(1));
    }

}