package com.librarysystem.controller;

import com.librarysystem.dto.BookDTO;
import com.librarysystem.exception.BookNotFoundException;
import com.librarysystem.exception.DuplicateIsbnException;
import com.librarysystem.model.Book;
import com.librarysystem.service.Library;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for book management with security and validation.
 * Implements proper input validation and authorization controls.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final Library library;

    @Autowired
    public BookController(Library library) {
        this.library = library;
    }

    // GET /api/books - List all books with secure parameter validation
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) 
            @Size(min = 1, max = 100, message = "Search query must be between 1 and 100 characters") 
            String search,
            
            @RequestParam(required = false) 
            @Pattern(regexp = "^(author|year|status|title|isbn):[^:]+$", 
                     message = "Filter must be in format 'field:value'")
            String filter,
            
            @RequestParam(required = false) 
            @Pattern(regexp = "^(title|author|year|isbn|id)$", 
                     message = "Sort field must be one of: title, author, year, isbn, id")
            String sortBy,
            
            @RequestParam(required = false, defaultValue = "true") 
            boolean ascending) {

        try {
            List<Book> books;
            if (search != null && !search.trim().isEmpty()) {
                books = library.searchBooks(search.trim());
            } else if (filter != null && !filter.trim().isEmpty()) {
                books = library.filterBooks(filter.trim());
            } else if (sortBy != null && !sortBy.trim().isEmpty()) {
                books = library.sortBooks(sortBy.trim(), ascending);
            } else {
                books = library.listAllBooks();
            }
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            // Log security event for potential attacks
            System.err.println("Security: Invalid book query attempt: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/books/{id} - Get book by ID with input validation
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable long id) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid book ID"));
            }
            
            Optional<Book> book = library.findBookById(id);
            return book.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve book"));
        }
    }

    // POST /api/books - Add a new book (requires LIBRARIAN or ADMIN role)
    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@Valid @RequestBody BookDTO bookDTO) {
        try {
            library.addBook(
                bookDTO.getTitle().trim(), 
                bookDTO.getAuthor().trim(), 
                bookDTO.getPublicationYear(), 
                bookDTO.getIsbn().trim()
            );
            
            Optional<Book> addedBookOpt = library.findBookByIsbn(bookDTO.getIsbn().trim());
            if (addedBookOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(addedBookOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Book added but could not be retrieved"));
            }
        } catch (DuplicateIsbnException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse("A book with this ISBN already exists"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Invalid book data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to add book"));
        }
    }

    // PUT /api/books/{id} - Update book by ID (requires LIBRARIAN or ADMIN role)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> updateBookById(@PathVariable long id, @Valid @RequestBody BookDTO updatedBookData) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid book ID"));
            }

            Optional<Book> existingBookOpt = library.findBookById(id);
            if (existingBookOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Book existingBook = existingBookOpt.get();

            // Prevent ISBN changes for security
            if (updatedBookData.getIsbn() != null && 
                !updatedBookData.getIsbn().trim().equals(existingBook.getIsbn())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("ISBN cannot be modified"));
            }

            library.updateBook(
                existingBook.getIsbn(),
                updatedBookData.getTitle() != null ? updatedBookData.getTitle().trim() : null,
                updatedBookData.getAuthor() != null ? updatedBookData.getAuthor().trim() : null,
                updatedBookData.getPublicationYear(),
                updatedBookData.getStatus()
            );

            Optional<Book> updatedBookOpt = library.findBookById(id);
            if (updatedBookOpt.isPresent()) {
                return ResponseEntity.ok(updatedBookOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Book updated but could not be retrieved"));
            }

        } catch (BookNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Invalid update data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update book"));
        }
    }

    // DELETE /api/books/{id} - Delete book by ID (requires ADMIN role)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBookById(@PathVariable long id) {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid book ID"));
            }

            library.deleteBookById(id);
            return ResponseEntity.noContent().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to delete book"));
        }
    }

    // DELETE /api/books/isbn/{isbn} - Delete book by ISBN (requires ADMIN role)
    @DeleteMapping("/isbn/{isbn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBookByIsbn(
            @PathVariable 
            @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
                     message = "Invalid ISBN format") 
            String isbn) {
        try {
            library.deleteBookByIsbn(isbn.trim());
            return ResponseEntity.noContent().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to delete book"));
        }
    }

    /**
     * Helper method to create standardized error responses.
     * Prevents information disclosure while providing useful feedback.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // Global exception handlers for this controller
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(createErrorResponse("Book not found"));
    }

    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateIsbn(DuplicateIsbnException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(createErrorResponse("Book with this ISBN already exists"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse("Invalid request data"));
    }
}
