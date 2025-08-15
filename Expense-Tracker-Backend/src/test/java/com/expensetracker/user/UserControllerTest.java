package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.expenditure.ExpenditureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Slf4j
@AutoConfigureMockMvc(addFilters = false)

public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ExpenditureRepository expenditureRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    //-------------------
    //---REGISTER USER---
    //-------------------
    @Test
    void testRegister_success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("john");
        userDTO.setPassword("password123");
        String token = "mock-jwt-token";

        when(userService.register(eq("john"), eq("password123"), any(), eq(authenticationManager), eq(jwtEncoder) ))
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
        String token = "mock-jwt-token";

        when(userService.register(eq(userDTO.getUsername()), eq(userDTO.getPassword()), any(), eq(authenticationManager), eq(jwtEncoder)))
                .thenReturn(token);

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

        when(userService.loginUser(eq("john"), eq("password"), any(), eq(authenticationManager), eq(jwtEncoder)))
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
    @WithMockUser(username = "user1")
    void testUpdatePassword_success() throws Exception {
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        when(userService.updatePassword(any(PasswordUpdateRequest.class))).thenReturn(0);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated"));

        verify(userService).updatePassword(any(PasswordUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void testUpdatePassword_incorrectOldPassword() throws Exception {
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass");

        when(userService.updatePassword(any(PasswordUpdateRequest.class))).thenReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect old password"));

        verify(userService).updatePassword(any(PasswordUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void testUpdatePassword_userNotFound() throws Exception {
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        when(userService.updatePassword(any(PasswordUpdateRequest.class))).thenReturn(-1);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(userService).updatePassword(any(PasswordUpdateRequest.class));
    }

    //-------------------
    //----DELETE USER----
    //-------------------
    @Test
    @WithMockUser(username = "john")
    void testDeleteUser_success() throws Exception {
        User user = new User();
        user.setUsername("john");

        when(userService.deleteUser()).thenReturn(0);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User and related expenditures deleted"));

        verify(userService, times(1)).deleteUser();
        verifyNoInteractions(expenditureRepository, userRepository);

    }

    @Test
    @WithMockUser(username = "notfound")
    void testDeleteUser_userNotFound() throws Exception {
        when(userService.deleteUser()).thenReturn(-1); // Force the "not found" branch

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser();
        verifyNoInteractions(expenditureRepository, userRepository);
    }

    //-------------------
    //-----GET USER------
    //-------------------
    @Test
    void testGetUser_success() throws Exception {
        Authentication authentication = new TestingAuthenticationToken("john", null);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("john"));
    }

    @Test
    void testGetUser_forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string("Login First"));
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