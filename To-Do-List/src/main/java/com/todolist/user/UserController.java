package com.todolist.user;

import com.todolist.listItem.ListItem;
import com.todolist.listItem.ListItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ListItemRepository listItemRepository;

    @Autowired
    public UserController(UserRepository userRepository, ListItemRepository listItemRepository) {
        this.userRepository = userRepository;
        this.listItemRepository = listItemRepository;
    }

    // Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // In real apps, hash password before saving
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Get user by email (id)
    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findById(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a new todo item for user
    @PostMapping("/{email}/todos")
    public ResponseEntity<ListItem> addTodoForUser(@PathVariable String email, @RequestBody ListItem item) {
        Optional<User> userOpt = userRepository.findById(email);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        // Set userId to user's email
        item.setUserId(email);
        item.setCreatedAt(LocalDateTime.now());
        item.setCompleted(false);

        ListItem savedItem = listItemRepository.save(item);
        return ResponseEntity.ok(savedItem);
    }

    // Get all todo items for user
    @GetMapping("/{email}/todos")
    public ResponseEntity<List<ListItem>> getTodosForUser(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findById(email);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<ListItem> items = listItemRepository.findByUserId(email);
        return ResponseEntity.ok(items);
    }
}
