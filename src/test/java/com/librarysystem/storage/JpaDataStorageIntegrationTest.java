package com.librarysystem.storage;

import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import com.librarysystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.librarysystem.app.LibraryManagementWebApplication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JPA data storage functionality.
 * Uses @DataJpaTest for isolated database testing.
 */
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = LibraryManagementWebApplication.class)
class JpaDataStorageIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private JpaDataStorage jpaDataStorage;

    @BeforeEach
    void setUp() {
        jpaDataStorage = new JpaDataStorage(bookRepository);
    }

    @Test
    @DisplayName("Should save and load books using JPA")
    void saveAndLoadBooks_shouldWorkCorrectly() {
        // Given
        Book book1 = new Book("Test Book 1", "Test Author 1", 2020, "TEST001");
        Book book2 = new Book("Test Book 2", "Test Author 2", 2021, "TEST002");
        List<Book> booksToSave = List.of(book1, book2);

        // When
        jpaDataStorage.saveBooks(booksToSave);
        List<Book> loadedBooks = jpaDataStorage.loadBooks();

        // Then
        assertEquals(2, loadedBooks.size());
        
        // Find books by ISBN to verify they were saved correctly
        Optional<Book> savedBook1 = loadedBooks.stream()
                .filter(book -> "TEST001".equals(book.getIsbn()))
                .findFirst();
        Optional<Book> savedBook2 = loadedBooks.stream()
                .filter(book -> "TEST002".equals(book.getIsbn()))
                .findFirst();

        assertTrue(savedBook1.isPresent());
        assertTrue(savedBook2.isPresent());

        assertEquals("Test Book 1", savedBook1.get().getTitle());
        assertEquals("Test Author 1", savedBook1.get().getAuthor());
        assertEquals(2020, savedBook1.get().getPublicationYear());
        assertEquals(BookStatus.AVAILABLE, savedBook1.get().getStatus());

        assertEquals("Test Book 2", savedBook2.get().getTitle());
        assertEquals("Test Author 2", savedBook2.get().getAuthor());
        assertEquals(2021, savedBook2.get().getPublicationYear());
        assertEquals(BookStatus.AVAILABLE, savedBook2.get().getStatus());
    }

    @Test
    @DisplayName("Should save and manage books incrementally")
    void saveBooks_shouldWorkIncrementally() {
        // Given - Save initial books
        Book initialBook = new Book("Initial Book", "Initial Author", 2019, "INIT001");
        jpaDataStorage.saveBooks(List.of(initialBook));

        // When - Save additional books (should add, not replace)
        Book newBook = new Book("New Book", "New Author", 2022, "NEW001");
        jpaDataStorage.saveBooks(List.of(newBook));

        // Then - Both books should exist
        List<Book> loadedBooks = jpaDataStorage.loadBooks();
        assertEquals(2, loadedBooks.size());
        
        // Verify both books exist
        boolean hasInitialBook = loadedBooks.stream()
                .anyMatch(book -> "INIT001".equals(book.getIsbn()));
        boolean hasNewBook = loadedBooks.stream()
                .anyMatch(book -> "NEW001".equals(book.getIsbn()));
        
        assertTrue(hasInitialBook);
        assertTrue(hasNewBook);
    }

    @Test
    @DisplayName("BookRepository should find book by ISBN")
    void bookRepository_shouldFindByIsbn() {
        // Given
        Book book = new Book();
        book.setTitle("Repository Test Book");
        book.setAuthor("Repository Author");
        book.setPublicationYear(2023);
        book.setIsbn("REPO001");
        book.setStatus(BookStatus.AVAILABLE);
        entityManager.persistAndFlush(book);

        // When
        Optional<Book> foundBook = bookRepository.findByIsbn("REPO001");

        // Then
        assertTrue(foundBook.isPresent());
        assertEquals("Repository Test Book", foundBook.get().getTitle());
        assertEquals("Repository Author", foundBook.get().getAuthor());
    }

    @Test
    @DisplayName("BookRepository should check if book exists by ISBN")
    void bookRepository_shouldCheckExistsByIsbn() {
        // Given
        Book book = new Book();
        book.setTitle("Exists Test Book");
        book.setAuthor("Exists Author");
        book.setPublicationYear(2024);
        book.setIsbn("EXISTS001");
        book.setStatus(BookStatus.AVAILABLE);
        entityManager.persistAndFlush(book);

        // When & Then
        assertTrue(bookRepository.existsByIsbn("EXISTS001"));
        assertFalse(bookRepository.existsByIsbn("NONEXISTENT"));
    }

    @Test
    @DisplayName("BookRepository should find books by status")
    void bookRepository_shouldFindByStatus() {
        // Given
        Book availableBook = new Book();
        availableBook.setTitle("Available Book");
        availableBook.setAuthor("Available Author");
        availableBook.setPublicationYear(2020);
        availableBook.setIsbn("AVAIL001");
        availableBook.setStatus(BookStatus.AVAILABLE);
        
        Book borrowedBook = new Book();
        borrowedBook.setTitle("Borrowed Book");
        borrowedBook.setAuthor("Borrowed Author");
        borrowedBook.setPublicationYear(2021);
        borrowedBook.setIsbn("BORR001");
        borrowedBook.setStatus(BookStatus.BORROWED);
        
        entityManager.persistAndFlush(availableBook);
        entityManager.persistAndFlush(borrowedBook);

        // When
        List<Book> availableBooks = bookRepository.findByStatus(BookStatus.AVAILABLE);
        List<Book> borrowedBooks = bookRepository.findByStatus(BookStatus.BORROWED);

        // Then
        assertEquals(1, availableBooks.size());
        assertEquals("Available Book", availableBooks.get(0).getTitle());
        
        assertEquals(1, borrowedBooks.size());
        assertEquals("Borrowed Book", borrowedBooks.get(0).getTitle());
    }
}