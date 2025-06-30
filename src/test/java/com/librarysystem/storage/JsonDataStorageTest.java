package com.librarysystem.storage;

import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonDataStorageTest {

    private JsonDataStorage storage;
    private String testFilePath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        testFilePath = tempDir.resolve("test-books.json").toString();
        storage = new JsonDataStorage(testFilePath);
    }

    @Test
    @DisplayName("Should save and load books correctly")
    void saveAndLoadBooks_shouldWorkCorrectly() {
        // Create test books
        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("Test Book 1");
        book1.setAuthor("Test Author 1");
        book1.setPublicationYear(2021);
        book1.setIsbn("TEST-001");
        book1.setStatus(BookStatus.AVAILABLE);

        Book book2 = new Book();
        book2.setId(2);
        book2.setTitle("Test Book 2");
        book2.setAuthor("Test Author 2");
        book2.setPublicationYear(2022);
        book2.setIsbn("TEST-002");
        book2.setStatus(BookStatus.BORROWED);

        List<Book> originalBooks = Arrays.asList(book1, book2);

        // Save books
        storage.saveBooks(originalBooks);

        // Load books
        List<Book> loadedBooks = storage.loadBooks();

        // Verify
        assertNotNull(loadedBooks);
        assertEquals(2, loadedBooks.size());
        
        assertEquals("Test Book 1", loadedBooks.get(0).getTitle());
        assertEquals("Test Author 1", loadedBooks.get(0).getAuthor());
        assertEquals("TEST-001", loadedBooks.get(0).getIsbn());
        assertEquals(BookStatus.AVAILABLE, loadedBooks.get(0).getStatus());

        assertEquals("Test Book 2", loadedBooks.get(1).getTitle());
        assertEquals("Test Author 2", loadedBooks.get(1).getAuthor());
        assertEquals("TEST-002", loadedBooks.get(1).getIsbn());
        assertEquals(BookStatus.BORROWED, loadedBooks.get(1).getStatus());
    }

    @Test
    @DisplayName("Should return empty list when file doesn't exist initially")
    void loadBooks_fromNonExistentFile_shouldReturnEmptyList() {
        List<Book> books = storage.loadBooks();
        
        assertNotNull(books);
        assertTrue(books.isEmpty());
    }

    @Test
    @DisplayName("Should handle saving empty list")
    void saveBooks_emptyList_shouldWorkCorrectly() {
        storage.saveBooks(Arrays.asList());
        
        List<Book> loadedBooks = storage.loadBooks();
        
        assertNotNull(loadedBooks);
        assertTrue(loadedBooks.isEmpty());
    }

    @Test
    @DisplayName("Should create file if it doesn't exist")
    void saveBooks_shouldCreateFileIfNotExists() {
        Book book = new Book();
        book.setTitle("Test");
        book.setAuthor("Author");
        book.setPublicationYear(2023);
        book.setIsbn("TEST-123");
        book.setStatus(BookStatus.AVAILABLE);

        storage.saveBooks(Arrays.asList(book));

        File file = new File(testFilePath);
        assertTrue(file.exists());
    }
}