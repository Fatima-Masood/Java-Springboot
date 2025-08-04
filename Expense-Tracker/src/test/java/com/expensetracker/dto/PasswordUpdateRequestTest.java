package com.expensetracker.dto;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUpdateRequestTest {

    @Test
    void testEquals_SameObject() {
        PasswordUpdateRequest req = new PasswordUpdateRequest("old123", "new123");
        assertEquals(req, req); // Reflexive
    }

    @Test
    void testEquals_EqualObjects() {
        PasswordUpdateRequest r1 = new PasswordUpdateRequest("old", "new");
        PasswordUpdateRequest r2 = new PasswordUpdateRequest("old", "new");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_NotEqualObjects() {
        PasswordUpdateRequest r1 = new PasswordUpdateRequest("old", "new");
        PasswordUpdateRequest r2 = new PasswordUpdateRequest("different", "newer");

        assertNotEquals(r1, r2);
    }

    @Test
    void testEquals_WithNullFields() {
        PasswordUpdateRequest r1 = new PasswordUpdateRequest(null, null);
        PasswordUpdateRequest r2 = new PasswordUpdateRequest(null, null);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testHashSetContains() {
        PasswordUpdateRequest req1 = new PasswordUpdateRequest("old", "new");
        HashSet<PasswordUpdateRequest> set = new HashSet<>();
        set.add(req1);

        assertTrue(set.contains(new PasswordUpdateRequest("old", "new")));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        PasswordUpdateRequest req = new PasswordUpdateRequest();
        req.setOldPassword("oldPass");
        req.setNewPassword("newPass");

        assertEquals("oldPass", req.getOldPassword());
        assertEquals("newPass", req.getNewPassword());
    }

    @Test
    void testToString_NotNull() {
        PasswordUpdateRequest req = new PasswordUpdateRequest("old", "new");
        assertNotNull(req.toString());
        assertTrue(req.toString().contains("old"));
    }

    @Test
    void testEquals_NullAndDifferentType() {
        PasswordUpdateRequest req = new PasswordUpdateRequest("a", "b");
        assertNotEquals(req, null);
        assertNotEquals(req, "string");
    }
}
