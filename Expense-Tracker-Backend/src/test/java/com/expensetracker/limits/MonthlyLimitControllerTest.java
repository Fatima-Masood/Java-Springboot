package com.expensetracker.limits;

import com.expensetracker.expenditure.Expenditure;
import com.expensetracker.expenditure.ExpenditureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonthlyLimitController.class)
@AutoConfigureMockMvc(addFilters = false)
class MonthlyLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonthlyLimitRepository monthlyLimitRepository;

    @MockitoBean
    private ExpenditureRepository expenditureRepository;

    @MockitoBean
    private Authentication authentication;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void testGetMonthlySummary_WithLimit() throws Exception {
        when(authentication.getName()).thenReturn("user1");

        Expenditure expenditure = new Expenditure();
        expenditure.setAmount(250.0);
        expenditure.setTimestamp(LocalDateTime.now());
        expenditure.setTitle("Groceries");

        YearMonth ym = YearMonth.of(2024, 8);
        when(expenditureRepository.findByUserAndTimestampBetween(
                Mockito.eq("user1"),
                Mockito.any(),
                Mockito.any()
        )).thenReturn(Collections.singletonList(expenditure));

        MonthlyLimit limit = new MonthlyLimit();
        limit.setLimitAmount(1000.0);
        when(monthlyLimitRepository.findByUsernameAndMonth("user1", "2024-08"))
                .thenReturn(Optional.of(limit));

        mockMvc.perform(get("/api/expenditures/monthly/monthly-summary")
                        .param("year", "2024")
                        .param("month", "8")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(250.0))
                .andExpect(jsonPath("$.limitAmount").value(1000.0))
                .andExpect(jsonPath("$.month").value("2024-08"))
                .andExpect(jsonPath("$.expenses[0].title").value("Groceries"));
    }

    @Test
    void testGetMonthlySummary_WithoutLimit() throws Exception {
        when(authentication.getName()).thenReturn("user1");

        Expenditure expenditure = new Expenditure();
        expenditure.setAmount(250.0);
        expenditure.setTimestamp(LocalDateTime.now());
        expenditure.setTitle("Groceries");

        YearMonth ym = YearMonth.of(2024, 8);
        when(expenditureRepository.findByUserAndTimestampBetween(
                Mockito.eq("user1"),
                Mockito.any(),
                Mockito.any()
        )).thenReturn(Collections.singletonList(expenditure));

        MonthlyLimit limit = new MonthlyLimit();
        when(monthlyLimitRepository.findByUsernameAndMonth("user1", "2024-08"))
                .thenReturn(Optional.of(limit));

        mockMvc.perform(get("/api/expenditures/monthly/monthly-summary")
                        .param("year", "2024")
                        .param("month", "8")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(250.0))
                .andExpect(jsonPath("$.limitAmount").value(0))
                .andExpect(jsonPath("$.month").value("2024-08"))
                .andExpect(jsonPath("$.expenses[0].title").value("Groceries"));
    }

    @Test
    void testGetMonthlySummary_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/expenditures/monthly/monthly-summary")
                        .param("year", "2024")
                        .param("month", "8"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Login First."));
    }

    @Test
    void testSetMonthlyLimit_WithAuth_NewLimit() throws Exception {
        when(authentication.getName()).thenReturn("user2");

        when(monthlyLimitRepository.findByUsernameAndMonth("user2", "2025-01"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/expenditures/monthly/set-limit")
                        .param("year", "2025")
                        .param("month", "01")
                        .param("limit", "750.0")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Monthly limit set successfully"));
    }

    @Test
    void testSetMonthlyLimit_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/expenditures/monthly/set-limit")
                        .param("year", "2025")
                        .param("month", "01")
                        .param("limit", "750.0"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Login First."));
    }
}
