package com.librarysystem.storage;

import com.librarysystem.model.Book;
import java.util.List;

/**
 * Kitap verilerini kalıcı olarak saklamak ve yüklemek için arayüz.
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
}
