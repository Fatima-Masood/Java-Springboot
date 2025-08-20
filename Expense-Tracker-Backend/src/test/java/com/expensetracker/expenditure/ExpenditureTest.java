package com.expensetracker.expenditure;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class ExpenditureTest {

    @Test
    void testCopyConstructorCopiesAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Expenditure original = Expenditure.builder()
                .id("123")
                .user("testuser")
                .title("Lunch")
                .amount(15.5)
                .yearMonth(YearMonth.of(2024, 8).toString())
                .timestamp(now)
                .build();

        Expenditure copy = new Expenditure(original);

        assertNotNull(copy);
        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getUser(), copy.getUser());
        assertEquals(original.getTitle(), copy.getTitle());
        assertEquals(original.getAmount(), copy.getAmount());
        assertEquals(original.getYearMonth(), copy.getYearMonth());
    }

    @Test
    void testCopyConstructorWithNullArgument() {
        Expenditure copy = new Expenditure(null);
        assertNotNull(copy);
        assertNull(copy.getId());
        assertNull(copy.getUser());
        assertNull(copy.getTitle());
        assertNull(copy.getYearMonth());
        assertEquals(0, copy.getAmount());
        assertNotNull(copy.getTimestamp());
    }
}
