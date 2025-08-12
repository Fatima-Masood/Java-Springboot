package com.expensetracker.expenditure;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ExpenditureTest {

    @Test
    void testEquals_SameObject() {
        Expenditure exp = Expenditure.builder()
                .id("1")
                .user("user1")
                .title("Coffee")
                .amount(150.0)
                .timestamp(LocalDateTime.now())
                .build();

        assertEquals(exp, exp);
    }

    @Test
    void testEquals_EqualObjects() {
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 10, 0);
        Expenditure e1 = new Expenditure("1", "user", "Lunch", 500.0, time);
        Expenditure e2 = new Expenditure("1", "user", "Lunch", 500.0, time);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testEquals_NotEqualObjects() {
        Expenditure e1 = new Expenditure("1", "user", "Lunch", 500.0, LocalDateTime.now());
        Expenditure e2 = new Expenditure("2", "other", "Dinner", 600.0, LocalDateTime.now());

        assertNotEquals(e1, e2);
        assertNotEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testEquals_NullFields() {
        Expenditure e1 = new Expenditure(null, null, null, 0.0, null);
        Expenditure e2 = new Expenditure(null, null, null, 0.0, null);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testEquals_PartialNullMismatch() {
        LocalDateTime time = LocalDateTime.of(2025, 8, 4, 12, 0);

        Expenditure e1 = new Expenditure(null, "user", "Taxi", 100.0, time);
        Expenditure e2 = new Expenditure("1", "user", "Taxi", 100.0, time);
        assertNotEquals(e1, e2);

        e1 = new Expenditure("1", null, "Taxi", 100.0, time);
        e2 = new Expenditure("1", "user", "Taxi", 100.0, time);
        assertNotEquals(e1, e2);

        e1 = new Expenditure("1", "user", null, 100.0, time);
        e2 = new Expenditure("1", "user", "Taxi", 100.0, time);
        assertNotEquals(e1, e2);

        e1 = new Expenditure("1", "user", "Taxi", 100.0, null);
        e2 = new Expenditure("1", "user", "Taxi", 100.0, time);
        assertNotEquals(e1, e2);
    }


    @Test
    void testHashSetContainsExpenditure() {
        LocalDateTime time = LocalDateTime.of(2024, 5, 1, 12, 0);
        Expenditure e1 = new Expenditure("1", "user", "Taxi", 300.0, time);
        HashSet<Expenditure> set = new HashSet<>();
        set.add(e1);

        assertTrue(set.contains(new Expenditure("1", "user", "Taxi", 300.0, time)));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Expenditure exp = new Expenditure();
        exp.setId("10");
        exp.setUser("sara");
        exp.setTitle("Books");
        exp.setAmount(1000.0);
        LocalDateTime t = LocalDateTime.of(2025, 1, 1, 12, 0);
        exp.setTimestamp(t);

        assertEquals("10", exp.getId());
        assertEquals("sara", exp.getUser());
        assertEquals("Books", exp.getTitle());
        assertEquals(1000.0, exp.getAmount());
        assertEquals(t, exp.getTimestamp());
    }

    @Test
    void testBuilder_DefaultValues() {
        Expenditure exp = Expenditure.builder()
                .id("abc")
                .user("ali")
                .title("Electricity")
                .build();

        assertEquals(0.0, exp.getAmount());
    }

    @Test
    void testToString_NotNull() {
        Expenditure exp = new Expenditure("1", "john", "Groceries", 1200.0, LocalDateTime.now());
        String result = exp.toString();

        assertNotNull(result);
        assertTrue(result.contains("Groceries"));
    }

    @Test
    void testEquals_WithNullAndDifferentType() {
        Expenditure exp = new Expenditure("1", "john", "Rent", 500.0, LocalDateTime.now());

        assertNotEquals(null, exp);
        assertNotEquals("some string", exp);
    }
}
