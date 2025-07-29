package com.expensetracker.user;


import com.expensetracker.config.JwtAuthFilter;
import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.expenses.ExpenditureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExpenditureRepository expenditureRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtEncoder jwtEncoder;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private String username;
    private String password;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLoginSuccess() throws Exception {
        username = "user1";
        password = "user1";

        User loginRequest = new User();
        loginRequest.setUsername(username);
        loginRequest.setPassword("user1");

        mockMvc.perform(post("/api/users/login")
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }


    @Test
    void testLoginFailure() throws Exception {
        User user = new User();
        user.setUsername("wronguser");
        user.setPassword("wrongpass");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdatePasswordSuccess() throws Exception {
        username = "user1";
        String oldPassword = "user1";
        String newPassword = "new-pass";

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword, List.of()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);

        mockMvc.perform(put("/api/users/" + username + "/password")
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

}