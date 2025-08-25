package com.expensetracker.user;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testEquals_SameObject() {
        User user = User.builder()
                .id("1")
                .username("john")
                .password("123")
                .role("USER")
                .build();
        assertEquals(user, user);
    }

    @Test
    void testEquals_EqualObjects() {
        User user1 = new User("1", "john", "123", "USER");
        User user2 = new User("1", "john", "123", "USER");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testEquals_NotEqualObjects() {
        User user1 = new User("1", "john", "123", "USER");
        User user2 = new User("2", "jane", "456", "ADMIN");

        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_NullAndDifferentClass() {
        User user = new User("1", "john", "123", "USER");

        assertNotEquals(null, user);
        assertNotEquals("some string", user);
    }

    @Test
    void testEquals_NullFields() {
        User user1 = new User(null, null, null, null);
        User user2 = new User(null, null, null, null);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testHashSetContainsUser() {
        User user1 = new User("1", "john", "123", "USER");
        HashSet<User> set = new HashSet<>();
        set.add(user1);

        assertTrue(set.contains(new User("1", "john", "123", "USER")));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        User user = new User();
        user.setId("10");
        user.setUsername("alice");
        user.setPassword("pwd");
        user.setRole("ADMIN");

        assertEquals("10", user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("pwd", user.getPassword());
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void testToString_NotNull() {
        User user = new User("1", "john", "123", "USER");
        String output = user.toString();

        assertNotNull(output);
        assertTrue(output.contains("john"));
        assertTrue(output.contains("USER"));
    }

    @Test
    void testBuilderPattern() {
        User user = User.builder()
                .id("101")
                .username("bob")
                .password("pass")
                .role("MODERATOR")
                .build();

        assertEquals("101", user.getId());
        assertEquals("bob", user.getUsername());
        assertEquals("pass", user.getPassword());
        assertEquals("MODERATOR", user.getRole());
    }
}
