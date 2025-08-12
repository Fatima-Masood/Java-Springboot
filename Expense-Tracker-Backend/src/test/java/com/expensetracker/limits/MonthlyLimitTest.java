package com.expensetracker.limits;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyLimitTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        MonthlyLimit limit = new MonthlyLimit("1", "alice", "2025-08", 1500.0);

        assertEquals("1", limit.getId());
        assertEquals("alice", limit.getUsername());
        assertEquals("2025-08", limit.getMonth());
        assertEquals(1500.0, limit.getLimitAmount());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        MonthlyLimit limit = new MonthlyLimit();

        limit.setId("2");
        limit.setUsername("bob");
        limit.setMonth("2025-09");
        limit.setLimitAmount(2000.0);

        assertEquals("2", limit.getId());
        assertEquals("bob", limit.getUsername());
        assertEquals("2025-09", limit.getMonth());
        assertEquals(2000.0, limit.getLimitAmount());
    }

    @Test
    void testBuilder() {
        MonthlyLimit limit = MonthlyLimit.builder()
                .id("3")
                .username("carol")
                .month("2025-10")
                .limitAmount(3000.0)
                .build();

        assertEquals("3", limit.getId());
        assertEquals("carol", limit.getUsername());
        assertEquals("2025-10", limit.getMonth());
        assertEquals(3000.0, limit.getLimitAmount());
    }

    @Test
    void testEqualsAndHashCode() {
        MonthlyLimit l1 = new MonthlyLimit("1", "dave", "2025-08", 1500.0);
        MonthlyLimit l2 = new MonthlyLimit("1", "dave", "2025-08", 1500.0);
        MonthlyLimit l3 = new MonthlyLimit("2", "emma", "2025-09", 1800.0);

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());

        assertNotEquals(l1, l3);
        assertNotEquals(l1.hashCode(), l3.hashCode());
    }

    @Test
    void testToString() {
        MonthlyLimit limit = new MonthlyLimit("4", "fred", "2025-11", 2500.0);
        String output = limit.toString();

        assertTrue(output.contains("fred"));
        assertTrue(output.contains("2025-11"));
        assertTrue(output.contains("2500.0"));
    }

    @Test
    void testEquals_PartialNullMismatch() {
        MonthlyLimit l1 = new MonthlyLimit(null, "george", "2025-12", 500.0);
        MonthlyLimit l2 = new MonthlyLimit("id", "george", "2025-12", 500.0);
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());

        l1 = new MonthlyLimit("id", null, "2025-12", 500.0);
        l2 = new MonthlyLimit("id", "george", "2025-12", 500.0);
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());

        l1 = new MonthlyLimit("id", "george", null, 500.0);
        l2 = new MonthlyLimit("id", "george", "2025-12", 500.0);
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    void testEqualsWithSelfAndNull() {
        MonthlyLimit limit = new MonthlyLimit("5", "hannah", "2025-10", 1000.0);
        assertEquals(limit, limit);
        assertNotEquals(null, limit);
        assertNotEquals("some string", limit);
    }
    @Test
    void testHashCodeForEmptyObject() {
        MonthlyLimit emptyLimit1 = new MonthlyLimit();
        MonthlyLimit emptyLimit2 = new MonthlyLimit();

        assertEquals(emptyLimit1, emptyLimit2);
        assertEquals(emptyLimit1.hashCode(), emptyLimit2.hashCode());
    }

}
