package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO,
                                      HttpServletResponse response) {

        if (userDTO.getUsername() == null || userDTO.getPassword() == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incomplete credentials");

        String tokenResponse = userService.register(userDTO.getUsername(), userDTO.getPassword(), authenticationManager);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        int response = userService.deleteUser();
        if (response == -1)
            return ResponseEntity.notFound().build();
        else
            return ResponseEntity.ok("User and related expenditures deleted");
    }

    @GetMapping
    public ResponseEntity<String> getUser(HttpServletRequest request, Authentication authentication) {
        if (authentication != null)
            return ResponseEntity.ok(authentication.getName());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First");
    }

    @PutMapping
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        int result = userService.updatePassword(request);

        return switch (result) {
            case -1 -> ResponseEntity.notFound().build();
            case 1 -> ResponseEntity.badRequest().body("Incorrect old password");
            default -> ResponseEntity.ok("Password updated");
        };
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        userService.deleteCookie(response, "access_token");
        userService.deleteCookie(response, "token");
        userService.deleteCookie(response, "XSRF-TOKEN");
        userService.deleteCookie(response, "csrf_token");
        userService.deleteCookie(response, "next-auth.session-token");
        return ResponseEntity.ok("{\"message\":\"Logged out successfully\"}");
    }
}