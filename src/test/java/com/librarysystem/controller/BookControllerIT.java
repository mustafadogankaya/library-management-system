package com.librarysystem.controller;

import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = com.librarysystem.app.LibraryManagementWebApplication.class
)
@TestPropertySource(properties = {
    "library.data.file=test-data/integration-test-books.json"
})
class BookControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/books should return list of books")
    void getAllBooks_shouldReturnBooksList() {
        ResponseEntity<Book[]> response = restTemplate.getForEntity("/api/books", Book[].class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/books should create a new book")
    void addBook_shouldCreateNewBook() {
        Book newBook = new Book();
        newBook.setTitle("Test Kitap");
        newBook.setAuthor("Test Yazar");
        newBook.setPublicationYear(2023);
        newBook.setIsbn("TEST-ISBN-001");
        newBook.setStatus(BookStatus.AVAILABLE);

        ResponseEntity<Book> response = restTemplate.postForEntity("/api/books", newBook, Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Test Kitap");
        assertThat(response.getBody().getAuthor()).isEqualTo("Test Yazar");
        assertThat(response.getBody().getIsbn()).isEqualTo("TEST-ISBN-001");
    }

    @Test
    @DisplayName("POST /api/books with invalid data should return internal server error")
    void addBook_withInvalidData_shouldReturnInternalServerError() {
        Book invalidBook = new Book();
        // Missing required fields

        ResponseEntity<String> response = restTemplate.postForEntity("/api/books", invalidBook, String.class);

        // Application returns 500 for invalid data due to validation logic
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("GET /api/books/{id} should return specific book")
    void getBookById_shouldReturnBook() {
        // First create a book
        Book newBook = new Book();
        newBook.setTitle("Test Kitap 2");
        newBook.setAuthor("Test Yazar 2");
        newBook.setPublicationYear(2023);
        newBook.setIsbn("TEST-ISBN-002");
        newBook.setStatus(BookStatus.AVAILABLE);

        ResponseEntity<Book> createResponse = restTemplate.postForEntity("/api/books", newBook, Book.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Book createdBook = createResponse.getBody();
        assertThat(createdBook).isNotNull();

        // Then retrieve it by ID
        ResponseEntity<Book> getResponse = restTemplate.getForEntity("/api/books/{id}", Book.class, createdBook.getId());
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Test Kitap 2");
        assertThat(getResponse.getBody().getAuthor()).isEqualTo("Test Yazar 2");
        assertThat(getResponse.getBody().getIsbn()).isEqualTo("TEST-ISBN-002");
    }

    @Test
    @DisplayName("GET /api/books/{id} with non-existent ID should return not found")
    void getBookById_withNonExistentId_shouldReturnNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/books/{id}", String.class, 99999);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/books/{id} should update existing book")
    void updateBook_shouldUpdateExistingBook() {
        // First create a book
        Book newBook = new Book();
        newBook.setTitle("Original Title");
        newBook.setAuthor("Original Author");
        newBook.setPublicationYear(2020);
        newBook.setIsbn("TEST-ISBN-003");
        newBook.setStatus(BookStatus.AVAILABLE);

        ResponseEntity<Book> createResponse = restTemplate.postForEntity("/api/books", newBook, Book.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Book createdBook = createResponse.getBody();
        assertThat(createdBook).isNotNull();

        // Update the book
        createdBook.setTitle("Updated Title");
        createdBook.setAuthor("Updated Author");

        HttpEntity<Book> updateEntity = new HttpEntity<>(createdBook);
        ResponseEntity<Book> updateResponse = restTemplate.exchange(
            "/api/books/{id}", 
            HttpMethod.PUT, 
            updateEntity, 
            Book.class, 
            createdBook.getId()
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(updateResponse.getBody().getAuthor()).isEqualTo("Updated Author");
    }

    @Test
    @DisplayName("DELETE /api/books/{id} should delete existing book")
    void deleteBook_shouldDeleteExistingBook() {
        // First create a book
        Book newBook = new Book();
        newBook.setTitle("Book to Delete");
        newBook.setAuthor("Author to Delete");
        newBook.setPublicationYear(2021);
        newBook.setIsbn("TEST-ISBN-004");
        newBook.setStatus(BookStatus.AVAILABLE);

        ResponseEntity<Book> createResponse = restTemplate.postForEntity("/api/books", newBook, Book.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Book createdBook = createResponse.getBody();
        assertThat(createdBook).isNotNull();

        // Delete the book
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/books/{id}", 
            HttpMethod.DELETE, 
            null, 
            Void.class, 
            createdBook.getId()
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/api/books/{id}", String.class, createdBook.getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/books with search parameter should filter results")
    void getAllBooks_withSearchParameter_shouldFilterResults() {
        // First create a book with a unique title
        Book searchableBook = new Book();
        searchableBook.setTitle("Unique Search Term Book");
        searchableBook.setAuthor("Search Author");
        searchableBook.setPublicationYear(2022);
        searchableBook.setIsbn("SEARCH-ISBN-001");
        searchableBook.setStatus(BookStatus.AVAILABLE);

        ResponseEntity<Book> createResponse = restTemplate.postForEntity("/api/books", searchableBook, Book.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Search for the book
        ResponseEntity<Book[]> searchResponse = restTemplate.getForEntity("/api/books?search=Unique Search Term", Book[].class);
        
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).isNotNull();
        
        boolean foundSearchableBook = false;
        for (Book book : searchResponse.getBody()) {
            if (book.getTitle().contains("Unique Search Term")) {
                foundSearchableBook = true;
                break;
            }
        }
        assertThat(foundSearchableBook).isTrue();
    }
}