package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.expenditure.Expenditure;
import com.expensetracker.expenditure.ExpenditureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MonthlyLimitServiceTest {

    @Mock
    private MonthlyLimitRepository monthlyLimitRepository;

    @Mock
    private ExpenditureRepository expenditureRepository;

    @InjectMocks
    private MonthlyLimitService monthlyLimitService;

    private final String mockUser = "john";
    private final YearMonth yearMonth = YearMonth.of(2025, 8);
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //-------------------------
    // SET MONTHLY LIMIT
    //-------------------------
    @Test
    void testSetMonthlyLimit_NewLimit() {

        when(monthlyLimitRepository.findByUsernameAndYearMonth(eq(mockUser), eq(yearMonth.toString())))
                .thenReturn(Optional.empty());

        MonthlyLimit saved = new MonthlyLimit();
        saved.setUsername(mockUser);
        saved.setYearMonth(yearMonth.toString());
        saved.setLimitAmount(500);

        when(monthlyLimitRepository.save(any(MonthlyLimit.class))).thenReturn(saved);

        String result = monthlyLimitService.setMonthlyLimit(mockUser, yearMonth, 500);

        assertEquals("Monthly limit set successfully", result);

        ArgumentCaptor<MonthlyLimit> captor = ArgumentCaptor.forClass(MonthlyLimit.class);
        verify(monthlyLimitRepository).save(captor.capture());
        assertEquals(mockUser, captor.getValue().getUsername());
        assertEquals(yearMonth.toString(), captor.getValue().getYearMonth());
        assertEquals(500, captor.getValue().getLimitAmount());
    }

    @Test
    void testSetMonthlyLimit_UpdateExisting() {
        MonthlyLimit existing = new MonthlyLimit();
        existing.setUsername(mockUser);
        existing.setYearMonth(yearMonth.toString());
        existing.setLimitAmount(300);

        when(monthlyLimitRepository.findByUsernameAndYearMonth(eq(mockUser), eq(yearMonth.toString())))
                .thenReturn(Optional.of(existing));

        monthlyLimitService.setMonthlyLimit(mockUser, yearMonth, 800);

        verify(monthlyLimitRepository).save(existing);
        assertEquals(800, existing.getLimitAmount());
    }

    //-------------------------
    // GET MONTHLY SUMMARY
    //-------------------------
    @Test
    void testGetMonthlySummary_WithLimitAndExpenses() {
        Expenditure e1 = new Expenditure();
        e1.setAmount(100);
        Expenditure e2 = new Expenditure();
        e2.setAmount(50);
        List<Expenditure> expenses = List.of(e1, e2);

        when(expenditureRepository.findByUserAndYearMonth(
                eq(mockUser), eq(yearMonth.toString())))
                .thenReturn(expenses);

        MonthlyLimit limit = new MonthlyLimit();
        limit.setLimitAmount(500);
        when(monthlyLimitRepository.findByUsernameAndYearMonth(eq(mockUser), eq(yearMonth.toString())))
                .thenReturn(Optional.of(limit));

        MonthlySummaryResponse response = monthlyLimitService.getMonthlySummary(mockUser, yearMonth);

        assertEquals("2025-08", response.getYearMonth());
        assertEquals(500, response.getLimitAmount());
        assertEquals(150, response.getTotalSpent());
        assertEquals(2, response.getExpenses().size());
    }

    @Test
    void testGetMonthlySummary_NoLimit_NoExpenses() {
        when(expenditureRepository.findByUserAndYearMonth(
                eq(mockUser), eq(yearMonth.toString())))
                .thenReturn(List.of());

        when(monthlyLimitRepository.findByUsernameAndYearMonth(eq(mockUser), eq(yearMonth.toString())))
                .thenReturn(Optional.empty());

        MonthlySummaryResponse response = monthlyLimitService.getMonthlySummary(mockUser, yearMonth);

        assertEquals(0, response.getLimitAmount());
        assertEquals(0, response.getTotalSpent());
        assertTrue(response.getExpenses().isEmpty());
    }
}
