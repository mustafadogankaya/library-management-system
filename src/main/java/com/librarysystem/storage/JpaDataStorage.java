package com.librarysystem.storage;

import com.librarysystem.model.Book;
import com.librarysystem.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JPA-based implementation of DataStorage interface.
 * This implementation uses Spring Data JPA to persist books to a database.
 * It's activated by default unless explicitly disabled.
 */
@Repository
@Profile("!json-storage") // Use this unless 'json-storage' profile is active
public class JpaDataStorage implements DataStorage {

    private final BookRepository bookRepository;

    @Autowired
    public JpaDataStorage(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        System.out.println("JpaDataStorage initialized with database persistence.");
    }

    @Override
    @Transactional
    public void saveBooks(List<Book> books) {
        try {
            // For each book, either save or update as needed
            for (Book book : books) {
                if (book.getId() > 0) {
                    // Book has an ID, check if it exists in database
                    if (bookRepository.existsById(book.getId())) {
                        // Update existing book
                        bookRepository.save(book);
                    } else {
                        // Book has ID but doesn't exist in DB - this can happen during initial load
                        // Let JPA assign a new ID
                        book.setId(0); // Reset ID to let JPA generate a new one
                        bookRepository.save(book);
                    }
                } else {
                    // New book without ID - let JPA generate ID
                    bookRepository.save(book);
                }
            }
            System.out.println("Successfully saved " + books.size() + " books to database.");
        } catch (Exception e) {
            System.err.println("Error saving books to database: " + e.getMessage());
            throw new RuntimeException("Failed to save books to database", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> loadBooks() {
        try {
            List<Book> books = bookRepository.findAll();
            System.out.println("Successfully loaded " + books.size() + " books from database.");
            return books;
        } catch (Exception e) {
            System.err.println("Error loading books from database: " + e.getMessage());
            // Return empty list on error to maintain compatibility
            return List.of();
        }
    }
}