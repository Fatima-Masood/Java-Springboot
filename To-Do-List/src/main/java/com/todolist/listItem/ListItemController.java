package com.todolist.listItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class ListItemController {

    private final ListItemRepository repository;

    @Autowired
    public ListItemController(ListItemRepository repository) {
        this.repository = repository;
    }

    // Create new item
    @PostMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<ListItem> createItem(@PathVariable String userId, @RequestBody ListItem item) {
        item.setUserId(userId);
        item.setCreatedAt(LocalDateTime.now());
        item.setCompleted(false);

        ListItem savedItem = repository.save(item);
        return ResponseEntity.ok(savedItem);
    }

    // Get all items
    @GetMapping
    public ResponseEntity<List<ListItem>> getAllItems() {
        return ResponseEntity.ok(repository.findAll());
    }

    // Get all items by userId (optional MongoDB-specific improvement)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ListItem>> getItemsByUser(@PathVariable String userId) {
        List<ListItem> items = repository.findByUserId(userId);
        return ResponseEntity.ok(items);
    }

    // Get item by ID
    @GetMapping("/{id}")
    public ResponseEntity<ListItem> getItemById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update item by ID
    @PutMapping("/{id}")
    public ResponseEntity<ListItem> updateItem(@PathVariable String id, @RequestBody ListItem updatedItem) {
        return repository.findById(id).map(item -> {
            item.setTitle(updatedItem.getTitle());
            item.setDescription(updatedItem.getDescription());
            item.setUserId(updatedItem.getUserId());
            item.setCompleted(updatedItem.isCompleted());

            ListItem saved = repository.save(item);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete item by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Mark item as done
    @PostMapping("/{id}/markdone")
    public ResponseEntity<ListItem> markAsDone(@PathVariable String id) {
        return repository.findById(id).map(item -> {
            item.markCompleted();
            ListItem saved = repository.save(item);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Mark item as undone
    @PostMapping("/{id}/markundone")
    public ResponseEntity<ListItem> markAsUndone(@PathVariable String id) {
        return repository.findById(id).map(item -> {
            item.markIncomplete();
            ListItem saved = repository.save(item);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
