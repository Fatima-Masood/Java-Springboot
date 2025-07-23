    package com.todolist.listItem;
    
    import lombok.*;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.mapping.Document;
    
    import java.time.LocalDateTime;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Document(collection = "listItems")  // MongoDB collection name
    public class ListItem {
    
        @Id
        private String id;
        private String userId;
        private String title;
        private String description;
        private boolean isCompleted;
        private LocalDateTime createdAt;
    
        public void markCompleted() {
            this.isCompleted = true;
        }
    
        public void markIncomplete() {
            this.isCompleted = false;
        }
    }
