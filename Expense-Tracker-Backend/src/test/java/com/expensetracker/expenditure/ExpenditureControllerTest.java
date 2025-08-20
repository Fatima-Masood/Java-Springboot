package com.expensetracker.expenditure;

import com.expensetracker.dto.ExpenditureDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(controllers = ExpenditureController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenditureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenditureService expenditureService;

    @Autowired
    private ObjectMapper objectMapper;

    private Authentication authentication;
    private final String mockUsername = "john";

    @BeforeEach
    void setup() {
        authentication = new TestingAuthenticationToken(mockUsername, null);
    }

    //-------------------------
    //-----ADD EXPENDITURE-----
    //-------------------------
    @Test
    void testAddExpenditure_Success() throws Exception {
        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("Lunch");
        dto.setAmount(15.5);

        Expenditure savedExp = new Expenditure();
        savedExp.setId("123");
        savedExp.setTitle("Lunch");
        savedExp.setAmount(15.5);
        savedExp.setYearMonth(YearMonth.of(2024, 8).toString());
        savedExp.setUser(mockUsername);

        when(expenditureService.addExpenditure(eq(mockUsername), any(ExpenditureDTO.class)))
                .thenReturn(savedExp);

        mockMvc.perform(post("/api/expenditures")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lunch"))
                .andExpect(jsonPath("$.yearMonth").value("2024-08"))
                .andExpect(jsonPath("$.user").value(mockUsername));
    }

    @Test
    void testAddExpenditure_MissingFields() throws Exception {
        ExpenditureDTO dto = new ExpenditureDTO();

        mockMvc.perform(post("/api/expenditures")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAddExpenditure_ForbiddenWhenNoAuthentication() throws Exception {
        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("Dinner");
        dto.setAmount(20.0);
        dto.setYearMonth(YearMonth.of(2025, 8).toString());

        mockMvc.perform(post("/api/expenditures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    //------------------------
    //---UPDATE EXPENDITURE---
    //------------------------
    @Test
    void testUpdateExpenditure_Success() throws Exception {
        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("New Title");
        dto.setAmount(30.0);

        Expenditure updated = new Expenditure();
        updated.setId("1");
        updated.setUser(mockUsername);
        updated.setTitle("New Title");
        updated.setAmount(20.0);

        when(expenditureService.updateExpenditure(eq(mockUsername), eq("1"), any(ExpenditureDTO.class)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/expenditures/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.amount").value(20.0));
    }

    @Test
    void testUpdateExpenditure_Forbidden() throws Exception {
        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("Test");
        dto.setAmount(50);

        when(expenditureService.updateExpenditure(eq(mockUsername), eq("1"), any(ExpenditureDTO.class)))
                .thenThrow(new SecurityException("Forbidden"));

        mockMvc.perform(put("/api/expenditures/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateExpenditure_NotFound() throws Exception {
        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("Test");
        dto.setAmount(50);

        when(expenditureService.updateExpenditure(eq(mockUsername), eq("2"), any(ExpenditureDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/expenditures/2")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Does not exist"));
    }

    //------------------------
    //---DELETE EXPENDITURE---
    //------------------------
    @Test
    void testDeleteExpenditure_Success() throws Exception {
        when(expenditureService.deleteExpenditure(eq(mockUsername), eq("1"))).thenReturn(true);

        mockMvc.perform(delete("/api/expenditures/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Expenditure deleted"));
    }

    @Test
    void testDeleteExpenditure_Forbidden() throws Exception {
        when(expenditureService.deleteExpenditure(eq(mockUsername), eq("1")))
                .thenThrow(new SecurityException("Forbidden"));

        mockMvc.perform(delete("/api/expenditures/1")
                        .principal(authentication))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteExpenditure_NotFound() throws Exception {
        when(expenditureService.deleteExpenditure(eq(mockUsername), eq("2"))).thenReturn(false);

        mockMvc.perform(delete("/api/expenditures/2")
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Does not exist"));
    }

    //-------------------------
    //--GET USER EXPENDITURES--
    //-------------------------
    @Test
    void testGetExpendituresByUser_Success() throws Exception {
        Expenditure e1 = new Expenditure();
        e1.setId("1");
        e1.setTitle("Milk");
        e1.setAmount(3.0);
        e1.setUser(mockUsername);

        when(expenditureService.getExpendituresByUser(mockUsername)).thenReturn(List.of(e1));

        mockMvc.perform(get("/api/expenditures")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Milk"))
                .andExpect(jsonPath("$[0].amount").value(3.0))
                .andExpect(jsonPath("$[0].user").value("john"));
    }

    @Test
    void testGetExpendituresByUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/expenditures"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Login First."));
    }
}
