package com.librarysystem.ui;

import com.librarysystem.exception.BookNotFoundException;
import com.librarysystem.exception.DuplicateIsbnException;
import com.librarysystem.model.Book;
import com.librarysystem.model.BookStatus;
import com.librarysystem.service.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Kütüphane yönetim sistemi için konsol tabanlı kullanıcı arayüzü.
 */
public class ConsoleUI {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);

    private final Library library;
    private final Scanner scanner;

    public ConsoleUI(Library library) {
        this.library = library;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Ana menüyü gösterir ve kullanıcı etkileşimini başlatır.
     */
    public void start() {
        int choice;
        do {
            displayMenu();
            choice = readIntInput("Seçiminizi yapın: ");

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    deleteBook();
                    break;
                case 3:
                    listBooks();
                    break;
                case 4:
                    searchBook();
                    break;
                case 5:
                    updateBook();
                    break;
                case 0:
                    System.out.println("Uygulamadan çıkılıyor...");
                    break;
                default:
                    System.out.println("Geçersiz seçim. Lütfen tekrar deneyin.");
            }
            System.out.println(); // Menü seçenekleri arasına boşluk ekle
        } while (choice != 0);

        scanner.close(); // Uygulama bittiğinde scanner'ı kapat
    }

    /**
     * Ana menüyü konsola yazdırır.
     */
    private void displayMenu() {
        System.out.println("===== Kütüphane Yönetim Sistemi =====");
        System.out.println("1. Kitap Ekle");
        System.out.println("2. Kitap Sil");
        System.out.println("3. Kitapları Listele");
        System.out.println("4. Kitap Ara");
        System.out.println("5. Kitap Güncelle");
        System.out.println("0. Çıkış");
        System.out.println("=====================================");
    }

    /**
     * Kullanıcıdan kitap bilgilerini alarak kütüphaneye yeni bir kitap ekler.
     */
    private void addBook() {
        System.out.println("--- Yeni Kitap Ekle ---");
        String title = readStringInput("Başlık: ");
        String author = readStringInput("Yazar: ");
        int publicationYear = readIntInput("Yayın Yılı: ");
        String isbn = readStringInput("ISBN: ");

        try {
            library.addBook(title, author, publicationYear, isbn);
            System.out.println("Kitap başarıyla eklendi.");
        } catch (DuplicateIsbnException e) {
            System.err.println("Hata: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Geçersiz kitap bilgisi: " + e.getMessage());
            logger.warn("Kitap ekleme sırasında geçersiz veri: {}", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Kitap eklenirken beklenmedik bir hata oluştu: " + e.getMessage());
            logger.error("Kitap ekleme sırasında beklenmedik hata", e);
        }
    }

    /**
     * Kullanıcıdan alınan ISBN veya ID ile kütüphaneden bir kitap siler.
     */
    private void deleteBook() {
        System.out.println("--- Kitap Sil ---");
        System.out.println("1. ISBN ile Sil");
        System.out.println("2. ID ile Sil");
        int choice = readIntInput("Seçiminiz: ");

        try {
            if (choice == 1) {
                String isbn = readStringInput("Silinecek kitabın ISBN'i: ");
                library.deleteBookByIsbn(isbn);
                System.out.println("Kitap (ISBN: " + isbn + ") başarıyla silindi.");
            } else if (choice == 2) {
                long id = readLongInput("Silinecek kitabın ID'si: ");
                library.deleteBookById(id);
                 System.out.println("Kitap (ID: " + id + ") başarıyla silindi.");
            } else {
                System.out.println("Geçersiz seçim.");
            }
        } catch (BookNotFoundException e) {
            System.err.println("Hata: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Geçersiz girdi: " + e.getMessage());
            logger.warn("Kitap silme sırasında geçersiz girdi: {}", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Kitap silinirken beklenmedik bir hata oluştu: " + e.getMessage());
            logger.error("Kitap silme sırasında beklenmedik hata", e);
        }
    }

    /**
     * Kütüphanedeki kitapları listeler (filtreleme ve sıralama seçenekleriyle).
     */
    private void listBooks() {
        System.out.println("--- Kitapları Listele ---");
        System.out.println("Listeleme Seçenekleri:");
        System.out.println("1. Tüm Kitaplar");
        System.out.println("2. Filtrele (alan:değer)");
        int listChoice = readIntInput("Seçiminiz: ");

        List<Book> booksToList;

        if (listChoice == 1) {
            booksToList = library.listAllBooks();
        } else if (listChoice == 2) {
            String filter = readStringInput("Filtre (örn: author:Orwell, year:1984, status:AVAILABLE): ");
            booksToList = library.filterBooks(filter);
        } else {
            System.out.println("Geçersiz listeleme seçimi. Tüm kitaplar listeleniyor.");
            booksToList = library.listAllBooks();
        }

        if (booksToList.isEmpty()) {
            System.out.println("Listelenecek kitap bulunamadı.");
            return;
        }

        // Sıralama seçeneği
        System.out.println("Sıralama Seçenekleri (Boş bırakırsanız ID'ye göre artan):");
        System.out.println("Alanlar: title, author, year, isbn, id");
        String sortBy = readStringInput("Sıralama alanı: ");
        if (!sortBy.trim().isEmpty()) {
             boolean ascending = readYesNoInput("Artan sırada mı (E/H)? ");
             booksToList = library.sortBooks(sortBy, ascending);
        } else {
             // Varsayılan sıralama (ID'ye göre artan)
             booksToList = library.sortBooks("id", true);
        }


        System.out.println("--- Kitap Listesi ---");
        if (booksToList.isEmpty()) {
            System.out.println("Belirtilen kriterlere uygun kitap bulunamadı.");
        } else {
            for (Book book : booksToList) {
                System.out.println(book); // Book sınıfının toString() metodu kullanılır
            }
        }
         System.out.println("Toplam " + booksToList.size() + " kitap listelendi.");
    }

    /**
     * Kullanıcıdan alınan sorgu ile kitapları arar (başlık, yazar, ISBN).
     */
    private void searchBook() {
        System.out.println("--- Kitap Ara ---");
        String query = readStringInput("Arama sorgusu (Başlık, Yazar veya ISBN): ");
        List<Book> foundBooks = library.searchBooks(query);

        System.out.println("--- Arama Sonuçları ---");
        if (foundBooks.isEmpty()) {
            System.out.println("'" + query + "' ile eşleşen kitap bulunamadı.");
        } else {
            for (Book book : foundBooks) {
                System.out.println(book);
            }
             System.out.println("Toplam " + foundBooks.size() + " kitap bulundu.");
        }
    }

    /**
     * Kullanıcıdan alınan ISBN ile bir kitabın bilgilerini günceller.
     */
    private void updateBook() {
        System.out.println("--- Kitap Güncelle ---");
        String isbn = readStringInput("Güncellenecek kitabın ISBN'i: ");

        Optional<Book> bookOptional = library.findBookByIsbn(isbn);
        if (bookOptional.isEmpty()) {
            System.err.println("Hata: Bu ISBN numarasıyla kitap bulunamadı: " + isbn);
            return;
        }

        Book currentBook = bookOptional.get();
        System.out.println("Mevcut Bilgiler: " + currentBook);

        System.out.println("Yeni bilgileri girin (Değiştirmek istemiyorsanız boş bırakın):");
        String newTitle = readStringInput("Yeni Başlık [" + currentBook.getTitle() + "]: ");
        String newAuthor = readStringInput("Yeni Yazar [" + currentBook.getAuthor() + "]: ");
        String yearStr = readStringInput("Yeni Yayın Yılı [" + currentBook.getPublicationYear() + "]: ");
        String statusStr = readStringInput("Yeni Durum (AVAILABLE/BORROWED) [" + currentBook.getStatus() + "]: ");


        Integer newPublicationYear = null;
        if (!yearStr.isBlank()) {
            try {
                newPublicationYear = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz yıl formatı. Yıl güncellenmeyecek.");
            }
        }

        BookStatus newStatus = null;
         if (!statusStr.isBlank()) {
            try {
                newStatus = BookStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                 System.out.println("Geçersiz durum formatı (AVAILABLE veya BORROWED girin). Durum güncellenmeyecek.");
            }
        }


        try {
            // Boş bırakılan alanlar için null gönderilir, Library sınıfı bunları işlemez.
            library.updateBook(isbn,
                    newTitle.isBlank() ? null : newTitle,
                    newAuthor.isBlank() ? null : newAuthor,
                    newPublicationYear,
                    newStatus);
            System.out.println("Kitap başarıyla güncellendi.");
        } catch (BookNotFoundException e) {
            // Bu durum yukarıda kontrol edildi ama yine de ekleyelim.
            System.err.println("Hata: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Geçersiz kitap bilgisi: " + e.getMessage());
            logger.warn("Kitap güncelleme sırasında geçersiz veri: {}", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Kitap güncellenirken beklenmedik bir hata oluştu: " + e.getMessage());
            logger.error("Kitap güncelleme sırasında beklenmedik hata", e);
        }
    }


    // --- Yardımcı Girdi Metotları ---

    /**
     * Kullanıcıdan bir String girdi okur.
     * @param prompt Kullanıcıya gösterilecek mesaj.
     * @return Kullanıcının girdiği String.
     */
    private String readStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Kullanıcıdan bir integer girdi okur ve doğrular.
     * @param prompt Kullanıcıya gösterilecek mesaj.
     * @return Kullanıcının girdiği integer.
     */
    private int readIntInput(String prompt) {
        int input = -1;
        boolean validInput = false;
        while (!validInput) {
            System.out.print(prompt);
            try {
                input = scanner.nextInt();
                validInput = true;
            } catch (InputMismatchException e) {
                System.err.println("Geçersiz giriş. Lütfen bir sayı girin.");
            } finally {
                 scanner.nextLine(); // new line karakterini tüket
            }
        }
        return input;
    }

     /**
     * Kullanıcıdan bir long girdi okur ve doğrular.
     * @param prompt Kullanıcıya gösterilecek mesaj.
     * @return Kullanıcının girdiği long.
     */
    private long readLongInput(String prompt) {
        long input = -1L;
        boolean validInput = false;
        while (!validInput) {
            System.out.print(prompt);
            try {
                input = scanner.nextLong();
                validInput = true;
            } catch (InputMismatchException e) {
                System.err.println("Geçersiz giriş. Lütfen bir sayı girin.");
            } finally {
                 scanner.nextLine(); // new line karakterini tüket
            }
        }
        return input;
    }

     /**
     * Kullanıcıdan Evet/Hayır (E/H) girdisi okur.
     * @param prompt Kullanıcıya gösterilecek mesaj.
     * @return Evet ise true, Hayır ise false.
     */
    private boolean readYesNoInput(String prompt) {
        while (true) {
            String input = readStringInput(prompt).trim().toUpperCase();
            if (input.equals("E")) {
                return true;
            } else if (input.equals("H")) {
                return false;
            } else {
                System.out.println("Geçersiz giriş. Lütfen 'E' (Evet) veya 'H' (Hayır) girin.");
            }
        }
    }

}
