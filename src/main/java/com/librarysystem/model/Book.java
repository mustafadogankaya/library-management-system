package com.librarysystem.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Kütüphanedeki bir kitabı temsil eden sınıf.
 */
@Entity
@Table(name = "books")
public class Book {
    // Keep the static counter for backward compatibility with JSON storage
    @JsonIgnore
    private static final AtomicLong idCounter = new AtomicLong(); // Benzersiz ID üretimi için sayaç

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(name = "publication_year", nullable = false)
    private int publicationYear;
    
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;
    
    @JsonIgnore
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @JsonIgnore
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Jackson kütüphanesinin JSON'dan nesne oluşturabilmesi için varsayılan constructor
    public Book() {
        this.status = BookStatus.AVAILABLE; // Varsayılan durum
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public Book(String title, String author, int publicationYear, String isbn) {
        this.id = idCounter.incrementAndGet(); // Otomatik artan ID ata
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.status = BookStatus.AVAILABLE; // Yeni eklenen kitap varsayılan olarak mevcut
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

     // --- Setters ---
     // ID'nin dışarıdan değiştirilmemesi gerektiği için setId metodu yok.
     // Ancak JSON deserialization için gerekebilir, bu yüzden ekleyelim ama dikkatli kullanalım.
     public void setId(long id) {
         this.id = id;
         // ID sayacını güncel tutmak için (eğer yüklenen ID mevcut sayaçtan büyükse)
         idCounter.updateAndGet(current -> Math.max(current, id));
     }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setIsbn(String isbn) {
        // ISBN genellikle değişmez ama güncelleme senaryosu için ekleyebiliriz.
        // Ancak ISBN'nin benzersiz olması gerektiği Library sınıfında kontrol edilmeli.
        this.isbn = isbn;
    }

    public void setStatus(BookStatus status) {
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
