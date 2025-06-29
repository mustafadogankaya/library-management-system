package com.librarysystem.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import com.librarysystem.constants.LibraryConstants;

/**
 * Kütüphanedeki bir kitabı temsil eden sınıf.
 */
public class Book {
    private static final AtomicLong idCounter = new AtomicLong();

    private long id;
    private String title;
    private String author;
    private int publicationYear;
    private String isbn;
    private BookStatus status;

    /**
     * Default constructor for JSON deserialization.
     */
    public Book() {
        this.status = BookStatus.AVAILABLE;
    }

    /**
     * Creates a new book with the specified details.
     * @param title Book title (cannot be null or empty)
     * @param author Book author (cannot be null or empty) 
     * @param publicationYear Publication year (must be >= 1)
     * @param isbn ISBN number (cannot be null or empty)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Book(String title, String author, int publicationYear, String isbn) {
        this.id = idCounter.incrementAndGet();
        setTitle(title);
        setAuthor(author);
        setPublicationYear(publicationYear);
        setIsbn(isbn);
        this.status = BookStatus.AVAILABLE;
    }

    // --- Getters ---
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public BookStatus getStatus() {
        return status;
    }

     /**
     * Sets the ID for this book. Used primarily for JSON deserialization.
     * Updates the global ID counter to maintain consistency.
     * @param id The new ID for this book
     */
     public void setId(long id) {
         this.id = id;
         idCounter.updateAndGet(current -> Math.max(current, id));
     }

    /**
     * Sets the title of this book.
     * @param title The new title (cannot be null or empty)
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Kitap başlığı boş olamaz");
        }
        this.title = title.trim();
    }

    /**
     * Sets the author of this book.
     * @param author The new author (cannot be null or empty)
     * @throws IllegalArgumentException if author is null or empty
     */
    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Yazar adı boş olamaz");
        }
        this.author = author.trim();
    }

    /**
     * Sets the publication year of this book.
     * @param publicationYear The new publication year (must be >= 1)
     * @throws IllegalArgumentException if year is invalid
     */
    public void setPublicationYear(int publicationYear) {
        if (publicationYear < LibraryConstants.MIN_PUBLICATION_YEAR) {
            throw new IllegalArgumentException("Yayın yılı " + LibraryConstants.MIN_PUBLICATION_YEAR + " veya daha büyük olmalı");
        }
        this.publicationYear = publicationYear;
    }

    /**
     * Sets the ISBN of this book.
     * @param isbn The new ISBN (cannot be null or empty)
     * @throws IllegalArgumentException if ISBN is null or empty
     */
    public void setIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN boş olamaz");
        }
        this.isbn = isbn.trim();
    }

    /**
     * Sets the status of this book.
     * @param status The new status (cannot be null)
     * @throws IllegalArgumentException if status is null
     */
    public void setStatus(BookStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Kitap durumu boş olamaz");
        }
        this.status = status;
    }


    @Override
    public String toString() {
        return "Book{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", author='" + author + '\'' +
               ", publicationYear=" + publicationYear +
               ", isbn='" + isbn + '\'' +
               ", status=" + status +
               '}';
    }

    // Kitapları karşılaştırmak ve koleksiyonlarda doğru çalışmasını sağlamak için equals ve hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        // ID veya ISBN benzersizliği temsil etmek için yeterlidir. ISBN kullanalım.
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        // Benzersizliği sağlamak için ISBN kullanalım.
        return Objects.hash(isbn);
    }

     /**
      * ID sayacını mevcut en yüksek ID'ye ayarlar.
      * Bu metot genellikle veri yükleme sonrası çağrılır.
      * @param maxId Yüklenen verideki en yüksek ID.
      */
     public static void syncIdCounter(long maxId) {
         idCounter.set(maxId);
     }
}
