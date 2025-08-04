package com.expensetracker.expenditure;

import com.expensetracker.config.JwtAuthFilter;
import com.expensetracker.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(controllers = ExpenditureController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenditureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private Authentication authentication;

    private String mockUsername = "john";


    @BeforeEach
    void setup() {
        authentication = new TestingAuthenticationToken(mockUsername, null);
    }

    //-------------------------
    //-----ADD EXPENDITURE-----
    //-------------------------
    @Test
    void testAddExpenditure_Success() throws Exception {
        Expenditure exp = new Expenditure();
        exp.setTitle("Lunch");
        exp.setAmount(15.5);

        Expenditure savedExp = new Expenditure();
        savedExp.setId("123");
        savedExp.setTitle("Lunch");
        savedExp.setAmount(15.5);
        savedExp.setUser(mockUsername);

        Mockito.when(expenditureRepository.save(any())).thenReturn(savedExp);

        mockMvc.perform(post("/api/expenditures")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lunch"))
                .andExpect(jsonPath("$.user").value(mockUsername));
    }

    @Test
    void testAddExpenditure_MissingFields() throws Exception {
        Expenditure exp = new Expenditure();  // no title or amount

        mockMvc.perform(post("/api/expenditures")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isNoContent());
    }

    //------------------------
    //---UPDATE EXPENDITURE---
    //------------------------
    @Test
    void testUpdateExpenditure_Success() throws Exception {
        Expenditure oldExp = new Expenditure();
        oldExp.setId("1");
        oldExp.setUser(mockUsername);
        oldExp.setTitle("Old Title");
        oldExp.setAmount(10.0);

        Expenditure updated = new Expenditure();
        updated.setId("1");
        updated.setUser(mockUsername);
        updated.setTitle("New Title");
        updated.setAmount(20.0);

        Mockito.when(expenditureRepository.findById("1")).thenReturn(Optional.of(oldExp));
        Mockito.when(expenditureRepository.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/expenditures/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.amount").value(20.0));
    }

    @Test
    void testUpdateExpenditure_Forbidden() throws Exception {
        Expenditure exp = new Expenditure();
        exp.setId("1");
        exp.setUser("other_user");
        exp.setTitle("Test");
        exp.setAmount(50);

        mockMvc.perform(put("/api/expenditures/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateExpenditure_Failure() throws Exception {
        Expenditure exp = new Expenditure();
        exp.setId("1");
        exp.setUser(mockUsername);
        exp.setTitle("Test");
        exp.setAmount(50);

        mockMvc.perform(put("/api/expenditures/2")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Does not exist"));
    }

    //------------------------
    //---DELETE EXPENDITURE---
    //------------------------
    @Test
    void testDeleteExpenditure_Success() throws Exception {
        Expenditure exp = new Expenditure();
        exp.setId("1");
        exp.setUser(mockUsername);

        when(expenditureRepository.findById("1")).thenReturn(Optional.of(exp));
        Mockito.doNothing().when(expenditureRepository).deleteById("1");

        mockMvc.perform(delete("/api/expenditures/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isOk())
                .andExpect(content().string("Expenditure deleted"));
    }

    @Test
    void testDeleteExpenditure_Forbidden() throws Exception {
        Expenditure exp = new Expenditure();
        exp.setId("1");
        exp.setUser("someone_else");

        when(expenditureRepository.findById("1")).thenReturn(Optional.of(exp));
        Mockito.doNothing().when(expenditureRepository).deleteById("1");

        mockMvc.perform(delete("/api/expenditures/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteExpenditure_Failure() throws Exception {
        Expenditure exp = new Expenditure();
        exp.setId("1");
        exp.setUser("someone_else");

        mockMvc.perform(delete("/api/expenditures/2")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
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
        e1.setUser("john");

        when(expenditureRepository.findByUser("john")).thenReturn(List.of(e1));

        MvcResult result = mockMvc.perform(get("/api/expenditures")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Milk"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amount").value(3.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user").value("john"))
                .andReturn();
    }

    @Test
    void testGetExpendituresByUser_Forbidden() throws Exception {
        when(expenditureRepository.findByUser("john")).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/api/expenditures"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Login First."))
                .andReturn();
    }

}

