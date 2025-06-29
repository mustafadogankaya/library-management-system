package com.librarysystem.service;

import com.librarysystem.exception.BookNotFoundException;
import com.librarysystem.exception.DuplicateIsbnException;
import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import com.librarysystem.storage.DataStorage;
import com.librarysystem.storage.InMemoryDataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class LibraryTest {

    private Library library;
    private InMemoryDataStorage testStorage; // Sahte storage'a erişim için

    @BeforeEach
    void setUp() {
        // Her testten önce yeni bir InMemoryDataStorage ve Library oluştur
        testStorage = new InMemoryDataStorage();
        library = new Library(testStorage);
        // Book sınıfındaki statik ID sayacını sıfırla (test izolasyonu için önemli)
        // Bu normalde pek önerilmez ama statik sayaç kullanıldığı için gerekli.
        // Daha iyi bir yaklaşım ID üretimini ayrı bir servise taşımak olabilir.
        try {
            java.lang.reflect.Field counterField = Book.class.getDeclaredField("idCounter");
            counterField.setAccessible(true);
            java.util.concurrent.atomic.AtomicLong counter = (java.util.concurrent.atomic.AtomicLong) counterField.get(null);
            counter.set(0L); // Sayacı sıfırla
             Book.syncIdCounter(0L); // Statik metodu da çağır
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Book ID sayacı sıfırlanamadı: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Yeni bir kitap başarıyla eklenmeli")
    void addBook_shouldAddBookSuccessfully() throws DuplicateIsbnException {
        library.addBook("1984", "George Orwell", 1949, "ISBN001");
        List<Book> books = library.listAllBooks();
        assertEquals(1, books.size());
        Book addedBook = books.get(0);
        assertEquals("1984", addedBook.getTitle());
        assertEquals("George Orwell", addedBook.getAuthor());
        assertEquals(1949, addedBook.getPublicationYear());
        assertEquals("ISBN001", addedBook.getIsbn());
        assertEquals(BookStatus.AVAILABLE, addedBook.getStatus());
        assertEquals(1L, addedBook.getId()); // İlk eklenen kitabın ID'si 1 olmalı
    }

    @Test
    @DisplayName("Aynı ISBN ile kitap eklenmeye çalışıldığında DuplicateIsbnException fırlatmalı")
    void addBook_shouldThrowDuplicateIsbnException_whenIsbnExists() throws DuplicateIsbnException {
        library.addBook("1984", "George Orwell", 1949, "ISBN001");
        assertThrows(DuplicateIsbnException.class, () -> {
            library.addBook("Hayvan Çiftliği", "George Orwell", 1945, "ISBN001");
        });
        assertEquals(1, library.listAllBooks().size()); // İkinci kitap eklenmemeli
    }

     @Test
    @DisplayName("ID sayacı doğru şekilde artmalı")
    void addBook_shouldIncrementIdCorrectly() throws DuplicateIsbnException {
        library.addBook("Kitap 1", "Yazar 1", 2001, "ISBN001");
        library.addBook("Kitap 2", "Yazar 2", 2002, "ISBN002");
        library.addBook("Kitap 3", "Yazar 3", 2003, "ISBN003");

        Optional<Book> book1 = library.findBookByIsbn("ISBN001");
        Optional<Book> book2 = library.findBookByIsbn("ISBN002");
        Optional<Book> book3 = library.findBookByIsbn("ISBN003");

        assertTrue(book1.isPresent());
        assertTrue(book2.isPresent());
        assertTrue(book3.isPresent());

        assertEquals(1L, book1.get().getId());
        assertEquals(2L, book2.get().getId());
        assertEquals(3L, book3.get().getId());
    }


    @Test
    @DisplayName("Var olan bir kitap ISBN ile başarıyla silinmeli")
    void deleteBookByIsbn_shouldDeleteExistingBook() throws DuplicateIsbnException, BookNotFoundException {
        library.addBook("1984", "George Orwell", 1949, "ISBN001");
        assertEquals(1, library.listAllBooks().size());
        library.deleteBookByIsbn("ISBN001");
        assertEquals(0, library.listAllBooks().size());
        assertTrue(testStorage.getBooksDirectly().isEmpty()); // Storage'ın da boş olduğunu kontrol et
    }

    @Test
    @DisplayName("Var olmayan bir kitap ISBN ile silinmeye çalışıldığında BookNotFoundException fırlatmalı")
    void deleteBookByIsbn_shouldThrowBookNotFoundException_whenIsbnDoesNotExist() {
        assertThrows(BookNotFoundException.class, () -> {
            library.deleteBookByIsbn("NONEXISTENT_ISBN");
        });
    }

     @Test
    @DisplayName("Var olan bir kitap ID ile başarıyla silinmeli")
    void deleteBookById_shouldDeleteExistingBook() throws DuplicateIsbnException, BookNotFoundException {
        library.addBook("1984", "George Orwell", 1949, "ISBN001");
        Book addedBook = library.findBookByIsbn("ISBN001").orElseThrow();
        long idToDelete = addedBook.getId();

        assertEquals(1, library.listAllBooks().size());
        library.deleteBookById(idToDelete);
        assertEquals(0, library.listAllBooks().size());
        assertTrue(library.findBookById(idToDelete).isEmpty());
        assertTrue(testStorage.getBooksDirectly().isEmpty());
    }

    @Test
    @DisplayName("Var olmayan bir kitap ID ile silinmeye çalışıldığında BookNotFoundException fırlatmalı")
    void deleteBookById_shouldThrowBookNotFoundException_whenIdDoesNotExist() {
         assertThrows(BookNotFoundException.class, () -> {
            library.deleteBookById(999L); // Var olmayan bir ID
        });
    }


    @Test
    @DisplayName("Tüm kitaplar doğru şekilde listelenmeli")
    void listAllBooks_shouldReturnAllBooks() throws DuplicateIsbnException {
        assertTrue(library.listAllBooks().isEmpty()); // Başlangıçta boş olmalı
        library.addBook("Kitap 1", "Yazar A", 2000, "ISBN001");
        library.addBook("Kitap 2", "Yazar B", 2005, "ISBN002");
        assertEquals(2, library.listAllBooks().size());
    }

    @Test
    @DisplayName("Kitaplar başlık, yazar veya ISBN ile aranabilmeli")
    void searchBooks_shouldFindBooksByTitleAuthorOrIsbn() throws DuplicateIsbnException {
        library.addBook("Java Programlama", "Yazar X", 2020, "ISBNJAVA");
        library.addBook("Python Programlama", "Yazar Y", 2021, "ISBNPYTHON");
        library.addBook("Veritabanları", "Yazar X", 2019, "ISDB");

        // Başlığa göre arama (kısmi eşleşme, case-insensitive)
        List<Book> foundByTitle = library.searchBooks("programlama");
        assertEquals(2, foundByTitle.size());

         // Yazara göre arama (kısmi eşleşme, case-insensitive)
        List<Book> foundByAuthor = library.searchBooks("yazar x");
        assertEquals(2, foundByAuthor.size());

        // ISBN'e göre arama (tam eşleşme, case-insensitive)
        List<Book> foundByIsbn = library.searchBooks("isbnjava");
        assertEquals(1, foundByIsbn.size());
        assertEquals("Java Programlama", foundByIsbn.get(0).getTitle());

        // Eşleşmeyen arama
        List<Book> notFound = library.searchBooks("C++");
        assertTrue(notFound.isEmpty());

         // Boş sorgu
        List<Book> emptyQuery = library.searchBooks("");
        assertTrue(emptyQuery.isEmpty());
         List<Book> nullQuery = library.searchBooks(null);
        assertTrue(nullQuery.isEmpty());
    }

    @Test
    @DisplayName("Var olan bir kitap ISBN ile bulunabilmeli")
    void findBookByIsbn_shouldReturnBook_whenIsbnExists() throws DuplicateIsbnException {
        library.addBook("1984", "George Orwell", 1949, "ISBN001");
        Optional<Book> foundBook = library.findBookByIsbn("ISBN001");
        assertTrue(foundBook.isPresent());
        assertEquals("1984", foundBook.get().getTitle());
    }

    @Test
    @DisplayName("Var olmayan bir ISBN ile kitap arandığında boş Optional dönmeli")
    void findBookByIsbn_shouldReturnEmptyOptional_whenIsbnDoesNotExist() {
        Optional<Book> foundBook = library.findBookByIsbn("NONEXISTENT_ISBN");
        assertTrue(foundBook.isEmpty());
    }

     @Test
    @DisplayName("Var olan bir kitap ID ile bulunabilmeli")
    void findBookById_shouldReturnBook_whenIdExists() throws DuplicateIsbnException {
        library.addBook("1984", "George Orwell", 1949, "ISBN001");
        Book addedBook = library.findBookByIsbn("ISBN001").orElseThrow();
        Optional<Book> foundBook = library.findBookById(addedBook.getId());
        assertTrue(foundBook.isPresent());
        assertEquals("1984", foundBook.get().getTitle());
        assertEquals(addedBook.getId(), foundBook.get().getId());
    }

    @Test
    @DisplayName("Var olmayan bir ID ile kitap arandığında boş Optional dönmeli")
    void findBookById_shouldReturnEmptyOptional_whenIdDoesNotExist() {
        Optional<Book> foundBook = library.findBookById(999L);
        assertTrue(foundBook.isEmpty());
    }


    @Test
    @DisplayName("Var olan bir kitabın bilgileri güncellenmeli")
    void updateBook_shouldUpdateBookDetails() throws DuplicateIsbnException, BookNotFoundException {
        String isbn = "ISBN001";
        library.addBook("Eski Başlık", "Eski Yazar", 2000, isbn);

        String newTitle = "Yeni Başlık";
        String newAuthor = "Yeni Yazar";
        int newYear = 2022;
        BookStatus newStatus = BookStatus.BORROWED;

        library.updateBook(isbn, newTitle, newAuthor, newYear, newStatus);

        Book updatedBook = library.findBookByIsbn(isbn).orElseThrow();
        assertEquals(newTitle, updatedBook.getTitle());
        assertEquals(newAuthor, updatedBook.getAuthor());
        assertEquals(newYear, updatedBook.getPublicationYear());
        assertEquals(newStatus, updatedBook.getStatus());

        // Storage'ı da kontrol et
        Book storedBook = testStorage.getBooksDirectly().get(0);
         assertEquals(newTitle, storedBook.getTitle());
        assertEquals(newAuthor, storedBook.getAuthor());
        assertEquals(newYear, storedBook.getPublicationYear());
        assertEquals(newStatus, storedBook.getStatus());
    }

     @Test
    @DisplayName("Kitap güncellenirken sadece belirtilen alanlar değişmeli")
    void updateBook_shouldUpdateOnlySpecifiedFields() throws DuplicateIsbnException, BookNotFoundException {
        String isbn = "ISBN001";
        String originalTitle = "Orjinal Başlık";
        String originalAuthor = "Orjinal Yazar";
        int originalYear = 2000;
        BookStatus originalStatus = BookStatus.AVAILABLE;

        library.addBook(originalTitle, originalAuthor, originalYear, isbn);

        String newTitle = "Güncel Başlık";
        // Yazar, yıl ve durum null/boş gönderilecek
        library.updateBook(isbn, newTitle, null, null, null);

        Book updatedBook = library.findBookByIsbn(isbn).orElseThrow();
        assertEquals(newTitle, updatedBook.getTitle()); // Başlık güncellendi
        assertEquals(originalAuthor, updatedBook.getAuthor()); // Yazar aynı kaldı
        assertEquals(originalYear, updatedBook.getPublicationYear()); // Yıl aynı kaldı
        assertEquals(originalStatus, updatedBook.getStatus()); // Durum aynı kaldı

         // Sadece durumu güncelle
         BookStatus newStatus = BookStatus.BORROWED;
         library.updateBook(isbn, " ", "  ", 0, newStatus); // Boş string ve 0 yıl gönder

         updatedBook = library.findBookByIsbn(isbn).orElseThrow();
         assertEquals(newTitle, updatedBook.getTitle()); // Başlık aynı kaldı (boş string gönderildiği için)
         assertEquals(originalAuthor, updatedBook.getAuthor()); // Yazar aynı kaldı (boş string gönderildiği için)
         assertEquals(originalYear, updatedBook.getPublicationYear()); // Yıl aynı kaldı (0 gönderildiği için)
         assertEquals(newStatus, updatedBook.getStatus()); // Durum güncellendi
    }


    @Test
    @DisplayName("Var olmayan bir kitap güncellenmeye çalışıldığında BookNotFoundException fırlatmalı")
    void updateBook_shouldThrowBookNotFoundException_whenIsbnDoesNotExist() {
        assertThrows(BookNotFoundException.class, () -> {
            library.updateBook("NONEXISTENT_ISBN", "Yeni Başlık", null, null, null);
        });
    }

     @Test
    @DisplayName("Geçersiz kitap verisiyle kitap oluşturulmamalı")
    void constructor_shouldThrowException_whenInvalidData() {
        // Test invalid title
        assertThrows(IllegalArgumentException.class, () -> 
            new Book(null, "Valid Author", 2023, "VALID-ISBN"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("", "Valid Author", 2023, "VALID-ISBN"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("   ", "Valid Author", 2023, "VALID-ISBN"));

        // Test invalid author
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("Valid Title", null, 2023, "VALID-ISBN"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("Valid Title", "", 2023, "VALID-ISBN"));

        // Test invalid year
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("Valid Title", "Valid Author", 0, "VALID-ISBN"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("Valid Title", "Valid Author", -1, "VALID-ISBN"));

        // Test invalid ISBN
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("Valid Title", "Valid Author", 2023, null));
        assertThrows(IllegalArgumentException.class, () -> 
            new Book("Valid Title", "Valid Author", 2023, ""));
    }

    @Test
    @DisplayName("Veri yükleme sonrası ID sayacı doğru ayarlanmalı")
    void loadBooks_shouldSetIdCounterCorrectly() {
        // Storage'a manuel olarak ID'leri farklı kitaplar ekle
        Book book1 = new Book("Kitap 1", "Yazar 1", 2000, "ISBN001"); book1.setId(5);
        Book book2 = new Book("Kitap 2", "Yazar 2", 2001, "ISBN002"); book2.setId(10);
        testStorage.saveBooks(List.of(book1, book2));

        // Yeni Library instance'ı oluşturarak loadBooks'un çalışmasını sağla
        Library newLibrary = new Library(testStorage);

        // Yeni bir kitap ekle ve ID'sini kontrol et (en yüksek ID olan 10'dan sonra gelmeli)
        try {
            newLibrary.addBook("Yeni Kitap", "Yeni Yazar", 2023, "ISBN003");
        } catch (DuplicateIsbnException e) {
            fail("Kitap eklenirken hata oluşmamalıydı.");
        }

        Optional<Book> addedBook = newLibrary.findBookByIsbn("ISBN003");
        assertTrue(addedBook.isPresent());
        assertEquals(11L, addedBook.get().getId()); // ID 11 olmalı
    }
}
