package com.expensetracker.dto;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setPassword("secure");

        assertEquals("testuser", dto.getUsername());
        assertEquals("secure", dto.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        UserDTO dto = new UserDTO("alice", "password123");

        assertEquals("alice", dto.getUsername());
        assertEquals("password123", dto.getPassword());
    }
}
