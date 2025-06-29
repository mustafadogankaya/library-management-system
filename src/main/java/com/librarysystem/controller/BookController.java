package com.librarysystem.controller;

import com.librarysystem.exception.BookNotFoundException;
import com.librarysystem.exception.DuplicateIsbnException;
import com.librarysystem.model.Book;
import com.librarysystem.service.Library;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books") // Base path for all methods in this controller
public class BookController {

    private final Library library;

    @Autowired
    public BookController(Library library) {
        this.library = library;
    }

    // GET /api/books - List all books or search/filter/sort
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter, // e.g., "author:Author Name", "status:AVAILABLE"
            @RequestParam(required = false) String sortBy, // Removed default value
            @RequestParam(required = false, defaultValue = "true") boolean ascending) {

        List<Book> books;
        if (search != null && !search.isEmpty()) {
            books = library.searchBooks(search);
            // Simple approach: Sorting/filtering after search is not directly combined here
            // If needed, apply sorting/filtering to the search results manually
        } else if (filter != null && !filter.isEmpty()) {
             // Pass the raw filter string as expected by Library.filterBooks
            books = library.filterBooks(filter);
             // Simple approach: Sorting after filtering is not directly combined here
        } else if (sortBy != null && !sortBy.isEmpty()) {
            // Call sortBooks if sortBy is provided (and no search/filter)
            books = library.sortBooks(sortBy, ascending);        }
        else {
            // Default: list all books if no search, filter, or sort criteria provided
            books = library.listAllBooks();
        }
        return ResponseEntity.ok(books);
    }

    // GET /api/books/{id} - Get book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable long id) {
        Optional<Book> book = library.findBookById(id);
        return book.map(ResponseEntity::ok) // If found, return 200 OK with book
                   .orElseGet(() -> ResponseEntity.notFound().build()); // If not found, return 404 Not Found
    }
    // POST /api/books - Add a new book
    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) { // Use RequestBody to get Book from JSON
        try {
            // Call library.addBook with individual fields
            library.addBook(book.getTitle(), book.getAuthor(), book.getPublicationYear(), book.getIsbn());
            // Since library.addBook is void, fetch the book again to return it
            Optional<Book> addedBookOpt = library.findBookByIsbn(book.getIsbn());
            if (addedBookOpt.isPresent()) {
                 return ResponseEntity.status(HttpStatus.CREATED).body(addedBookOpt.get()); // Return 201 Created with the new book
            } else {
                 // Should ideally not happen if add was successful and ISBN is correct
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Kitap eklendi ancak bulunamadı.");
            }
        } catch (DuplicateIsbnException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // Return 409 Conflict
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Return 400 Bad Request for validation errors
        }
    }

     // PUT /api/books/{id} - Update book by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBookById(@PathVariable long id, @RequestBody Book updatedBookData) {
        try {
            // Find the existing book first to get its ISBN
            Optional<Book> existingBookOpt = library.findBookById(id);
            if (existingBookOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Book existingBook = existingBookOpt.get();

            // Ensure the ID in the path matches the ID in the body if present
            if (updatedBookData.getId() != 0 && updatedBookData.getId() != id) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID in path does not match ID in request body.");
            }
             // Check for ISBN change attempt - generally disallowed or needs careful handling
            if (updatedBookData.getIsbn() != null && !updatedBookData.getIsbn().equals(existingBook.getIsbn())) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN güncellenemez.");
            }


            // Call library.updateBook with ISBN and individual fields from updatedBookData
            library.updateBook(
                existingBook.getIsbn(), // Use existing ISBN
                updatedBookData.getTitle(),
                updatedBookData.getAuthor(),
                updatedBookData.getPublicationYear() > 0 ? updatedBookData.getPublicationYear() : null, // Pass null if year is invalid/not provided
                updatedBookData.getStatus()
            );

            // Since library.updateBook is void, fetch the book again to return it
            Optional<Book> updatedBookOpt = library.findBookById(id);
             if (updatedBookOpt.isPresent()) {
                 return ResponseEntity.ok(updatedBookOpt.get());
            } else {
                 // Should ideally not happen if update was successful
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Kitap güncellendi ancak bulunamadı.");
            }

        } catch (BookNotFoundException e) {
             // This catch might now be redundant due to the initial findById check, but keep for safety
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) { // Catches potential errors from setters if used within updateBook
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        // DuplicateIsbnException is not expected here as we prevent ISBN changes
    }

    // PUT /api/books/isbn/{isbn} - Update book by ISBN
    @PutMapping("/isbn/{isbn}")
    public ResponseEntity<?> updateBookByIsbn(@PathVariable String isbn, @RequestBody Book updatedBookData) {
         Optional<Book> existingBookOpt = library.findBookByIsbn(isbn);
         if (existingBookOpt.isEmpty()) {
             return ResponseEntity.notFound().build();
         }
         // Ensure the ISBN in the path matches the ISBN in the body if present
         if (updatedBookData.getIsbn() != null && !updatedBookData.getIsbn().equals(isbn)) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN in path does not match ISBN in request body.");
         }

        try {
             // Call library.updateBook with ISBN and individual fields from updatedBookData
            library.updateBook(
                isbn, // Use ISBN from path
                updatedBookData.getTitle(),
                updatedBookData.getAuthor(),
                updatedBookData.getPublicationYear() > 0 ? updatedBookData.getPublicationYear() : null, // Pass null if year is invalid/not provided
                updatedBookData.getStatus()
            );

             // Since library.updateBook is void, fetch the book again to return it
            Optional<Book> updatedBookOpt = library.findBookByIsbn(isbn);
             if (updatedBookOpt.isPresent()) {
                 return ResponseEntity.ok(updatedBookOpt.get());
            } else {
                 // Should ideally not happen if update was successful
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Kitap güncellendi ancak bulunamadı.");
            }
        } catch (BookNotFoundException e) {
             // Should not happen due to the check above, but good practice
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
         // DuplicateIsbnException is not expected here as we prevent ISBN changes
    }


    // DELETE /api/books/{id} - Delete book by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable long id) {
        try {
            library.deleteBookById(id);
            return ResponseEntity.noContent().build(); // Return 204 No Content on success
        } catch (BookNotFoundException e) {
            return ResponseEntity.notFound().build(); // Return 404 if book not found
        }
    }

     // DELETE /api/books/isbn/{isbn} - Delete book by ISBN
    @DeleteMapping("/isbn/{isbn}")
    public ResponseEntity<Void> deleteBookByIsbn(@PathVariable String isbn) {
        try {
            library.deleteBookByIsbn(isbn);
            return ResponseEntity.noContent().build(); // Return 204 No Content on success
        } catch (BookNotFoundException e) {
            return ResponseEntity.notFound().build(); // Return 404 if book not found
        }
    }

    // Optional: Exception Handlers within the controller
    // Alternatively, a global @ControllerAdvice can be used

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<String> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<String> handleDuplicateIsbn(DuplicateIsbnException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

     @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        // Catches validation errors from Book setters or Library methods
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
