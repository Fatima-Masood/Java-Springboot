package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.dto.UserDTO;
import com.expensetracker.expenses.ExpenditureRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ExpenditureRepository expenditureRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    // REGISTER
    @PostMapping("/register")
    @ResponseStatus (HttpStatus.CREATED)
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        userService.register(userDTO.getUsername(), userDTO.getPassword());
        return ResponseEntity.ok("User registered successfully");
    }

    // LOGIN
    @PostMapping("/form-login")
    public ResponseEntity<String> formLogin(@RequestBody User user) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            return ResponseEntity.ok("Login successful");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
    @PostMapping("/auth-login")
    public ResponseEntity<String> authLogin(@RequestBody User user) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            return ResponseEntity.ok("Login successful");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    // DELETE + cascade delete expenditures
    @DeleteMapping("/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        expenditureRepository.deleteByUser(username);
        userRepository.delete(user);

        return ResponseEntity.ok("User and related expenditures deleted");
    }

    // UPDATE PASSWORD
    @PutMapping("/{username}/password")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<String> updatePassword(@PathVariable String username,
                                                 @RequestBody PasswordUpdateRequest request) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        if (!passwordEncoder.matches(request.oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated");
    }
}
