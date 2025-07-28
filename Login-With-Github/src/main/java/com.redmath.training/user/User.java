package com.redmath.training.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity (name = "users")
public class User {
    @Id
    private int id;

    private String name;
    private String password;
    private String roles;
    private LocalDateTime lastUpdatedAt;

    public User(int id, String name, String password, String roles, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.roles = roles;
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
