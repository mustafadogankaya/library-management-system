package com.librarysystem.model;

/**
 * Enumeration for user roles in the library management system.
 * Defines different levels of access and permissions.
 */
public enum Role {
    ADMIN("ROLE_ADMIN", "System Administrator"),
    LIBRARIAN("ROLE_LIBRARIAN", "Librarian"),
    USER("ROLE_USER", "Regular User");

    private final String authority;
    private final String description;

    Role(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return authority;
    }
}