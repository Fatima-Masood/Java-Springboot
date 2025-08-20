package com.expensetracker.limits;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyLimitTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        MonthlyLimit limit = new MonthlyLimit("1", "alice", YearMonth.of(2025, 8).toString(), 1500.0);

        assertEquals("1", limit.getId());
        assertEquals("alice", limit.getUsername());
        assertEquals(YearMonth.of(2025, 8).toString(), limit.getYearMonth());
        assertEquals(1500.0, limit.getLimitAmount());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        MonthlyLimit limit = new MonthlyLimit();

        limit.setId("2");
        limit.setUsername("bob");
        limit.setYearMonth(YearMonth.of(2025, 9).toString());
        limit.setLimitAmount(2000.0);

        assertEquals("2", limit.getId());
        assertEquals("bob", limit.getUsername());
        assertEquals(YearMonth.of(2025, 9).toString(), limit.getYearMonth());
        assertEquals(2000.0, limit.getLimitAmount());
    }

    @Test
    void testBuilder() {
        MonthlyLimit limit = MonthlyLimit.builder()
                .id("3")
                .username("carol")
                .yearMonth(YearMonth.of(2025, 10).toString())
                .limitAmount(3000.0)
                .build();

        assertEquals("3", limit.getId());
        assertEquals("carol", limit.getUsername());
        assertEquals(YearMonth.of(2025, 10).toString(), limit.getYearMonth());
        assertEquals(3000.0, limit.getLimitAmount());
    }

    @Test
    void testEqualsAndHashCode() {
        MonthlyLimit l1 = new MonthlyLimit("1", "dave", YearMonth.of(2025, 8).toString(), 1500.0);
        MonthlyLimit l2 = new MonthlyLimit("1", "dave", YearMonth.of(2025, 8).toString(), 1500.0);
        MonthlyLimit l3 = new MonthlyLimit("2", "emma", YearMonth.of(2025, 9).toString(), 1800.0);

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());

        assertNotEquals(l1, l3);
        assertNotEquals(l1.hashCode(), l3.hashCode());
    }

    @Test
    void testToString() {
        MonthlyLimit limit = new MonthlyLimit("4", "fred", YearMonth.of(2025, 11).toString(), 2500.0);
        String output = limit.toString();

        assertTrue(output.contains("fred"));
        assertTrue(output.contains("2025-11"));
        assertTrue(output.contains("2500.0"));
    }

    @Test
    void testEquals_PartialNullMismatch() {
        String yearMonth = YearMonth.of(2025, 12).toString();
        MonthlyLimit l1 = new MonthlyLimit(null, "george", yearMonth, 500.0);
        MonthlyLimit l2 = new MonthlyLimit("id", "george", yearMonth, 500.0);
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());

        l1 = new MonthlyLimit("id", null, yearMonth, 500.0);
        l2 = new MonthlyLimit("id", "george", yearMonth, 500.0);
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());

        l1 = new MonthlyLimit("id", "george", null, 500.0);
        l2 = new MonthlyLimit("id", "george", yearMonth, 500.0);
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    void testEqualsWithSelfAndNull() {
        MonthlyLimit limit = new MonthlyLimit("5", "hannah", YearMonth.of(2025, 12).toString(), 1000.0);
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
