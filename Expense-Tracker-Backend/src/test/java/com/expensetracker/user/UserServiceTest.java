package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.expenditure.ExpenditureRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ExpenditureRepository expenditureRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private HttpServletResponse response;

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedpass");
        mockUser.setRole("USER");

        // Always return an authenticated Authentication object
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
    }

    @Test
    void loadUserByUsername_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        var userDetails = userService.loadUserByUsername("testuser");

        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("missing"));
    }

    @Test
    void authenticate_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("rawpass", "encodedpass")).thenReturn(true);

        var result = userService.authenticate("testuser", "rawpass");
        assertEquals(mockUser, result);
    }

    @Test
    void authenticate_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "encodedpass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.authenticate("testuser", "wrong"));
    }

    @Test
    void oAuthSignUp_NewUser() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(mockUser);

        var result = userService.OAuthSignUp("testuser", authenticationManager);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_NewUser_CreatesAndLogsIn() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpass");
        when(jwtEncoder.encode(any())).thenReturn(mock(Jwt.class));

        var result = userService.register("testuser", "pass", response, authenticationManager, jwtEncoder);
        assertTrue(result.contains("access_token"));
    }

    @Test
    void loginUser_SetsSecurityContextAndReturnsToken() {
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("token123");
        when(jwtEncoder.encode(any())).thenReturn(mockJwt);

        var result = userService.loginUser("testuser", "pass", response, authenticationManager, jwtEncoder);
        assertTrue(result.contains("token123"));
    }

    @Test
    void setJwtAndResponse_AddsCookie() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("abc123");

        userService.setJwtAndResponse(response, jwt);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie added = cookieCaptor.getValue();
        assertEquals("access_token", added.getName());
        assertEquals("abc123", added.getValue());
    }

    @Test
    void setJwt_ReturnsEncodedJwt() {
        Jwt expectedJwt = mock(Jwt.class);
        when(jwtEncoder.encode(any())).thenReturn(expectedJwt);

        var result = userService.setJwt("testuser", jwtEncoder);
        assertEquals(expectedJwt, result);
    }

    @Test
    void deleteCookie_AddsExpiredCookie() {
        userService.deleteCookie(response, "access_token");
        verify(response).addCookie(cookieCaptor.capture());
        assertEquals(0, cookieCaptor.getValue().getMaxAge());
    }

    @Test
    void deleteUser_UserExists() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(context.getAuthentication()).thenReturn(auth);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        int result = userService.deleteUser();
        assertEquals(0, result);
        verify(expenditureRepository).deleteByUser("testuser");
        verify(userRepository).delete(mockUser);
    }

    @Test
    void deleteUser_UserNotFound() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(context.getAuthentication()).thenReturn(auth);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        int result = userService.deleteUser();
        assertEquals(-1, result);
    }

    @Test
    void updatePassword_Success() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        PasswordUpdateRequest req = new PasswordUpdateRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedpass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("old", "encodedpass")).thenReturn(true);

        int result = userService.updatePassword(req);

        assertEquals(0, result);
        verify(userRepository).save(mockUser);
    }


    @Test
    void updatePassword_UserNotFound() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(context.getAuthentication()).thenReturn(auth);


        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        int result = userService.updatePassword(new PasswordUpdateRequest());
        assertEquals(-1, result);
    }

    @Test
    void updatePassword_WrongOldPassword() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(context.getAuthentication()).thenReturn(auth);


        PasswordUpdateRequest req = new PasswordUpdateRequest();
        req.setOldPassword("wrong");
        req.setNewPassword("new");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "encodedpass")).thenReturn(false);

        int result = userService.updatePassword(req);
        assertEquals(1, result);
    }
}
