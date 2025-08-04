package com.expensetracker;

import com.expensetracker.config.JwtAuthFilter;
import com.expensetracker.user.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.servlet.FilterChain;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @InjectMocks
    JwtAuthFilter jwtAuthFilter;

    @Mock
    JwtDecoder jwtDecoder;

    @Mock
    UserService userService;

    @Mock
    FilterChain filterChain;

    @Mock
    UserDetails userDetails;

    @Mock
    Jwt jwt;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Inject dependencies manually if needed
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(jwtAuthFilter, "userService", userService);
    }

    @Test
    void test_noCookie_proceedsNormally() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void test_validJWT_setsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie jwtCookie = new Cookie("access_token", "valid-token");
        request.setCookies(jwtCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("john");
        when(userService.loadUserByUsername("john")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void test_invalidJWT_returnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie jwtCookie = new Cookie("access_token", "bad-token");
        request.setCookies(jwtCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        StringWriter responseWriter = new StringWriter();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(new PrintWriter(responseWriter).toString());

        when(jwtDecoder.decode("bad-token")).thenThrow(new JwtException("Invalid"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid JWT"));

        verify(filterChain, times(0)).doFilter(any(), any());
    }


}
