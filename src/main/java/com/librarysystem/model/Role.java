package com.librarysystem.model;

/**
 * Kullanıcı rollerini belirtir.
 */
public enum Role {
    MEMBER("Üye"),
    LIBRARIAN("Kütüphaneci"),
    ADMIN("Yönetici");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
