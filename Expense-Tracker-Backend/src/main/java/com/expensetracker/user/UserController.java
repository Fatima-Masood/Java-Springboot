package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.expenditure.ExpenditureRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  ExpenditureRepository expenditureRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO,
                                      HttpServletResponse response) {

        if (userDTO.getUsername() == null || userDTO.getPassword() == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incomplete credentials");

        String tokenResponse = userService.register(userDTO.getUsername(), userDTO.getPassword(), response, authenticationManager, jwtEncoder);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginRequest,
                                   HttpServletResponse response) {
        try {
            if (loginRequest.getUsername() != null && loginRequest.getPassword() != null) {
                User user = new User ();
                user.setUsername(loginRequest.getUsername());
                user.setPassword(loginRequest.getPassword());
                String tokenResponse = userService.loginUser(
                        user,
                        response,
                        authenticationManager,
                        jwtEncoder
                );
                return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incomplete credentials");
            }
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
    public ResponseEntity<String> getUser(HttpServletRequest request, Authentication authentication) {
        if (authentication != null) {
            return ResponseEntity.ok(authentication.getName());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First");
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
        userService.deleteCookie(response, "access_token");
        userService.deleteCookie(response, "token");
        userService.deleteCookie(response, "XSRF-TOKEN");
        userService.deleteCookie(response, "csrfToken");
        userService.deleteCookie(response, "csrf_token");
        userService.deleteCookie(response, "next-auth.session-token");
        return ResponseEntity.ok("{\"message\":\"Logged out successfully\"}");
    }

}
