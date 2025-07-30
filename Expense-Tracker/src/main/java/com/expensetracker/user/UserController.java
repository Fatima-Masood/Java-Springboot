package com.expensetracker.user;

import com.expensetracker.config.SecurityConfig;
import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.expenses.ExpenditureRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:8000")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ExpenditureRepository expenditureRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    @Autowired
    private JwtEncoder jwtEncoder;


    @PostMapping("/register")
    @ResponseStatus (HttpStatus.CREATED)
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO,
                                      HttpServletResponse response) {

        if (userDTO.getUsername() == null || userDTO.getPassword() == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incomplete credentials");

        try {
            User user = userService.register(userDTO.getUsername(), userDTO.getPassword());
            return login(user, response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not logged in");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user,
                                   HttpServletResponse response) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incomplete credentials");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(auth);

            int expirySeconds = 3600;

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .subject(user.getUsername())
                    .expiresAt(Instant.now().plusSeconds(expirySeconds))
                    .build();

            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));

            Cookie cookie = new Cookie("access_token", jwt.getTokenValue());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(expirySeconds);
            response.addCookie(cookie);

            String json = String.format("{\"access_token\":\"%s\", \"expires_in\":%d}", jwt.getTokenValue(), expirySeconds);
            return ResponseEntity.ok().body(json);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        expenditureRepository.deleteByUser(username);
        userRepository.delete(user);

        return ResponseEntity.ok("User and related expenditures deleted");
    }

    @GetMapping
    public ResponseEntity<User> getUser(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest,
                                                 HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        if (!passwordEncoder.matches(passwordUpdateRequest.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(passwordUpdateRequest.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return ResponseEntity.ok().body("{\"message\":\"Logged out successfully\"}");
    }

}
