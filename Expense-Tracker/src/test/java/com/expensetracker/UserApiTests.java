package com.expensetracker;

import com.expensetracker.config.JwtAuthFilter;
import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.expenses.ExpenditureRepository;
import com.expensetracker.user.User;
import com.expensetracker.user.UserController;
import com.expensetracker.user.UserRepository;
import com.expensetracker.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Slf4j
@AutoConfigureMockMvc(addFilters = false)

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

    //-------------------
    //---REGISTER USER---
    //-------------------
    @Test
    void testRegister_success() throws Exception {
        UserDTO userDTO = new UserDTO("john", "password123");
        String token = "mock-jwt-token";

        when(userService.register(eq("john"), eq("password123"), any(), eq(authenticationManager), eq(jwtEncoder)))
                .thenReturn(token);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }

    @Test
    void testRegister_incompleteCredentials() throws Exception {
        UserDTO userDTO = new UserDTO(null, "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Incomplete credentials"));
    }

    //-------------------
    //-----LOGIN USER----
    //-------------------
    @Test
    void testLogin_success() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setPassword("password");

        when(userService.loginUser(eq(user), any(), eq(authenticationManager), eq(jwtEncoder)))
                .thenReturn("mock-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-token"));
    }

    @Test
    void testLogin_incompleteCredentials() throws Exception {
        User user = new User();
        user.setUsername(null);
        user.setPassword("pass");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Incomplete credentials"));
    }

    //-------------------
    //--UPDATE PASSWORD--
    //-------------------
    @Test
    @WithMockUser(username = "john")
    void testUpdatePassword_success() throws Exception {
        PasswordUpdateRequest req = new PasswordUpdateRequest("oldPass", "newPass");
        User user = new User();
        user.setUsername("john");
        user.setPassword("encodedOldPass");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated"));
    }

    @Test
    @WithMockUser(username = "john")
    void testUpdatePassword_wrongOldPassword() throws Exception {
        PasswordUpdateRequest req = new PasswordUpdateRequest("wrongOld", "newPass");
        User user = new User();
        user.setUsername("john");
        user.setPassword("encodedOldPass");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "encodedOldPass")).thenReturn(false);

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect old password"));
    }

    @Test
    @WithMockUser(username = "ghost")
    void testUpdatePassword_userNotFound() throws Exception {
        PasswordUpdateRequest req = new PasswordUpdateRequest("any", "new");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    //-------------------
    //----DELETE USER----
    //-------------------
    @Test
    @WithMockUser(username = "john")
    void testDeleteUser_success() throws Exception {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User and related expenditures deleted"));

        verify(expenditureRepository).deleteByUser("john");
        verify(userRepository).delete(user);
    }

    @Test
    @WithMockUser(username = "notfound")
    void testDeleteUser_userNotFound() throws Exception {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    //-------------------
    //-----GET USER------
    //-------------------
    @Test
    @WithMockUser(username = "john")
    void testGetUser_success() throws Exception {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("john"));
    }

    @Test
    @WithMockUser(username = "unknown")
    void testGetUser_notFound() throws Exception {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isNotFound());
    }

    //-------------------
    //----LOGOUT USER----
    //-------------------
    @Test
    void testLogout_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Logged out successfully\"}"));
    }


}