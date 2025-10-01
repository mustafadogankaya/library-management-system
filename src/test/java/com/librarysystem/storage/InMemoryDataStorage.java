package com.librarysystem.storage;

import com.librarysystem.model.Book;
import com.librarysystem.model.User;
import com.librarysystem.model.BorrowRecord;
import com.librarysystem.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Testler için bellekte çalışan basit bir DataStorage implementasyonu.
 * Dosya I/O işlemi yapmaz.
 */
public class InMemoryDataStorage implements DataStorage {

    // Thread-safe listeler kullanalım
    private final List<Book> books = new CopyOnWriteArrayList<>();
    private final List<User> users = new CopyOnWriteArrayList<>();
    private final List<BorrowRecord> borrowRecords = new CopyOnWriteArrayList<>();
    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();

    @Override
    public void saveBooks(List<Book> booksToSave) {
        this.books.clear();
        this.books.addAll(new ArrayList<>(booksToSave));
        System.out.println("[Test Storage] " + booksToSave.size() + " kitap kaydedildi (bellekte).");
    }

    @Override
    public List<Book> loadBooks() {
        System.out.println("[Test Storage] " + books.size() + " kitap yüklendi (bellekten).");
        return new ArrayList<>(this.books);
    }

    @Override
    public void saveUsers(List<User> usersToSave) {
        this.users.clear();
        this.users.addAll(new ArrayList<>(usersToSave));
        System.out.println("[Test Storage] " + usersToSave.size() + " kullanıcı kaydedildi (bellekte).");
    }

    @Override
    public List<User> loadUsers() {
        System.out.println("[Test Storage] " + users.size() + " kullanıcı yüklendi (bellekten).");
        return new ArrayList<>(this.users);
    }

    @Override
    public void saveBorrowRecords(List<BorrowRecord> borrowRecordsToSave) {
        this.borrowRecords.clear();
        this.borrowRecords.addAll(new ArrayList<>(borrowRecordsToSave));
        System.out.println("[Test Storage] " + borrowRecordsToSave.size() + " ödünç kaydı kaydedildi (bellekte).");
    }

    @Override
    public List<BorrowRecord> loadBorrowRecords() {
        System.out.println("[Test Storage] " + borrowRecords.size() + " ödünç kaydı yüklendi (bellekten).");
        return new ArrayList<>(this.borrowRecords);
    }

    @Override
    public void saveReservations(List<Reservation> reservationsToSave) {
        this.reservations.clear();
        this.reservations.addAll(new ArrayList<>(reservationsToSave));
        System.out.println("[Test Storage] " + reservationsToSave.size() + " rezervasyon kaydedildi (bellekte).");
    }

    @Override
    public List<Reservation> loadReservations() {
        System.out.println("[Test Storage] " + reservations.size() + " rezervasyon yüklendi (bellekten).");
        return new ArrayList<>(this.reservations);
    }

    // Testler arasında temizlik yapmak için yardımcı metotlar
    public void clear() {
        this.books.clear();
        this.users.clear();
        this.borrowRecords.clear();
        this.reservations.clear();
    }

    // Testlerde doğrulama için yardımcı metotlar
    public List<Book> getBooksDirectly() {
        return new ArrayList<>(this.books);
    }

    public List<User> getUsersDirectly() {
        return new ArrayList<>(this.users);
    }

    public List<BorrowRecord> getBorrowRecordsDirectly() {
        return new ArrayList<>(this.borrowRecords);
    }

    public List<Reservation> getReservationsDirectly() {
        return new ArrayList<>(this.reservations);
    }
}
