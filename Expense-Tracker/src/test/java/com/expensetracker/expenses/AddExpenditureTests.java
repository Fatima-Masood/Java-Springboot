package com.expensetracker.expenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenditureController.class)
class AddExpenditureTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addExpenditureWhenUserMatchesJwtSubject() throws Exception {
        String username = "Fatima-Masood";

        Expenditure input = new Expenditure();
        input.setTitle("Lunch");
        input.setAmount(550);

        Expenditure saved = new Expenditure();
        saved.setUser(username);
        saved.setTitle(input.getTitle());
        saved.setAmount(input.getAmount());

        when(expenditureRepository.save(Mockito.any(Expenditure.class))).thenReturn(saved);

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claims(claims -> claims.put("sub", username))
                .build();

        mockMvc.perform(post("/api/user/{user}/expenditures", username)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value(username))
                .andExpect(jsonPath("$.title").value("Lunch"));
    }

    @Test
    void addExpenditureWhenUserDoesNotMatchAuthentication() throws Exception {
        String pathUser = "someone-else";
        String authenticatedUser = "Fatima-Masood";

        Expenditure input = new Expenditure();
        input.setTitle("Coffee");
        input.setAmount(375);

        mockMvc.perform(post("/api/user/{user}/expenditures", pathUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .principal(() -> authenticatedUser)) // Simulates different user
                .andExpect(status().isForbidden());
    }
}

