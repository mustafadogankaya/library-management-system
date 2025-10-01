package com.librarysystem.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kitap rezervasyon kaydını temsil eden sınıf.
 */
public class Reservation {
    private static final AtomicLong idCounter = new AtomicLong();

    private long id;
    private long userId;
    private long bookId;
    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;
    private boolean active;
    private boolean fulfilled; // Rezervasyon yerine getirildi mi?

    // Jackson için varsayılan constructor
    public Reservation() {
        this.reservationDate = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusDays(7); // 7 gün geçerli
        this.active = true;
        this.fulfilled = false;
    }

    public Reservation(long userId, long bookId) {
        this();
        this.id = idCounter.incrementAndGet();
        this.userId = userId;
        this.bookId = bookId;
    }

    public Reservation(long userId, long bookId, int expiryDays) {
        this();
        this.id = idCounter.incrementAndGet();
        this.userId = userId;
        this.bookId = bookId;
        this.expiryDate = LocalDateTime.now().plusDays(expiryDays);
    }

    // Getters
    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getBookId() {
        return bookId;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    /**
     * Rezervasyonu iptal et
     */
    public void cancel() {
        this.active = false;
    }

    /**
     * Rezervasyonu yerine getir
     */
    public void fulfill() {
        this.fulfilled = true;
        this.active = false;
    }

    /**
     * Rezervasyon süresi dolmuş mu?
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * ID sayacını mevcut en yüksek ID'ye ayarlar.
     */
    public static void syncIdCounter(long maxId) {
        idCounter.set(maxId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservation that = (Reservation) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", reservationDate=" + reservationDate +
                ", expiryDate=" + expiryDate +
                ", active=" + active +
                ", fulfilled=" + fulfilled +
                '}';
    }
}