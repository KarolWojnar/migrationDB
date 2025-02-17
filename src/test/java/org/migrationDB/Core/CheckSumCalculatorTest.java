package org.migrationDB.Core;

import org.junit.jupiter.api.Test;
import org.migrationDB.Service.MigrationsCompatibilityService;

import static org.junit.jupiter.api.Assertions.*;

class CheckSumCalculatorTest {

    @Test
    void successChecksum() {
        String lineQuery = "INSERT INTO `test` (`id`, `name`, `age`) VALUES (1, 'John', 30);";
        String v1 = MigrationsCompatibilityService.calculateCheckSum(lineQuery);
        String v2 = MigrationsCompatibilityService.calculateCheckSum(lineQuery);
        assertEquals(v1, v2);
    }

    @Test
    void failChecksum() {
        String lineQuery1 = "INSERT INTO `test` (`id`, `name`, `age`) VALUES (1, 'John', 30);";
        String lineQuery2 = "INSERT INTO `test` (`id`, `name`, `age`) VALUES (1, 'Adam', 30);";
        String v1 = MigrationsCompatibilityService.calculateCheckSum(lineQuery1);
        String v2 = MigrationsCompatibilityService.calculateCheckSum(lineQuery2);
        assertNotEquals(v1, v2);
    }

}