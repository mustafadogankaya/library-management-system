package com.librarysystem.service;

import com.librarysystem.model.*;
import com.librarysystem.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kitap ödünç alma/iade ve rezervasyon işlemleri için servis sınıfı.
 */
@Service
public class BorrowService {
    
    private final DataStorage dataStorage;
    private final Library library;
    private final UserService userService;
    private final ConcurrentHashMap<Long, BorrowRecord> borrowRecordsById;
    private final ConcurrentHashMap<Long, Reservation> reservationsById;

    // Varsayılan ödünç alma süresi (gün)
    private static final int DEFAULT_BORROW_DURATION = 14;
    // Varsayılan rezervasyon süresi (gün) 
    private static final int DEFAULT_RESERVATION_DURATION = 7;
    // Günlük ceza miktarı
    private static final double DAILY_FINE_AMOUNT = 1.0;

    @Autowired
    public BorrowService(DataStorage dataStorage, Library library, UserService userService) {
        this.dataStorage = dataStorage;
        this.library = library;
        this.userService = userService;
        this.borrowRecordsById = new ConcurrentHashMap<>();
        this.reservationsById = new ConcurrentHashMap<>();
        loadBorrowRecords();
        loadReservations();
    }

    /**
     * Ödünç alma kayıtlarını yükler.
     */
    private void loadBorrowRecords() {
        List<BorrowRecord> borrowRecords = dataStorage.loadBorrowRecords();
        borrowRecordsById.clear();
        
        long maxId = 0;
        for (BorrowRecord record : borrowRecords) {
            borrowRecordsById.put(record.getId(), record);
            if (record.getId() > maxId) {
                maxId = record.getId();
            }
        }
        
        BorrowRecord.syncIdCounter(maxId);
        System.out.println(borrowRecords.size() + " ödünç alma kaydı yüklendi.");
    }

    /**
     * Rezervasyon kayıtlarını yükler.
     */
    private void loadReservations() {
        List<Reservation> reservations = dataStorage.loadReservations();
        reservationsById.clear();
        
        long maxId = 0;
        for (Reservation reservation : reservations) {
            reservationsById.put(reservation.getId(), reservation);
            if (reservation.getId() > maxId) {
                maxId = reservation.getId();
            }
        }
        
        Reservation.syncIdCounter(maxId);
        System.out.println(reservations.size() + " rezervasyon kaydı yüklendi.");
    }

    /**
     * Kitap ödünç alma işlemi.
     */
    public BorrowRecord borrowBook(long userId, long bookId) throws IllegalArgumentException {
        return borrowBook(userId, bookId, DEFAULT_BORROW_DURATION);
    }

    /**
     * Belirli süreyle kitap ödünç alma işlemi.
     */
    public BorrowRecord borrowBook(long userId, long bookId, int borrowDurationDays) throws IllegalArgumentException {
        // Kullanıcı kontrolü
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            throw new IllegalArgumentException("Geçersiz veya pasif kullanıcı");
        }

        // Kitap kontrolü
        Optional<Book> bookOpt = library.findBookById(bookId);
        if (bookOpt.isEmpty()) {
            throw new IllegalArgumentException("Kitap bulunamadı");
        }

        Book book = bookOpt.get();
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("Kitap mevcut değil (ödünç verilmiş veya rezerve edilmiş)");
        }

        // Kullanıcının aynı kitabı zaten ödünç almış olup olmadığını kontrol et
        boolean alreadyBorrowed = borrowRecordsById.values().stream()
                .anyMatch(record -> record.getUserId() == userId && 
                         record.getBookId() == bookId && 
                         !record.isReturned());
        
        if (alreadyBorrowed) {
            throw new IllegalArgumentException("Bu kitap zaten tarafınızdan ödünç alınmış");
        }

        // Kitabın rezerve edilip edilmediğini ve rezervasyonun bu kullanıcının olup olmadığını kontrol et
        Optional<Reservation> activeReservation = getActiveReservationForBook(bookId);
        if (activeReservation.isPresent() && activeReservation.get().getUserId() != userId) {
            throw new IllegalArgumentException("Bu kitap başka bir kullanıcı tarafından rezerve edilmiş");
        }

        // Ödünç alma kaydı oluştur
        BorrowRecord borrowRecord = new BorrowRecord(userId, bookId, borrowDurationDays);
        borrowRecordsById.put(borrowRecord.getId(), borrowRecord);
        
        // Kitap durumunu güncelle
        book.setStatus(BookStatus.BORROWED);
        try {
            library.updateBook(book.getIsbn(), null, null, 0, BookStatus.BORROWED);
        } catch (Exception e) {
            // Güncelleme başarısız olursa geri al
            borrowRecordsById.remove(borrowRecord.getId());
            throw new IllegalArgumentException("Kitap durumu güncellenirken hata oluştu: " + e.getMessage());
        }
        
        // Eğer rezervasyon varsa yerine getirildi olarak işaretle
        activeReservation.ifPresent(reservation -> {
            reservation.fulfill();
            saveReservations();
        });
        
        saveBorrowRecords();
        return borrowRecord;
    }

    /**
     * Kitap iade işlemi.
     */
    public BorrowRecord returnBook(long borrowRecordId) throws IllegalArgumentException {
        BorrowRecord borrowRecord = borrowRecordsById.get(borrowRecordId);
        if (borrowRecord == null) {
            throw new IllegalArgumentException("Ödünç alma kaydı bulunamadı");
        }

        if (borrowRecord.isReturned()) {
            throw new IllegalArgumentException("Bu kitap zaten iade edilmiş");
        }

        // Kitabı iade et
        borrowRecord.returnBook();
        
        // Gecikme cezası hesapla
        long overdueDays = borrowRecord.getOverdueDays();
        if (overdueDays > 0) {
            borrowRecord.setFineAmount(overdueDays * DAILY_FINE_AMOUNT);
        }

        // Kitap durumunu güncelle
        Optional<Book> bookOpt = library.findBookById(borrowRecord.getBookId());
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            // Kitabın rezervasyonu var mı kontrol et
            Optional<Reservation> nextReservation = getNextActiveReservationForBook(book.getId());
            BookStatus newStatus = nextReservation.isPresent() ? BookStatus.RESERVED : BookStatus.AVAILABLE;
            
            book.setStatus(newStatus);
            try {
                library.updateBook(book.getIsbn(), null, null, 0, newStatus);
            } catch (Exception e) {
                System.err.println("Kitap durumu güncellenirken hata oluştu: " + e.getMessage());
            }
        }

        saveBorrowRecords();
        return borrowRecord;
    }

    /**
     * Kitap rezerve etme işlemi.
     */
    public Reservation reserveBook(long userId, long bookId) throws IllegalArgumentException {
        return reserveBook(userId, bookId, DEFAULT_RESERVATION_DURATION);
    }

    /**
     * Belirli süreyle kitap rezerve etme işlemi.
     */
    public Reservation reserveBook(long userId, long bookId, int reservationDurationDays) throws IllegalArgumentException {
        // Kullanıcı kontrolü
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            throw new IllegalArgumentException("Geçersiz veya pasif kullanıcı");
        }

        // Kitap kontrolü
        Optional<Book> bookOpt = library.findBookById(bookId);
        if (bookOpt.isEmpty()) {
            throw new IllegalArgumentException("Kitap bulunamadı");
        }

        Book book = bookOpt.get();
        if (book.getStatus() == BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("Kitap mevcut, direkt ödünç alabilirsiniz");
        }

        // Kullanıcının aynı kitap için aktif rezervasyonu var mı kontrol et
        boolean alreadyReserved = reservationsById.values().stream()
                .anyMatch(reservation -> reservation.getUserId() == userId && 
                         reservation.getBookId() == bookId && 
                         reservation.isActive());
        
        if (alreadyReserved) {
            throw new IllegalArgumentException("Bu kitap için zaten aktif rezervasyonunuz var");
        }

        // Rezervasyon oluştur
        Reservation reservation = new Reservation(userId, bookId, reservationDurationDays);
        reservationsById.put(reservation.getId(), reservation);
        
        // Eğer kitap mevcut ise durumunu rezerve olarak güncelle
        if (book.getStatus() != BookStatus.BORROWED) {
            book.setStatus(BookStatus.RESERVED);
            try {
                library.updateBook(book.getIsbn(), null, null, 0, BookStatus.RESERVED);
            } catch (Exception e) {
                System.err.println("Kitap durumu güncellenirken hata oluştu: " + e.getMessage());
            }
        }
        
        saveReservations();
        return reservation;
    }

    /**
     * Rezervasyon iptal etme.
     */
    public void cancelReservation(long reservationId) throws IllegalArgumentException {
        Reservation reservation = reservationsById.get(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Rezervasyon bulunamadı");
        }

        if (!reservation.isActive()) {
            throw new IllegalArgumentException("Rezervasyon zaten iptal edilmiş veya yerine getirilmiş");
        }

        reservation.cancel();
        
        // Kitap durumunu güncelle
        Optional<Book> bookOpt = library.findBookById(reservation.getBookId());
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            // Başka rezervasyon var mı kontrol et
            Optional<Reservation> nextReservation = getNextActiveReservationForBook(book.getId());
            if (nextReservation.isEmpty() && book.getStatus() == BookStatus.RESERVED) {
                book.setStatus(BookStatus.AVAILABLE);
                try {
                    library.updateBook(book.getIsbn(), null, null, 0, BookStatus.AVAILABLE);
                } catch (Exception e) {
                    System.err.println("Kitap durumu güncellenirken hata oluştu: " + e.getMessage());
                }
            }
        }
        
        saveReservations();
    }

    /**
     * Kullanıcının aktif ödünç aldığı kitapları listeler.
     */
    public List<BorrowRecord> getUserActiveBorrows(long userId) {
        return borrowRecordsById.values().stream()
                .filter(record -> record.getUserId() == userId && !record.isReturned())
                .toList();
    }

    /**
     * Kullanıcının tüm ödünç alma geçmişini listeler.
     */
    public List<BorrowRecord> getUserBorrowHistory(long userId) {
        return borrowRecordsById.values().stream()
                .filter(record -> record.getUserId() == userId)
                .toList();
    }

    /**
     * Kullanıcının aktif rezervasyonlarını listeler.
     */
    public List<Reservation> getUserActiveReservations(long userId) {
        return reservationsById.values().stream()
                .filter(reservation -> reservation.getUserId() == userId && reservation.isActive())
                .toList();
    }

    /**
     * Vadesi geçmiş ödünç alma kayıtlarını listeler.
     */
    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecordsById.values().stream()
                .filter(BorrowRecord::isOverdue)
                .toList();
    }

    /**
     * Belirli kitap için aktif rezervasyon bulur.
     */
    private Optional<Reservation> getActiveReservationForBook(long bookId) {
        return reservationsById.values().stream()
                .filter(reservation -> reservation.getBookId() == bookId && reservation.isActive())
                .findFirst();
    }

    /**
     * Belirli kitap için sıradaki aktif rezervasyon bulur.
     */
    private Optional<Reservation> getNextActiveReservationForBook(long bookId) {
        return reservationsById.values().stream()
                .filter(reservation -> reservation.getBookId() == bookId && reservation.isActive())
                .sorted((r1, r2) -> r1.getReservationDate().compareTo(r2.getReservationDate()))
                .findFirst();
    }

    /**
     * Ödünç alma kayıtlarını dosyaya kaydeder.
     */
    private void saveBorrowRecords() {
        dataStorage.saveBorrowRecords(borrowRecordsById.values().stream().toList());
    }

    /**
     * Rezervasyon kayıtlarını dosyaya kaydeder.
     */
    private void saveReservations() {
        dataStorage.saveReservations(reservationsById.values().stream().toList());
    }

    /**
     * Süresi dolan rezervasyonları temizler.
     */
    public void cleanupExpiredReservations() {
        List<Reservation> expiredReservations = reservationsById.values().stream()
                .filter(reservation -> reservation.isActive() && reservation.isExpired())
                .toList();
        
        for (Reservation reservation : expiredReservations) {
            reservation.cancel();
            
            // Kitap durumunu güncelle
            Optional<Book> bookOpt = library.findBookById(reservation.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                Optional<Reservation> nextReservation = getNextActiveReservationForBook(book.getId());
                if (nextReservation.isEmpty() && book.getStatus() == BookStatus.RESERVED) {
                    book.setStatus(BookStatus.AVAILABLE);
                    try {
                        library.updateBook(book.getIsbn(), null, null, 0, BookStatus.AVAILABLE);
                    } catch (Exception e) {
                        System.err.println("Kitap durumu güncellenirken hata oluştu: " + e.getMessage());
                    }
                }
            }
        }
        
        if (!expiredReservations.isEmpty()) {
            saveReservations();
            System.out.println(expiredReservations.size() + " süresi dolan rezervasyon temizlendi.");
        }
    }

    /**
     * İstatistikler için toplam ödünç alma sayısı.
     */
    public int getTotalBorrowCount() {
        return borrowRecordsById.size();
    }

    /**
     * İstatistikler için aktif ödünç alma sayısı.
     */
    public int getActiveBorrowCount() {
        return (int) borrowRecordsById.values().stream()
                .filter(record -> !record.isReturned())
                .count();
    }

    /**
     * İstatistikler için aktif rezervasyon sayısı.
     */
    public int getActiveReservationCount() {
        return (int) reservationsById.values().stream()
                .filter(Reservation::isActive)
                .count();
    }
}