package com.todolist.user;

import com.todolist.listItem.ListItem;
import com.todolist.listItem.ListItemRepository;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ListItemRepository listItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository,
                          ListItemRepository listItemRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.listItemRepository = listItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PermitAll
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{email}")
    @PreAuthorize("#email == authentication.name")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userRepository.findById(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{email}/todos")
    @PreAuthorize("#email == authentication.name")
    public ResponseEntity<ListItem> addTodoForUser(@PathVariable String email, @RequestBody ListItem item) {
        if (userRepository.findById(email).isEmpty()) return ResponseEntity.notFound().build();

        item.setUserId(email);
        item.setCreatedAt(LocalDateTime.now());
        item.setCompleted(false);

        ListItem savedItem = listItemRepository.save(item);
        return ResponseEntity.ok(savedItem);
    }

    @GetMapping("/{email}/todos")
    @PreAuthorize("#email == authentication.name")
    public ResponseEntity<List<ListItem>> getTodosForUser(@PathVariable String email) {
        if (userRepository.findById(email).isEmpty()) return ResponseEntity.notFound().build();

        List<ListItem> items = listItemRepository.findByUserId(email);
        return ResponseEntity.ok(items);
    }
}
