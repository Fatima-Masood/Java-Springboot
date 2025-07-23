package com.todolist.listItem;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ListItemRepository extends MongoRepository<ListItem, String> {
    List<ListItem> findByUserId(String userId);
}

