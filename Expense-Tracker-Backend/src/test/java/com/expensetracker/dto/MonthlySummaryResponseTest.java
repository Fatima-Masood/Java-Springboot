package com.expensetracker.dto;

import com.expensetracker.expenditure.Expenditure;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthlySummaryResponseTest {

    @Test
    void testAllArgsConstructorAndDefensiveCopy() {
        YearMonth month = YearMonth.of(2023, 8);
        List<Expenditure> expenses = new ArrayList<>();
        expenses.add(new Expenditure("1", "user1", "Groceries", 100.0, null));

        MonthlySummaryResponse response =
                new MonthlySummaryResponse(month, 2000.0, 500.0, expenses);

        assertEquals(month, response.getMonth());
        assertEquals(2000.0, response.getLimitAmount());
        assertEquals(500.0, response.getTotalSpent());
        assertEquals(expenses, response.getExpenses());
        assertNotSame(expenses, response.getExpenses(), "Should store a copy, not same reference");

        expenses.add(new Expenditure("2", "user2", "Rent", 500.0, null));
        assertEquals(1, response.getExpenses().size());
    }

    @Test
    void testSetExpensesWithNull() {
        MonthlySummaryResponse response =
                new MonthlySummaryResponse(YearMonth.of(2023, 1), 1000.0, 100.0, null);

        assertNull(response.getExpenses(), "Expenses should be null when initialized with null");

        response.setExpenses(null);
        assertNull(response.getExpenses(), "Setting expenses to null should keep it null");
    }

    @Test
    void testGetExpensesDefensiveCopy() {
        List<Expenditure> expenses = new ArrayList<>();
        expenses.add(new Expenditure("1", "user1", "Utilities", 150.0, null));

        MonthlySummaryResponse response =
                new MonthlySummaryResponse(YearMonth.of(2023, 5), 3000.0, 1000.0, expenses);

        List<Expenditure> retrieved = response.getExpenses();
        assertEquals(expenses, retrieved);
        assertNotSame(expenses, retrieved, "Returned list should be a new copy");

        retrieved.add(new Expenditure("2", "user2", "Transport", 50.0, null));
        assertEquals(1, response.getExpenses().size());
    }

    @Test
    void testBasicSettersAndGetters() {
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                YearMonth.of(2023, 6), 5000.0, 2000.0, new ArrayList<>());

        response.setLimitAmount(6000.0);
        response.setTotalSpent(2500.0);
        response.setMonth(YearMonth.of(2024, 1));

        assertEquals(6000.0, response.getLimitAmount());
        assertEquals(2500.0, response.getTotalSpent());
        assertEquals(YearMonth.of(2024, 1), response.getMonth());
    }
}
