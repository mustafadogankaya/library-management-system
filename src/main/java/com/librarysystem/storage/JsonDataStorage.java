package com.librarysystem.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.librarysystem.model.Book;
import com.librarysystem.model.User;
import com.librarysystem.model.BorrowRecord;
import com.librarysystem.model.Reservation;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonDataStorage implements DataStorage {

    private final String booksFilePath;
    private final String usersFilePath;
    private final String borrowRecordsFilePath;
    private final String reservationsFilePath;
    private final ObjectMapper objectMapper;

    /**
     * Varsayılan constructor - Spring Bean için
     */
    public JsonDataStorage(@Value("${library.data.file:data/library_data.json}") String baseFilePath) {
        // Farklı dosyalar için path'leri oluştur
        File baseFile = new File(baseFilePath);
        String basePath = baseFile.getParent() != null ? baseFile.getParent() : "data";
        
        this.booksFilePath = basePath + "/books.json";
        this.usersFilePath = basePath + "/users.json";
        this.borrowRecordsFilePath = basePath + "/borrow_records.json";
        this.reservationsFilePath = basePath + "/reservations.json";
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Java 8 time desteği
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Dosyaları oluştur
        ensureFileExists(booksFilePath);
        ensureFileExists(usersFilePath);
        ensureFileExists(borrowRecordsFilePath);
        ensureFileExists(reservationsFilePath);
        
        System.out.println("JsonDataStorage başlatıldı:");
        System.out.println("  Kitaplar: " + this.booksFilePath);
        System.out.println("  Kullanıcılar: " + this.usersFilePath);
        System.out.println("  Ödünç Kayıtları: " + this.borrowRecordsFilePath);
        System.out.println("  Rezervasyonlar: " + this.reservationsFilePath);
    }

    @Override
    public void saveBooks(List<Book> books) {
        saveToFile(books, booksFilePath, "kitapları");
    }

    @Override
    public List<Book> loadBooks() {
        return loadFromFile(booksFilePath, new TypeReference<List<Book>>() {}, "kitapları");
    }

    @Override
    public void saveUsers(List<User> users) {
        saveToFile(users, usersFilePath, "kullanıcıları");
    }

    @Override
    public List<User> loadUsers() {
        return loadFromFile(usersFilePath, new TypeReference<List<User>>() {}, "kullanıcıları");
    }

    @Override
    public void saveBorrowRecords(List<BorrowRecord> borrowRecords) {
        saveToFile(borrowRecords, borrowRecordsFilePath, "ödünç kayıtlarını");
    }

    @Override
    public List<BorrowRecord> loadBorrowRecords() {
        return loadFromFile(borrowRecordsFilePath, new TypeReference<List<BorrowRecord>>() {}, "ödünç kayıtlarını");
    }

    @Override
    public void saveReservations(List<Reservation> reservations) {
        saveToFile(reservations, reservationsFilePath, "rezervasyonları");
    }

    @Override
    public List<Reservation> loadReservations() {
        return loadFromFile(reservationsFilePath, new TypeReference<List<Reservation>>() {}, "rezervasyonları");
    }

    /**
     * Generic save method
     */
    private <T> void saveToFile(List<T> data, String filePath, String dataType) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            System.err.println(dataType + " dosyaya kaydederken hata oluştu (" + filePath + "): " + e.getMessage());
        }
    }

    /**
     * Generic load method
     */
    private <T> List<T> loadFromFile(String filePath, TypeReference<List<T>> typeReference, String dataType) {
        try {
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            
            List<T> data = objectMapper.readValue(file, typeReference);
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            System.err.println(dataType + " dosyadan yüklerken hata oluştu (" + filePath + "): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Veri dosyasının var olduğundan emin olur, yoksa oluşturur.
     */
    private void ensureFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                if (file.createNewFile()) {
                    System.out.println("Veri dosyası oluşturuldu: " + filePath);
                    // Yeni oluşturulan dosyaya boş bir JSON dizisi yaz
                    objectMapper.writeValue(file, new ArrayList<>());
                }
            } catch (IOException e) {
                System.err.println("Veri dosyası oluşturulurken hata oluştu (" + filePath + "): " + e.getMessage());
            }
        }
    }
}
