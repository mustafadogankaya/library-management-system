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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Unit Tests")
class BookControllerStandaloneTest {

    private MockMvc mockMvc;

    @Mock
    private Library library;

    @InjectMocks
    private BookController bookController;

    private ObjectMapper objectMapper;

    private Book testBook;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        // MockMvc standalone setup
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
        objectMapper = new ObjectMapper();
        
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
            doThrow(new IllegalArgumentException("Baslik bos olamaz")).when(library)
                    .addBook(anyString(), anyString(), anyInt(), anyString());

            Book invalidBook = new Book("", "Test Yazar", 2023, "978-0123456789");

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Baslik bos olamaz")));

            verify(library).addBook("", "Test Yazar", 2023, "978-0123456789");
            verifyNoMoreInteractions(library);
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
                    .andExpect(content().string(containsString("Kitap eklendi ancak")));

            verify(library).addBook("Test Kitap", "Test Yazar", 2023, "978-0123456789");
            verify(library).findBookByIsbn("978-0123456789");
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
    }

    @Nested
    @DisplayName("Exception Handler Tests")
    class ExceptionHandlerTests {

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
            doThrow(new IllegalArgumentException("Gecersiz girdi")).when(library)
                    .addBook(anyString(), anyString(), anyInt(), anyString());

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Gecersiz girdi"));

            verify(library).addBook(anyString(), anyString(), anyInt(), anyString());
            verifyNoMoreInteractions(library);
        }
    }
}