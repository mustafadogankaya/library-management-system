package com.librarysystem.controller;

import com.librarysystem.model.BorrowRecord;
import com.librarysystem.model.Reservation;
import com.librarysystem.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    @Autowired
    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * Kitap ödünç alma
     */
    @PostMapping("/books/{bookId}/borrow")
    public ResponseEntity<?> borrowBook(@PathVariable long bookId, @RequestBody Map<String, Object> borrowData) {
        try {
            Long userId = ((Number) borrowData.get("userId")).longValue();
            Integer duration = (Integer) borrowData.get("duration");

            if (userId == null) {
                return ResponseEntity.badRequest().body("Kullanıcı ID gereklidir");
            }

            BorrowRecord borrowRecord;
            if (duration != null && duration > 0) {
                borrowRecord = borrowService.borrowBook(userId, bookId, duration);
            } else {
                borrowRecord = borrowService.borrowBook(userId, bookId);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(borrowRecord);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kitap ödünç alınırken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kitap iade etme
     */
    @PutMapping("/records/{recordId}/return")
    public ResponseEntity<?> returnBook(@PathVariable long recordId) {
        try {
            BorrowRecord borrowRecord = borrowService.returnBook(recordId);
            return ResponseEntity.ok(borrowRecord);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kitap iade edilirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kitap rezerve etme
     */
    @PostMapping("/books/{bookId}/reserve")
    public ResponseEntity<?> reserveBook(@PathVariable long bookId, @RequestBody Map<String, Object> reservationData) {
        try {
            Long userId = ((Number) reservationData.get("userId")).longValue();
            Integer duration = (Integer) reservationData.get("duration");

            if (userId == null) {
                return ResponseEntity.badRequest().body("Kullanıcı ID gereklidir");
            }

            Reservation reservation;
            if (duration != null && duration > 0) {
                reservation = borrowService.reserveBook(userId, bookId, duration);
            } else {
                reservation = borrowService.reserveBook(userId, bookId);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kitap rezerve edilirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Rezervasyon iptal etme
     */
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable long reservationId) {
        try {
            borrowService.cancelReservation(reservationId);
            return ResponseEntity.ok().body("Rezervasyon başarıyla iptal edildi");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Rezervasyon iptal edilirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcının aktif ödünç aldığı kitapları listeler
     */
    @GetMapping("/users/{userId}/active-borrows")
    public ResponseEntity<?> getUserActiveBorrows(@PathVariable long userId) {
        try {
            List<BorrowRecord> activeBorrows = borrowService.getUserActiveBorrows(userId);
            return ResponseEntity.ok(activeBorrows);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Aktif ödünç alımlar listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcının ödünç alma geçmişini listeler
     */
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<?> getUserBorrowHistory(@PathVariable long userId) {
        try {
            List<BorrowRecord> borrowHistory = borrowService.getUserBorrowHistory(userId);
            return ResponseEntity.ok(borrowHistory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ödünç alma geçmişi listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcının aktif rezervasyonlarını listeler
     */
    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<?> getUserActiveReservations(@PathVariable long userId) {
        try {
            List<Reservation> activeReservations = borrowService.getUserActiveReservations(userId);
            return ResponseEntity.ok(activeReservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Aktif rezervasyonlar listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Vadesi geçmiş ödünç alma kayıtlarını listeler
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueRecords() {
        try {
            List<BorrowRecord> overdueRecords = borrowService.getOverdueRecords();
            return ResponseEntity.ok(overdueRecords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Vadesi geçmiş kayıtlar listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Süresi dolan rezervasyonları temizler
     */
    @PostMapping("/cleanup-expired-reservations")
    public ResponseEntity<?> cleanupExpiredReservations() {
        try {
            borrowService.cleanupExpiredReservations();
            return ResponseEntity.ok().body("Süresi dolan rezervasyonlar temizlendi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Rezervasyon temizliği sırasında hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Ödünç alma ve rezervasyon istatistikleri
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getBorrowStats() {
        try {
            Map<String, Object> stats = Map.of(
                "totalBorrows", borrowService.getTotalBorrowCount(),
                "activeBorrows", borrowService.getActiveBorrowCount(),
                "activeReservations", borrowService.getActiveReservationCount(),
                "overdueCount", borrowService.getOverdueRecords().size()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("İstatistikler alınırken hata oluştu: " + e.getMessage());
        }
    }

    // Exception handlers
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNumberFormat(NumberFormatException ex) {
        return ResponseEntity.badRequest().body("Geçersiz sayı formatı");
    }
}