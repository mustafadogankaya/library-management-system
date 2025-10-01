package com.librarysystem.repository;

import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book entity using Spring Data JPA.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find book by ISBN (unique identifier).
     * @param isbn The ISBN to search for
     * @return Optional containing the book if found
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Check if a book exists by ISBN.
     * @param isbn The ISBN to check
     * @return true if book exists, false otherwise
     */
    boolean existsByIsbn(String isbn);

    /**
     * Find books by status.
     * @param status The book status to filter by
     * @return List of books with the given status
     */
    List<Book> findByStatus(BookStatus status);

    /**
     * Find books by title containing the given text (case insensitive).
     * @param title The title text to search for
     * @return List of books matching the title criteria
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Find books by author containing the given text (case insensitive).
     * @param author The author text to search for
     * @return List of books matching the author criteria
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Find books by publication year.
     * @param year The publication year to search for
     * @return List of books published in the given year
     */
    List<Book> findByPublicationYear(int year);

    /**
     * Find books by multiple criteria using a custom query.
     * @param title Title to search for (can be partial)
     * @param author Author to search for (can be partial)
     * @param year Publication year (optional)
     * @param status Book status (optional)
     * @return List of books matching the criteria
     */
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:year IS NULL OR b.publicationYear = :year) AND " +
           "(:status IS NULL OR b.status = :status)")
    List<Book> findBooksWithCriteria(@Param("title") String title,
                                     @Param("author") String author,
                                     @Param("year") Integer year,
                                     @Param("status") BookStatus status);
}