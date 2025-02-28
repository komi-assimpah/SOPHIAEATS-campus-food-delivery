package fr.unice.polytech.junit.utils;

import fr.unice.polytech.utils.Utils;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void testGenerateUniqueId() {
        String id1 = Utils.generateUniqueId();
        String id2 = Utils.generateUniqueId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);

        assertTrue(id1.matches("^[a-f0-9\\-]{36}$"), "Generated ID should match UUID format");
        assertTrue(id2.matches("^[a-f0-9\\-]{36}$"), "Generated ID should match UUID format");
    }

    @Test
    void testIsInFutureWithFutureDate() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        assertTrue(Utils.isInFuture(futureDate), "Date in the future should return true");
    }

    @Test
    void testIsInFutureWithPastDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        assertFalse(Utils.isInFuture(pastDate), "Date in the past should return false");
    }

    @Test
    void testIsInFutureWithCurrentDate() {
        LocalDateTime currentDate = LocalDateTime.now();
        assertFalse(Utils.isInFuture(currentDate), "Current date should return false");
    }

}