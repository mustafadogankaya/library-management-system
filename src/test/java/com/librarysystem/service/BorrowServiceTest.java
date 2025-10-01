package com.librarysystem.service;

import com.librarysystem.model.*;
import com.librarysystem.storage.InMemoryDataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BorrowServiceTest {

    private BorrowService borrowService;
    private UserService userService;
    private Library library;
    private InMemoryDataStorage testStorage;
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testStorage = new InMemoryDataStorage();
        library = new Library(testStorage);
        userService = new UserService(testStorage);
        borrowService = new BorrowService(testStorage, library, userService);

        // Test kullanıcısı oluştur
        testUser = userService.registerUser("Test User", "test@example.com", "555-1234", "Test Address", "password123");
        
        // Test kitabı ekle
        try {
            library.addBook("Test Book", "Test Author", 2023, "978-1234567890");
            testBook = library.findBookByIsbn("978-1234567890").orElseThrow();
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kitap başarıyla ödünç alınmalı")
    void borrowBook_shouldCreateBorrowRecord() {
        // When
        BorrowRecord borrowRecord = borrowService.borrowBook(testUser.getId(), testBook.getId());

        // Then
        assertNotNull(borrowRecord);
        assertEquals(testUser.getId(), borrowRecord.getUserId());
        assertEquals(testBook.getId(), borrowRecord.getBookId());
        assertFalse(borrowRecord.isReturned());
        assertEquals(0.0, borrowRecord.getFineAmount());
        
        // Kitap durumu BORROWED olmalı
        Book updatedBook = library.findBookById(testBook.getId()).orElseThrow();
        assertEquals(BookStatus.BORROWED, updatedBook.getStatus());
    }

    @Test
    @DisplayName("Mevcut olmayan kitap ödünç alınamaz")
    void borrowBook_shouldThrowException_whenBookNotAvailable() {
        // Given
        borrowService.borrowBook(testUser.getId(), testBook.getId()); // Kitabı ödünç al

        // Başka bir kullanıcı oluştur
        User otherUser = userService.registerUser("Other User", "other@example.com", "555-5678", "Other Address", "password456");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowService.borrowBook(otherUser.getId(), testBook.getId());
        });

        assertEquals("Kitap mevcut değil (ödünç verilmiş veya rezerve edilmiş)", exception.getMessage());
    }

    @Test
    @DisplayName("Aynı kullanıcı aynı kitabı tekrar ödünç alamaz")
    void borrowBook_shouldThrowException_whenSameUserTriesToBorrowSameBook() {
        // Given
        borrowService.borrowBook(testUser.getId(), testBook.getId()); // Kitabı ödünç al

        // When & Then - Aynı kullanıcı tekrar denediğinde book status kontrolü önce gelir
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowService.borrowBook(testUser.getId(), testBook.getId());
        });

        // Book status kontrolü önce yapıldığı için bu hata gelir
        assertEquals("Kitap mevcut değil (ödünç verilmiş veya rezerve edilmiş)", exception.getMessage());
    }

    @Test
    @DisplayName("Kitap başarıyla iade edilmeli")
    void returnBook_shouldMarkAsReturned() {
        // Given
        BorrowRecord borrowRecord = borrowService.borrowBook(testUser.getId(), testBook.getId());

        // When
        BorrowRecord returnedRecord = borrowService.returnBook(borrowRecord.getId());

        // Then
        assertTrue(returnedRecord.isReturned());
        assertNotNull(returnedRecord.getReturnDate());
        
        // Kitap durumu AVAILABLE olmalı
        Book updatedBook = library.findBookById(testBook.getId()).orElseThrow();
        assertEquals(BookStatus.AVAILABLE, updatedBook.getStatus());
    }

    @Test
    @DisplayName("Kitap başarıyla rezerve edilmeli")
    void reserveBook_shouldCreateReservation() {
        // Given
        // Önce kitabı ödünç al
        borrowService.borrowBook(testUser.getId(), testBook.getId());
        
        // Başka bir kullanıcı oluştur
        User otherUser = userService.registerUser("Other User", "other@example.com", "555-5678", "Other Address", "password456");

        // When
        Reservation reservation = borrowService.reserveBook(otherUser.getId(), testBook.getId());

        // Then
        assertNotNull(reservation);
        assertEquals(otherUser.getId(), reservation.getUserId());
        assertEquals(testBook.getId(), reservation.getBookId());
        assertTrue(reservation.isActive());
        assertFalse(reservation.isFulfilled());
    }

    @Test
    @DisplayName("Mevcut kitap rezerve edilemez")
    void reserveBook_shouldThrowException_whenBookAvailable() {
        // Given
        // Kitap mevcut durumda

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowService.reserveBook(testUser.getId(), testBook.getId());
        });

        assertEquals("Kitap mevcut, direkt ödünç alabilirsiniz", exception.getMessage());
    }

    @Test
    @DisplayName("Kullanıcının aktif ödünç aldığı kitaplar listelenebilmeli")
    void getUserActiveBorrows_shouldReturnActiveBorrows() {
        // Given
        borrowService.borrowBook(testUser.getId(), testBook.getId());

        // When
        var activeBorrows = borrowService.getUserActiveBorrows(testUser.getId());

        // Then
        assertEquals(1, activeBorrows.size());
        assertEquals(testBook.getId(), activeBorrows.get(0).getBookId());
        assertFalse(activeBorrows.get(0).isReturned());
    }

    @Test
    @DisplayName("İstatistikler doğru değerleri dönmeli")
    void getStats_shouldReturnCorrectValues() {
        // Given
        borrowService.borrowBook(testUser.getId(), testBook.getId());

        // When & Then
        assertEquals(1, borrowService.getTotalBorrowCount());
        assertEquals(1, borrowService.getActiveBorrowCount());
        assertEquals(0, borrowService.getActiveReservationCount());
    }
}