package com.librarysystem.storage;

import com.librarysystem.model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Testler için bellekte çalışan basit bir DataStorage implementasyonu.
 * Dosya I/O işlemi yapmaz.
 */
public class InMemoryDataStorage implements DataStorage {

    // Thread-safe liste kullanalım, testler paralel çalıştırılırsa sorun olmasın.
    private final List<Book> books = new CopyOnWriteArrayList<>();

    @Override
    public void saveBooks(List<Book> booksToSave) {
        // Gelen listeyi kopyalayarak sakla
        this.books.clear();
        this.books.addAll(new ArrayList<>(booksToSave)); // Gelen listenin kopyasını al
        System.out.println("[Test Storage] " + booksToSave.size() + " kitap kaydedildi (bellekte).");
    }

    @Override
    public List<Book> loadBooks() {
        System.out.println("[Test Storage] " + books.size() + " kitap yüklendi (bellekten).");
        // Saklanan listenin bir kopyasını döndür
        return new ArrayList<>(this.books);
    }

    // Testler arasında temizlik yapmak için yardımcı metot
    public void clear() {
        this.books.clear();
    }

    // Testlerde doğrulama için yardımcı metot
    public List<Book> getBooksDirectly() {
        return new ArrayList<>(this.books);
    }
}
