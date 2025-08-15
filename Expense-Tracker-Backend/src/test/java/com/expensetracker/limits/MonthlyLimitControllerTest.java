package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.expenditure.Expenditure;
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

import java.time.YearMonth;
import java.util.Collections;

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
    private MonthlyLimitService monthlyLimitService;

    @MockitoBean
    private Authentication authentication;

    @Test
    void testGetMonthlySummary_WithLimit() throws Exception {
        when(authentication.getName()).thenReturn("user1");

        Expenditure expenditure = new Expenditure();
        expenditure.setAmount(250.0);
        expenditure.setTitle("Groceries");

        MonthlySummaryResponse response = new MonthlySummaryResponse(
                YearMonth.of(2024, 8),
                1000.0,
                250.0,
                Collections.singletonList(expenditure)
        );

        when(monthlyLimitService.getMonthlySummary("user1", 2024, 8)).thenReturn(response);

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
        expenditure.setTitle("Groceries");

        MonthlySummaryResponse response = new MonthlySummaryResponse(
                YearMonth.of(2024, 8),
                0.0,
                250.0,
                Collections.singletonList(expenditure)
        );

        when(monthlyLimitService.getMonthlySummary("user1", 2024, 8)).thenReturn(response);

        mockMvc.perform(get("/api/expenditures/monthly/monthly-summary")
                        .param("year", "2024")
                        .param("month", "8")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(250.0))
                .andExpect(jsonPath("$.limitAmount").value(0.0))
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
        when(monthlyLimitService.setMonthlyLimit("user2", "2025", "01", 750.0))
                .thenReturn("Monthly limit set successfully");

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
