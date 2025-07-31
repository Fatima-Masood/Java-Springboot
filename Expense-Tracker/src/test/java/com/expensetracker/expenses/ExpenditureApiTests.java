package com.expensetracker.expenses;

import com.expensetracker.config.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ExpenditureController.class)
class ExpenditureApiTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private AuthenticationManager authenticationManager;

    String username = "Fatima-Masood";
    String password = "Fatima-Masood";

    @Test
    void addExpenditureSuccess() throws Exception {
        Expenditure input = new Expenditure();
        input.setTitle("Lunch");
        input.setAmount(550);

        MvcResult result = mockMvc.perform(post("/api/expenditures")
                        .with(csrf().asHeader())
                        .with(user("Fatima-Masood"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn();

        log.info("Result: {}", result.getResponse().getContentAsString());
    }

    @Test
    void addExpenditureFailure() throws Exception {
        Expenditure input = new Expenditure();
        input.setTitle("Lunch");
        input.setAmount(550);

        MvcResult result = mockMvc.perform(post("/api/expenditures")
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        log.info("Result: {}", result.getResponse().getContentAsString());
    }



}

