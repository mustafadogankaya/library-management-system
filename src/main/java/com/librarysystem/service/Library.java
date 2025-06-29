package com.librarysystem.service;

import com.librarysystem.exception.BookNotFoundException;
import com.librarysystem.exception.DuplicateIsbnException;
import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import com.librarysystem.storage.DataStorage;
import com.librarysystem.constants.LibraryConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kütüphanedeki kitap koleksiyonunu yöneten sınıf.
 * Kitap ekleme, silme, listeleme, arama ve güncelleme işlemlerini sağlar.
 */
@Service
public class Library {

    private static final Logger logger = LoggerFactory.getLogger(Library.class);
    
    private final ConcurrentHashMap<String, Book> booksByIsbn;
    private final DataStorage dataStorage;

    /**
     * Library sınıfını başlatır ve mevcut kitapları yükler.
     * @param dataStorage Kitap verilerini okuyup yazacak DataStorage nesnesi.
     */
    @Autowired
    public Library(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.booksByIsbn = new ConcurrentHashMap<>();
        loadBooks();
    }

    /**
     * Yeni bir kitap ekler.
     * @param title Kitap başlığı.
     * @param author Kitap yazarı.
     * @param publicationYear Yayın yılı.
     * @param isbn Benzersiz ISBN numarası.
     * @throws DuplicateIsbnException Eğer aynı ISBN ile başka bir kitap varsa.
     */
    public void addBook(String title, String author, int publicationYear, String isbn) throws DuplicateIsbnException {
        if (booksByIsbn.containsKey(isbn)) {
            throw new DuplicateIsbnException(LibraryConstants.ERROR_DUPLICATE_ISBN + ": " + isbn);
        }
        Book newBook = new Book(title, author, publicationYear, isbn);
        booksByIsbn.put(isbn, newBook);
        saveBooks();
    }

    /**
     * ISBN numarasına göre bir kitabı siler.
     * @param isbn Silinecek kitabın ISBN numarası.
     * @throws BookNotFoundException Eğer belirtilen ISBN ile kitap bulunamazsa.
     */
    public void deleteBookByIsbn(String isbn) throws BookNotFoundException {
        Book removedBook = booksByIsbn.remove(isbn);
        if (removedBook == null) {
            throw new BookNotFoundException(LibraryConstants.ERROR_BOOK_NOT_FOUND + " (ISBN): " + isbn);
        }
        saveBooks();
    }

    /**
     * Kitap ID'sine göre bir kitabı siler.
     * @param id Silinecek kitabın ID'si.
     * @throws BookNotFoundException Eğer belirtilen ID ile kitap bulunamazsa.
     */
    public void deleteBookById(long id) throws BookNotFoundException {
        Optional<Book> bookToRemove = findBookByIdInternal(id);
        if (bookToRemove.isPresent()) {
            booksByIsbn.remove(bookToRemove.get().getIsbn());
            saveBooks();
        } else {
            throw new BookNotFoundException(LibraryConstants.ERROR_BOOK_NOT_FOUND + " (ID): " + id);
        }
    }


    /**
     * Kütüphanedeki tüm kitapları listeler.
     * @return Kitap listesi.
     */
    public List<Book> listAllBooks() {
        return new ArrayList<>(booksByIsbn.values());
    }

    /**
     * Kitapları belirtilen kritere göre filtreler.
     * @param filter Kriter (örneğin, "author:George Orwell", "year:1984", "status:AVAILABLE").
     * @return Filtrelenmiş kitap listesi.
     */
    public List<Book> filterBooks(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return listAllBooks();
        }

        String[] parts = filter.split(LibraryConstants.FILTER_DELIMITER, LibraryConstants.FILTER_PARTS_COUNT);
        if (parts.length != LibraryConstants.FILTER_PARTS_COUNT) {
            logger.warn(LibraryConstants.ERROR_INVALID_FILTER_FORMAT);
            return new ArrayList<>();
        }

        String field = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        return booksByIsbn.values().stream()
                .filter(book -> matchesFilter(book, field, value))
                .collect(Collectors.toList());
    }

     /**
     * Kitapları belirtilen alana göre sıralar.
     * @param sortBy Sıralama alanı (title, author, year, isbn, id).
     * @param ascending Artan sırada mı (true) yoksa azalan sırada mı (false).
     * @return Sıralanmış kitap listesi.
     */
    public List<Book> sortBooks(String sortBy, boolean ascending) {
        Comparator<Book> comparator;
        switch (sortBy.toLowerCase()) {
            case LibraryConstants.SORT_FIELD_TITLE:
                comparator = Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case LibraryConstants.SORT_FIELD_AUTHOR:
                comparator = Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER);
                break;
            case LibraryConstants.SORT_FIELD_YEAR:
                comparator = Comparator.comparingInt(Book::getPublicationYear);
                break;
            case LibraryConstants.SORT_FIELD_ISBN:
                comparator = Comparator.comparing(Book::getIsbn);
                break;
             case LibraryConstants.SORT_FIELD_ID:
                comparator = Comparator.comparingLong(Book::getId);
                break;
            default:
                logger.warn("{}: {}. ID'ye göre sıralanıyor.", LibraryConstants.ERROR_INVALID_SORT_FIELD, sortBy);
                 comparator = Comparator.comparingLong(Book::getId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return booksByIsbn.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }


    /**
     * Başlık, yazar veya ISBN'ye göre kitap arar.
     * @param query Arama sorgusu.
     * @return Eşleşen kitapların listesi.
     */
    public List<Book> searchBooks(String query) {
         if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerCaseQuery = query.toLowerCase();
        return booksByIsbn.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                               book.getAuthor().toLowerCase().contains(lowerCaseQuery) ||
                               book.getIsbn().equalsIgnoreCase(lowerCaseQuery))
                .collect(Collectors.toList());
    }

     /**
     * ISBN numarasına göre bir kitabı bulur.
     * @param isbn Aranan kitabın ISBN'si.
     * @return Kitabı içeren Optional, bulunamazsa boş Optional.
     */
    public Optional<Book> findBookByIsbn(String isbn) {
        return Optional.ofNullable(booksByIsbn.get(isbn));
    }

    /**
     * Kitap ID'sine göre bir kitabı bulur.
     * @param id Aranan kitabın ID'si.
     * @return Kitabı içeren Optional, bulunamazsa boş Optional.
     */
    public Optional<Book> findBookById(long id) {
       return findBookByIdInternal(id);
    }


    /**
     * Mevcut bir kitabın bilgilerini günceller.
     * @param isbn Güncellenecek kitabın ISBN'si.
     * @param newTitle Yeni başlık (değişmiyorsa null).
     * @param newAuthor Yeni yazar (değişmiyorsa null).
     * @param newPublicationYear Yeni yayın yılı (değişmiyorsa null veya <= 0).
     * @param newStatus Yeni durum (değişmiyorsa null).
     * @throws BookNotFoundException Eğer belirtilen ISBN ile kitap bulunamazsa.
     */
    public void updateBook(String isbn, String newTitle, String newAuthor, Integer newPublicationYear, BookStatus newStatus) throws BookNotFoundException {
        Book bookToUpdate = booksByIsbn.get(isbn);
        if (bookToUpdate == null) {
            throw new BookNotFoundException(LibraryConstants.ERROR_BOOK_NOT_FOUND + " (güncelleme): " + isbn);
        }

        boolean updated = false;
        if (newTitle != null && !newTitle.isBlank() && !newTitle.equals(bookToUpdate.getTitle())) {
            bookToUpdate.setTitle(newTitle);
            updated = true;
        }
        if (newAuthor != null && !newAuthor.isBlank() && !newAuthor.equals(bookToUpdate.getAuthor())) {
            bookToUpdate.setAuthor(newAuthor);
            updated = true;
        }
        if (newPublicationYear != null && newPublicationYear >= LibraryConstants.MIN_PUBLICATION_YEAR && newPublicationYear != bookToUpdate.getPublicationYear()) {
            bookToUpdate.setPublicationYear(newPublicationYear);
            updated = true;
        }
         if (newStatus != null && newStatus != bookToUpdate.getStatus()) {
            bookToUpdate.setStatus(newStatus);
            updated = true;
        }


        if (updated) {
            saveBooks();
        }
    }

    // --- Yardımcı Metotlar ---

    /**
     * Kitapları dosyadan yükler.
     */
    private void loadBooks() {
        List<Book> loadedBooks = dataStorage.loadBooks();
        long maxId = 0;
        booksByIsbn.clear(); // Haritanın boş olduğundan emin ol
        for (Book book : loadedBooks) {
            // Yüklenen kitapları ISBN anahtarıyla map'e ekle
            // Eğer aynı ISBN varsa, son okunan geçerli olur (veya birleştirme stratejisi uygulanabilir)
            booksByIsbn.put(book.getIsbn(), book);
            if (book.getId() > maxId) {
                maxId = book.getId(); // En yüksek ID'yi bul
            }
        }
         Book.syncIdCounter(maxId);
        logger.info("{} kitap başarıyla yüklendi.", loadedBooks.size());
    }

    /**
     * Kitapları dosyaya kaydeder.
     */
    private void saveBooks() {
        dataStorage.saveBooks(new ArrayList<>(booksByIsbn.values()));
    }

     /**
     * Bir kitabın belirtilen filtre kriteriyle eşleşip eşleşmediğini kontrol eder.
     * @param book Kontrol edilecek kitap.
     * @param field Filtre alanı (author, year, status, title, isbn).
     * @param value Filtre değeri.
     * @return Eşleşiyorsa true, aksi takdirde false.
     */
    private boolean matchesFilter(Book book, String field, String value) {
        switch (field) {
            case LibraryConstants.FILTER_FIELD_AUTHOR:
                return book.getAuthor().toLowerCase().contains(value.toLowerCase());
            case LibraryConstants.FILTER_FIELD_YEAR:
                try {
                    int year = Integer.parseInt(value);
                    return book.getPublicationYear() == year;
                } catch (NumberFormatException e) {
                    return false;
                }
            case LibraryConstants.FILTER_FIELD_STATUS:
                try {
                    BookStatus status = BookStatus.valueOf(value.toUpperCase());
                    return book.getStatus() == status;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            case LibraryConstants.FILTER_FIELD_TITLE:
                 return book.getTitle().toLowerCase().contains(value.toLowerCase());
            case LibraryConstants.FILTER_FIELD_ISBN:
                 return book.getIsbn().equalsIgnoreCase(value);
            default:
                return false;
        }
    }

     /**
     * ID'ye göre kitabı dahili olarak arar.
     * @param id Aranan ID.
     * @return Kitabı içeren Optional.
     */
     private Optional<Book> findBookByIdInternal(long id) {
        return booksByIsbn.values().stream()
                .filter(book -> book.getId() == id)
                .findFirst();
    }

}
