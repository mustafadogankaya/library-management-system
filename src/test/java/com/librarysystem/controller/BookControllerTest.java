package com.librarysystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.exception.BookNotFoundException;
import com.librarysystem.exception.DuplicateIsbnException;
import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import com.librarysystem.service.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BookController için kapsamlı unit testler.
 * MockMvc kullanarak web katmanını izole ederek test eder.
 */
@WebMvcTest(BookController.class)
@ContextConfiguration(classes = {BookController.class})
@DisplayName("BookController Unit Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Library library;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        // Test için örnek kitap nesneleri oluştur
        testBook = new Book("Test Kitap", "Test Yazar", 2023, "978-0123456789");
        testBook2 = new Book("Başka Kitap", "Başka Yazar", 2022, "978-0987654321");
        
        // ID'leri manuel olarak ayarla (Book constructor otomatik ID atar ama test için kontrol etmek istiyoruz)
        setBookId(testBook, 1L);
        setBookId(testBook2, 2L);
    }

    /**
     * Book nesnesinin ID'sini reflection kullanarak ayarlar (test amaçlı)
     */
    private void setBookId(Book book, long id) {
        try {
            java.lang.reflect.Field idField = Book.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(book, id);
        } catch (Exception e) {
            throw new RuntimeException("Test için Book ID ayarlanamadı", e);
        }
    }

    @Nested
    @DisplayName("GET /api/books - Tüm kitapları listele")
    class GetAllBooksTests {

        @Test
        @DisplayName("Parametresiz çağrı - tüm kitapları döndürür")
        void getAllBooks_withoutParameters_shouldReturnAllBooks() throws Exception {
            // Given
            List<Book> allBooks = Arrays.asList(testBook, testBook2);
            when(library.listAllBooks()).thenReturn(allBooks);

            // When & Then
            mockMvc.perform(get("/api/books"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].title", is("Test Kitap")))
                    .andExpect(jsonPath("$[0].author", is("Test Yazar")))
                    .andExpect(jsonPath("$[0].isbn", is("978-0123456789")))
                    .andExpect(jsonPath("$[1].title", is("Başka Kitap")));

            verify(library).listAllBooks();
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Arama parametresi ile - arama sonuçlarını döndürür")
        void getAllBooks_withSearchParameter_shouldReturnSearchResults() throws Exception {
            // Given
            List<Book> searchResults = Collections.singletonList(testBook);
            when(library.searchBooks("Test")).thenReturn(searchResults);

            // When & Then
            mockMvc.perform(get("/api/books").param("search", "Test"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("Test Kitap")));

            verify(library).searchBooks("Test");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Filtre parametresi ile - filtrelenmiş sonuçları döndürür")
        void getAllBooks_withFilterParameter_shouldReturnFilteredResults() throws Exception {
            // Given
            List<Book> filteredResults = Collections.singletonList(testBook);
            when(library.filterBooks("author:Test Yazar")).thenReturn(filteredResults);

            // When & Then
            mockMvc.perform(get("/api/books").param("filter", "author:Test Yazar"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].author", is("Test Yazar")));

            verify(library).filterBooks("author:Test Yazar");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Sıralama parametresi ile - sıralanmış sonuçları döndürür")
        void getAllBooks_withSortParameter_shouldReturnSortedResults() throws Exception {
            // Given
            List<Book> sortedResults = Arrays.asList(testBook, testBook2);
            when(library.sortBooks("title", true)).thenReturn(sortedResults);

            // When & Then
            mockMvc.perform(get("/api/books")
                    .param("sortBy", "title")
                    .param("ascending", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(library).sortBooks("title", true);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Boş arama parametresi - tüm kitapları döndürür")
        void getAllBooks_withEmptySearchParameter_shouldReturnAllBooks() throws Exception {
            // Given
            List<Book> allBooks = Arrays.asList(testBook, testBook2);
            when(library.listAllBooks()).thenReturn(allBooks);

            // When & Then
            mockMvc.perform(get("/api/books").param("search", ""))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(library).listAllBooks();
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Azalan sıralama ile")
        void getAllBooks_withDescendingSortParameter_shouldReturnDescendingSortedResults() throws Exception {
            // Given
            List<Book> sortedResults = Arrays.asList(testBook2, testBook);
            when(library.sortBooks("title", false)).thenReturn(sortedResults);

            // When & Then
            mockMvc.perform(get("/api/books")
                    .param("sortBy", "title")
                    .param("ascending", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(library).sortBooks("title", false);
            verifyNoMoreInteractions(library);
        }
    }

    @Nested
    @DisplayName("GET /api/books/{id} - ID ile kitap getir")
    class GetBookByIdTests {

        @Test
        @DisplayName("Geçerli ID ile - kitabı döndürür")
        void getBookById_withValidId_shouldReturnBook() throws Exception {
            // Given
            when(library.findBookById(1L)).thenReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(get("/api/books/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("Test Kitap")))
                    .andExpect(jsonPath("$.author", is("Test Yazar")))
                    .andExpect(jsonPath("$.isbn", is("978-0123456789")))
                    .andExpect(jsonPath("$.publicationYear", is(2023)))
                    .andExpect(jsonPath("$.status", is("AVAILABLE")));

            verify(library).findBookById(1L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ID ile - 404 döndürür")
        void getBookById_withInvalidId_shouldReturn404() throws Exception {
            // Given
            when(library.findBookById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/books/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).findBookById(999L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Negatif ID ile - 404 döndürür")
        void getBookById_withNegativeId_shouldReturn404() throws Exception {
            // Given
            when(library.findBookById(-1L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/books/-1"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).findBookById(-1L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ID formatı ile - 400 döndürür")
        void getBookById_withInvalidIdFormat_shouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/books/invalid"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(library);
        }
    }

    @Nested
    @DisplayName("POST /api/books - Yeni kitap ekle")
    class AddBookTests {

        @Test
        @DisplayName("Geçerli kitap bilgileri ile - 201 Created döndürür")
        void addBook_withValidBook_shouldReturn201Created() throws Exception {
            // Given
            doNothing().when(library).addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            when(library.findBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title", is("Test Kitap")))
                    .andExpect(jsonPath("$.author", is("Test Yazar")))
                    .andExpect(jsonPath("$.isbn", is("978-0123456789")));

            verify(library).addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            verify(library).findBookByIsbn("978-0123456789");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Duplicate ISBN ile - 409 Conflict döndürür")
        void addBook_withDuplicateIsbn_shouldReturn409Conflict() throws Exception {
            // Given
            doThrow(new DuplicateIsbnException("Bu ISBN zaten mevcut")).when(library)
                    .addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("Bu ISBN zaten mevcut")));

            verify(library).addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz kitap bilgileri ile - 400 Bad Request döndürür")
        void addBook_withInvalidBook_shouldReturn400BadRequest() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Başlık boş olamaz")).when(library)
                    .addBook(anyString(), anyString(), anyInt(), anyString());

            Book invalidBook = new Book("", "Test Yazar", 2023, "978-0123456789");

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Başlık boş olamaz")));

            verify(library).addBook("", "Test Yazar", 2023, "978-0123456789");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz JSON ile - 400 Bad Request döndürür")
        void addBook_withInvalidJson_shouldReturn400BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(library);
        }

        @Test
        @DisplayName("Boş request body ile - 400 Bad Request döndürür")
        void addBook_withEmptyBody_shouldReturn400BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(library);
        }

        @Test
        @DisplayName("Kitap eklendi ama bulunamadı - 500 Internal Server Error döndürür")
        void addBook_whenBookAddedButNotFound_shouldReturn500InternalServerError() throws Exception {
            // Given
            doNothing().when(library).addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            when(library.findBookByIsbn("978-0123456789")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Kitap eklendi ancak bulunamadı."));

            verify(library).addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            verify(library).findBookByIsbn("978-0123456789");
            verifyNoMoreInteractions(library);
        }
    }

    @Nested
    @DisplayName("PUT /api/books/{id} - ID ile kitap güncelle")
    class UpdateBookByIdTests {

        @Test
        @DisplayName("Geçerli ID ve kitap bilgileri ile - güncellenmiş kitabı döndürür")
        void updateBookById_withValidIdAndBook_shouldReturnUpdatedBook() throws Exception {
            // Given
            Book updatedBook = new Book("Güncellenmiş Kitap", "Güncellenmiş Yazar", 2024, "978-0123456789");
            setBookId(updatedBook, 1L);
            updatedBook.setStatus(BookStatus.BORROWED);

            when(library.findBookById(1L)).thenReturn(Optional.of(testBook), Optional.of(updatedBook));
            doNothing().when(library).updateBook("978-0123456789", "Güncellenmiş Kitap", "Güncellenmiş Yazar", 2024, BookStatus.BORROWED);

            // When & Then
            mockMvc.perform(put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedBook)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title", is("Güncellenmiş Kitap")))
                    .andExpect(jsonPath("$.author", is("Güncellenmiş Yazar")))
                    .andExpect(jsonPath("$.publicationYear", is(2024)))
                    .andExpect(jsonPath("$.status", is("BORROWED")));

            verify(library, times(2)).findBookById(1L);
            verify(library).updateBook("978-0123456789", "Güncellenmiş Kitap", "Güncellenmiş Yazar", 2024, BookStatus.BORROWED);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ID ile - 404 Not Found döndürür")
        void updateBookById_withInvalidId_shouldReturn404NotFound() throws Exception {
            // Given
            when(library.findBookById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put("/api/books/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).findBookById(999L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Path ve body ID'leri eşleşmediğinde - 400 Bad Request döndürür")
        void updateBookById_withMismatchedIds_shouldReturn400BadRequest() throws Exception {
            // Given
            Book bookWithDifferentId = new Book("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            setBookId(bookWithDifferentId, 2L); // Farklı ID

            when(library.findBookById(1L)).thenReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookWithDifferentId)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("ID in path does not match ID in request body."));

            verify(library).findBookById(1L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("ISBN değiştirilmeye çalışıldığında - 400 Bad Request döndürür")
        void updateBookById_whenIsbnChangeAttempted_shouldReturn400BadRequest() throws Exception {
            // Given
            Book bookWithDifferentIsbn = new Book("Test Kitap", "Test Yazar", 2023, "978-0987654321");
            setBookId(bookWithDifferentIsbn, 1L);

            when(library.findBookById(1L)).thenReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookWithDifferentIsbn)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("ISBN güncellenemez."));

            verify(library).findBookById(1L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("BookNotFoundException fırlatıldığında - 404 Not Found döndürür")
        void updateBookById_whenBookNotFoundExceptionThrown_shouldReturn404NotFound() throws Exception {
            // Given
            when(library.findBookById(1L)).thenReturn(Optional.of(testBook));
            doThrow(new BookNotFoundException("Kitap bulunamadı")).when(library)
                    .updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));

            // When & Then
            mockMvc.perform(put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).findBookById(1L);
            verify(library).updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("IllegalArgumentException fırlatıldığında - 400 Bad Request döndürür")
        void updateBookById_whenIllegalArgumentExceptionThrown_shouldReturn400BadRequest() throws Exception {
            // Given
            when(library.findBookById(1L)).thenReturn(Optional.of(testBook));
            doThrow(new IllegalArgumentException("Geçersiz kitap bilgisi")).when(library)
                    .updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));

            // When & Then
            mockMvc.perform(put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Geçersiz kitap bilgisi"));

            verify(library).findBookById(1L);
            verify(library).updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Sıfır yayın yılı ile güncelleme - null olarak geçer")
        void updateBookById_withZeroPublicationYear_shouldPassNullToLibrary() throws Exception {
            // Given
            Book bookWithZeroYear = new Book("Test Kitap", "Test Yazar", 0, "978-0123456789");
            setBookId(bookWithZeroYear, 1L);

            when(library.findBookById(1L)).thenReturn(Optional.of(testBook), Optional.of(bookWithZeroYear));
            doNothing().when(library).updateBook("978-0123456789", "Test Kitap", "Test Yazar", null, BookStatus.AVAILABLE);

            // When & Then
            mockMvc.perform(put("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookWithZeroYear)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(library, times(2)).findBookById(1L);
            verify(library).updateBook("978-0123456789", "Test Kitap", "Test Yazar", null, BookStatus.AVAILABLE);
            verifyNoMoreInteractions(library);
        }
    }

    @Nested
    @DisplayName("PUT /api/books/isbn/{isbn} - ISBN ile kitap güncelle")
    class UpdateBookByIsbnTests {

        @Test
        @DisplayName("Geçerli ISBN ve kitap bilgileri ile - güncellenmiş kitabı döndürür")
        void updateBookByIsbn_withValidIsbnAndBook_shouldReturnUpdatedBook() throws Exception {
            // Given
            Book updatedBook = new Book("Güncellenmiş Kitap", "Güncellenmiş Yazar", 2024, "978-0123456789");
            updatedBook.setStatus(BookStatus.BORROWED);

            when(library.findBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook), Optional.of(updatedBook));
            doNothing().when(library).updateBook("978-0123456789", "Güncellenmiş Kitap", "Güncellenmiş Yazar", 2024, BookStatus.BORROWED);

            // When & Then
            mockMvc.perform(put("/api/books/isbn/978-0123456789")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedBook)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title", is("Güncellenmiş Kitap")))
                    .andExpect(jsonPath("$.author", is("Güncellenmiş Yazar")))
                    .andExpect(jsonPath("$.publicationYear", is(2024)))
                    .andExpect(jsonPath("$.status", is("BORROWED")));

            verify(library, times(2)).findBookByIsbn("978-0123456789");
            verify(library).updateBook("978-0123456789", "Güncellenmiş Kitap", "Güncellenmiş Yazar", 2024, BookStatus.BORROWED);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ISBN ile - 404 Not Found döndürür")
        void updateBookByIsbn_withInvalidIsbn_shouldReturn404NotFound() throws Exception {
            // Given
            when(library.findBookByIsbn("978-0000000000")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put("/api/books/isbn/978-0000000000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).findBookByIsbn("978-0000000000");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Path ve body ISBN'leri eşleşmediğinde - 400 Bad Request döndürür")
        void updateBookByIsbn_withMismatchedIsbns_shouldReturn400BadRequest() throws Exception {
            // Given
            Book bookWithDifferentIsbn = new Book("Test Kitap", "Test Yazar", 2023, "978-0987654321");

            when(library.findBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(put("/api/books/isbn/978-0123456789")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookWithDifferentIsbn)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("ISBN in path does not match ISBN in request body."));

            verify(library).findBookByIsbn("978-0123456789");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("BookNotFoundException fırlatıldığında - 404 Not Found döndürür")
        void updateBookByIsbn_whenBookNotFoundExceptionThrown_shouldReturn404NotFound() throws Exception {
            // Given
            when(library.findBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));
            doThrow(new BookNotFoundException("Kitap bulunamadı")).when(library)
                    .updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));

            // When & Then
            mockMvc.perform(put("/api/books/isbn/978-0123456789")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).findBookByIsbn("978-0123456789");
            verify(library).updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("IllegalArgumentException fırlatıldığında - 400 Bad Request döndürür")
        void updateBookByIsbn_whenIllegalArgumentExceptionThrown_shouldReturn400BadRequest() throws Exception {
            // Given
            when(library.findBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));
            doThrow(new IllegalArgumentException("Geçersiz kitap bilgisi")).when(library)
                    .updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));

            // When & Then
            mockMvc.perform(put("/api/books/isbn/978-0123456789")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Geçersiz kitap bilgisi"));

            verify(library).findBookByIsbn("978-0123456789");
            verify(library).updateBook(anyString(), anyString(), anyString(), anyInt(), any(BookStatus.class));
            verifyNoMoreInteractions(library);
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{id} - ID ile kitap sil")
    class DeleteBookByIdTests {

        @Test
        @DisplayName("Geçerli ID ile - 204 No Content döndürür")
        void deleteBookById_withValidId_shouldReturn204NoContent() throws Exception {
            // Given
            doNothing().when(library).deleteBookById(1L);

            // When & Then
            mockMvc.perform(delete("/api/books/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(library).deleteBookById(1L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ID ile - 404 Not Found döndürür")
        void deleteBookById_withInvalidId_shouldReturn404NotFound() throws Exception {
            // Given
            doThrow(new BookNotFoundException("Kitap bulunamadı")).when(library).deleteBookById(999L);

            // When & Then
            mockMvc.perform(delete("/api/books/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).deleteBookById(999L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ID formatı ile - 400 Bad Request döndürür")
        void deleteBookById_withInvalidIdFormat_shouldReturn400BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/books/invalid"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(library);
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/isbn/{isbn} - ISBN ile kitap sil")
    class DeleteBookByIsbnTests {

        @Test
        @DisplayName("Geçerli ISBN ile - 204 No Content döndürür")
        void deleteBookByIsbn_withValidIsbn_shouldReturn204NoContent() throws Exception {
            // Given
            doNothing().when(library).deleteBookByIsbn("978-0123456789");

            // When & Then
            mockMvc.perform(delete("/api/books/isbn/978-0123456789"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(library).deleteBookByIsbn("978-0123456789");
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("Geçersiz ISBN ile - 404 Not Found döndürür")
        void deleteBookByIsbn_withInvalidIsbn_shouldReturn404NotFound() throws Exception {
            // Given
            doThrow(new BookNotFoundException("Kitap bulunamadı")).when(library).deleteBookByIsbn("978-0000000000");

            // When & Then
            mockMvc.perform(delete("/api/books/isbn/978-0000000000"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(library).deleteBookByIsbn("978-0000000000");
            verifyNoMoreInteractions(library);
        }
    }

    @Nested
    @DisplayName("Exception Handler Tests")
    class ExceptionHandlerTests {

        @Test
        @DisplayName("BookNotFoundException için 404 status ve mesaj döndürür")
        void handleBookNotFoundException_shouldReturn404WithMessage() throws Exception {
            // Given
            when(library.findBookById(999L)).thenThrow(new BookNotFoundException("Kitap bulunamadı"));

            // When & Then
            mockMvc.perform(get("/api/books/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Kitap bulunamadı"));

            verify(library).findBookById(999L);
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("DuplicateIsbnException için 409 status ve mesaj döndürür")
        void handleDuplicateIsbnException_shouldReturn409WithMessage() throws Exception {
            // Given
            doThrow(new DuplicateIsbnException("Bu ISBN zaten mevcut")).when(library)
                    .addBook(anyString(), anyString(), anyInt(), anyString());

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(content().string("Bu ISBN zaten mevcut"));

            verify(library).addBook(anyString(), anyString(), anyInt(), anyString());
            verifyNoMoreInteractions(library);
        }

        @Test
        @DisplayName("IllegalArgumentException için 400 status ve mesaj döndürür")
        void handleIllegalArgumentException_shouldReturn400WithMessage() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Geçersiz girdi")).when(library)
                    .addBook(anyString(), anyString(), anyInt(), anyString());

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Geçersiz girdi"));

            verify(library).addBook(anyString(), anyString(), anyInt(), anyString());
            verifyNoMoreInteractions(library);
        }
    }

    // Diğer test grupları devam edecek...
}