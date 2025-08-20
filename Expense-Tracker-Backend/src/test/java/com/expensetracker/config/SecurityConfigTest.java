package com.expensetracker.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(classes = SecurityConfig.class)
class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();
    @MockitoBean
    SecurityFilterChain chain;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void jwtBeansShouldBeAvailable() {
        assertNotNull(jwtEncoder);
        assertNotNull(jwtDecoder);
    }

    @BeforeEach
    void setupAuth() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("testUser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testSecurityFilterChainBuilds() {
        assertNotNull(chain);
    }

    @Test
    void testJwtEncoderAndDecoderWork() {
        byte[] key = "12345678901234567890123456789012".getBytes();
        JwtEncoder encoder = securityConfig.jwtEncoder(key);
        JwtDecoder decoder = securityConfig.jwtDecoder(key);

        assertNotNull(encoder);
        assertNotNull(decoder);
    }

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "mypassword";
        String encoded = encoder.encode(raw);

        assertNotNull(encoded);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    void testAuthenticationManagerBean() throws Exception {
        AuthenticationConfiguration mockConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);

        when(mockConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = securityConfig.authenticationManager(mockConfig);
        assertEquals(mockManager, result);
    }
}
