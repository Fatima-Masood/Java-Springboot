package com.expensetracker.user;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserService userService;

    private final User sampleUser = new User("1", "testuser", "encodedpass", "USER");

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticate_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("rawpass", "encodedpass")).thenReturn(true);

        User result = userService.authenticate("testuser", "rawpass");
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpass", "encodedpass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            userService.authenticate("testuser", "wrongpass");
        });
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        UserDetails details = userService.loadUserByUsername("testuser");
        assertEquals("testuser", details.getUsername());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("ghost");
        });
    }

    @Test
    void testOAuthSignUp_NewUser() {
        when(userRepository.existsByUsername("oauthuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));

        User newUser = userService.OAuthSignUp("oauthuser", authenticationManager);
        assertEquals("oauthuser", newUser.getUsername());
    }

    @Test
    void testRegister_NewUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("securepass")).thenReturn("hashed");
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token123");

        when(jwtEncoder.encode(any())).thenReturn(jwt);

        String json = userService.register("newuser", "securepass", response, authenticationManager, jwtEncoder);
        assertTrue(json.contains("access_token"));
    }

    @Test
    void testLoginUser_ValidCredentials() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("abc123");
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        String token = userService.loginUser(sampleUser, response, authenticationManager, jwtEncoder);
        assertTrue(token.contains("access_token"));
    }

    @Test
    void testSetJwtAndResponse_Valid() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("tok123");

        String json = userService.setJwtAndResponse(sampleUser, jwtEncoder, response, jwt);
        assertEquals("{\"access_token\":\"tok123\", \"expires_in\":3600}", json);
    }

    @Test
    void testSetJwt_CreatesToken() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("jwtToken");
        when(encoder.encode(any())).thenReturn(mockJwt);

        Jwt jwt = userService.setJwt(sampleUser, encoder);
        assertEquals("jwtToken", jwt.getTokenValue());
    }
}
