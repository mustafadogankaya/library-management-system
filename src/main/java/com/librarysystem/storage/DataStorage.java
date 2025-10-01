package com.librarysystem.storage;

import com.librarysystem.model.Book;
import com.librarysystem.model.User;
import com.librarysystem.model.BorrowRecord;
import com.librarysystem.model.Reservation;

import java.util.List;

/**
 * Veri kalıcılığı için arayüz.
 * Farklı depolama mekanizmaları (JSON, CSV, veritabanı) bu arayüzü uygulayabilir.
 */
public interface DataStorage {

    /**
     * Kitap listesini depolama mekanizmasına kaydeder.
     * @param books Kaydedilecek kitapların listesi.
     */
    void saveBooks(List<Book> books);

    /**
     * Kitap listesini depolama mekanizmasından yükler.
     * @return Yüklenen kitapların listesi. Depolama boşsa veya hata oluşursa boş liste dönebilir.
     */
    List<Book> loadBooks();

    /**
     * Kullanıcı listesini depolama mekanizmasına kaydeder.
     * @param users Kaydedilecek kullanıcıların listesi.
     */
    void saveUsers(List<User> users);

    /**
     * Kullanıcı listesini depolama mekanizmasından yükler.
     * @return Yüklenen kullanıcıların listesi.
     */
    List<User> loadUsers();

    /**
     * Ödünç alma kayıtlarını depolama mekanizmasına kaydeder.
     * @param borrowRecords Kaydedilecek ödünç alma kayıtlarının listesi.
     */
    void saveBorrowRecords(List<BorrowRecord> borrowRecords);

    /**
     * Ödünç alma kayıtlarını depolama mekanizmasından yükler.
     * @return Yüklenen ödünç alma kayıtlarının listesi.
     */
    List<BorrowRecord> loadBorrowRecords();

    /**
     * Rezervasyon kayıtlarını depolama mekanizmasına kaydeder.
     * @param reservations Kaydedilecek rezervasyon kayıtlarının listesi.
     */
    void saveReservations(List<Reservation> reservations);

    /**
     * Rezervasyon kayıtlarını depolama mekanizmasından yükler.
     * @return Yüklenen rezervasyon kayıtlarının listesi.
     */
    List<Reservation> loadReservations();
}
