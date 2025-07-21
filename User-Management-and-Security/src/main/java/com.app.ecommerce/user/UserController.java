package com.app.ecommerce.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private static final Logger logData = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/v1/user/{id}")
    public ResponseEntity<User> get(@PathVariable("id") Long id) {
        Optional<User> news = userRepository.findById(id);
        if (news.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(news.get());
    }

    @GetMapping("/api/v1/user")
    public ResponseEntity<Page<User>> get(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "roles", required = false) String roles,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<User> user;
        Pageable pageable = PageRequest.of(page, size);
        if (name != null && !name.isEmpty()) {
            user = userRepository.findByNameLike("%" + name + "%", pageable);
        } else if(roles != null && !roles.isEmpty()){
            user = userRepository.findByRolesLike("%" + roles + "%", pageable);
        }else {
            logData.info("Returning all news!");
            user = userRepository.findAll(pageable);
        }

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }


    @PostMapping("/api/v1/user")
    @ResponseStatus (HttpStatus.CREATED)
    public ResponseEntity<User> post(@RequestBody User request) {
        User user = new User(
                request.getId(),
                request.getName(),
                request.getPassword(),
                request.getRoles(),
                LocalDateTime.now()
        );

        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/api/v1/user/{id}")
    @Transactional
    public ResponseEntity<User> update(@PathVariable("id") Long id, @RequestBody User updatedUser) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existing.get();
        user.setName(updatedUser.getName());
        user.setPassword(updatedUser.getPassword());
        user.setRoles(updatedUser.getRoles());
        user.setLastUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

}
