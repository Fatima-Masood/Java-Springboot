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

import java.time.LocalDateTime;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //-------------------------
    // SET MONTHLY LIMIT
    //-------------------------
    @Test
    void testSetMonthlyLimit_NewLimit() {
        when(monthlyLimitRepository.findByUsernameAndMonth(eq(mockUser), eq("2025-08")))
                .thenReturn(Optional.empty());

        MonthlyLimit saved = new MonthlyLimit();
        saved.setUsername(mockUser);
        saved.setMonth("2025-08");
        saved.setLimitAmount(500);

        when(monthlyLimitRepository.save(any(MonthlyLimit.class))).thenReturn(saved);

        String result = monthlyLimitService.setMonthlyLimit(mockUser, "2025", "08", 500);

        assertEquals("Monthly limit set successfully", result);

        ArgumentCaptor<MonthlyLimit> captor = ArgumentCaptor.forClass(MonthlyLimit.class);
        verify(monthlyLimitRepository).save(captor.capture());
        assertEquals(mockUser, captor.getValue().getUsername());
        assertEquals("2025-08", captor.getValue().getMonth());
        assertEquals(500, captor.getValue().getLimitAmount());
    }

    @Test
    void testSetMonthlyLimit_UpdateExisting() {
        MonthlyLimit existing = new MonthlyLimit();
        existing.setUsername(mockUser);
        existing.setMonth("2025-08");
        existing.setLimitAmount(300);

        when(monthlyLimitRepository.findByUsernameAndMonth(eq(mockUser), eq("2025-08")))
                .thenReturn(Optional.of(existing));

        monthlyLimitService.setMonthlyLimit(mockUser, "2025", "08", 800);

        verify(monthlyLimitRepository).save(existing);
        assertEquals(800, existing.getLimitAmount());
    }

    //-------------------------
    // GET MONTHLY SUMMARY
    //-------------------------
    @Test
    void testGetMonthlySummary_WithLimitAndExpenses() {
        int year = 2025, month = 8;
        YearMonth ym = YearMonth.of(year, month);

        Expenditure e1 = new Expenditure();
        e1.setAmount(100);
        Expenditure e2 = new Expenditure();
        e2.setAmount(50);
        List<Expenditure> expenses = List.of(e1, e2);

        when(expenditureRepository.findByUserAndTimestampBetween(
                eq(mockUser), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expenses);

        MonthlyLimit limit = new MonthlyLimit();
        limit.setLimitAmount(500);
        when(monthlyLimitRepository.findByUsernameAndMonth(eq(mockUser), eq(ym.toString())))
                .thenReturn(Optional.of(limit));

        MonthlySummaryResponse response = monthlyLimitService.getMonthlySummary(mockUser, year, month);

        assertEquals(ym, response.getMonth());
        assertEquals(500, response.getLimitAmount());
        assertEquals(150, response.getTotalSpent());
        assertEquals(2, response.getExpenses().size());
    }

    @Test
    void testGetMonthlySummary_NoLimit_NoExpenses() {
        int year = 2025, month = 8;
        YearMonth ym = YearMonth.of(year, month);

        when(expenditureRepository.findByUserAndTimestampBetween(
                eq(mockUser), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(monthlyLimitRepository.findByUsernameAndMonth(eq(mockUser), eq(ym.toString())))
                .thenReturn(Optional.empty());

        MonthlySummaryResponse response = monthlyLimitService.getMonthlySummary(mockUser, year, month);

        assertEquals(0, response.getLimitAmount());
        assertEquals(0, response.getTotalSpent());
        assertTrue(response.getExpenses().isEmpty());
    }
}
