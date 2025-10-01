package com.librarysystem.service;

import com.librarysystem.model.User;
import com.librarysystem.model.Role;
import com.librarysystem.storage.InMemoryDataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private InMemoryDataStorage testStorage;

    @BeforeEach
    void setUp() {
        testStorage = new InMemoryDataStorage();
        userService = new UserService(testStorage);
    }

    @Test
    @DisplayName("Yeni kullanıcı başarıyla kaydedilmeli")
    void registerUser_shouldCreateUserSuccessfully() {
        // Given
        String name = "Ali Veli";
        String email = "ali@example.com";
        String phone = "555-1234";
        String address = "Istanbul";
        String password = "password123";

        // When
        User user = userService.registerUser(name, email, phone, address, password);

        // Then
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email.toLowerCase(), user.getEmail());
        assertEquals(phone, user.getPhone());
        assertEquals(address, user.getAddress());
        assertEquals(Role.MEMBER, user.getRole());
        assertTrue(user.isActive());
        assertEquals(1, userService.getUserCount());
    }

    @Test
    @DisplayName("Aynı email ile kullanıcı kaydı yapmaya çalışıldığında hata fırlatmalı")
    void registerUser_shouldThrowException_whenEmailExists() {
        // Given
        userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "password123");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("Veli Ali", "ali@example.com", "555-5678", "Ankara", "password456");
        });

        assertEquals("Bu e-posta adresi zaten kullanılmaktadır", exception.getMessage());
        assertEquals(1, userService.getUserCount());
    }

    @Test
    @DisplayName("Kullanıcı doğrulaması başarılı olmalı")
    void authenticate_shouldReturnUser_whenCredentialsAreValid() {
        // Given
        User registeredUser = userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "password123");

        // When
        Optional<User> authenticatedUser = userService.authenticate("ali@example.com", "password123");

        // Then
        assertTrue(authenticatedUser.isPresent());
        assertEquals(registeredUser.getId(), authenticatedUser.get().getId());
        assertEquals(registeredUser.getEmail(), authenticatedUser.get().getEmail());
    }

    @Test
    @DisplayName("Yanlış şifre ile doğrulama başarısız olmalı")
    void authenticate_shouldReturnEmpty_whenPasswordIsWrong() {
        // Given
        userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "password123");

        // When
        Optional<User> authenticatedUser = userService.authenticate("ali@example.com", "wrongpassword");

        // Then
        assertTrue(authenticatedUser.isEmpty());
    }

    @Test
    @DisplayName("Kullanıcı bilgileri başarıyla güncellenmeli")
    void updateUser_shouldUpdateUserDetails() {
        // Given
        User user = userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "password123");
        String newName = "Ali Yeni";
        String newPhone = "555-9999";
        String newAddress = "Ankara";

        // When
        User updatedUser = userService.updateUser(user.getId(), newName, newPhone, newAddress);

        // Then
        assertEquals(newName, updatedUser.getName());
        assertEquals(newPhone, updatedUser.getPhone());
        assertEquals(newAddress, updatedUser.getAddress());
        assertEquals(user.getEmail(), updatedUser.getEmail()); // Email değişmemeli
    }

    @Test
    @DisplayName("Şifre başarıyla değiştirilmeli")
    void changePassword_shouldUpdatePassword() {
        // Given
        User user = userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "oldpassword");

        // When
        userService.changePassword(user.getId(), "oldpassword", "newpassword");

        // Then
        Optional<User> authenticatedUser = userService.authenticate("ali@example.com", "newpassword");
        assertTrue(authenticatedUser.isPresent());

        // Eski şifre ile giriş başarısız olmalı
        Optional<User> oldPasswordAuth = userService.authenticate("ali@example.com", "oldpassword");
        assertTrue(oldPasswordAuth.isEmpty());
    }

    @Test
    @DisplayName("Kullanıcı rolü başarıyla değiştirilmeli")
    void changeRole_shouldUpdateUserRole() {
        // Given
        User user = userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "password123");
        assertEquals(Role.MEMBER, user.getRole());

        // When
        userService.changeRole(user.getId(), Role.LIBRARIAN);

        // Then
        Optional<User> updatedUser = userService.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals(Role.LIBRARIAN, updatedUser.get().getRole());
    }

    @Test
    @DisplayName("İsim araması doğru sonuçları dönmeli")
    void searchByName_shouldReturnMatchingUsers() {
        // Given
        userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "password123");
        userService.registerUser("Mehmet Ali", "mehmet@example.com", "555-5678", "Ankara", "password456");
        userService.registerUser("Veli Mehmet", "veli@example.com", "555-9999", "Izmir", "password789");

        // When
        var aliResults = userService.searchByName("Ali");
        var mehmetResults = userService.searchByName("Mehmet");

        // Then
        assertEquals(2, aliResults.size()); // Ali Veli ve Mehmet Ali
        assertEquals(2, mehmetResults.size()); // Mehmet Ali ve Veli Mehmet
    }

    @Test
    @DisplayName("Geçersiz email ile kullanıcı kaydı hata fırlatmalı")
    void registerUser_shouldThrowException_whenEmailInvalid() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("Ali Veli", "invalid-email", "555-1234", "Istanbul", "password123");
        });

        assertEquals("Geçerli bir e-posta adresi giriniz", exception.getMessage());
    }

    @Test
    @DisplayName("Çok kısa şifre ile kullanıcı kaydı hata fırlatmalı")
    void registerUser_shouldThrowException_whenPasswordTooShort() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("Ali Veli", "ali@example.com", "555-1234", "Istanbul", "123");
        });

        assertEquals("Şifre en az 6 karakter olmalıdır", exception.getMessage());
    }
}