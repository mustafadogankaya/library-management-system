package com.librarysystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kitap ödünç alma kaydını temsil eden sınıf.
 */
public class BorrowRecord {
    private static final AtomicLong idCounter = new AtomicLong();

    private long id;
    private long userId;
    private long bookId;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private boolean returned;
    private double fineAmount;

    // Jackson için varsayılan constructor
    public BorrowRecord() {
        this.borrowDate = LocalDateTime.now();
        this.dueDate = LocalDate.now().plusDays(14); // Varsayılan 14 gün
        this.returned = false;
        this.fineAmount = 0.0;
    }

    public BorrowRecord(long userId, long bookId) {
        this();
        this.id = idCounter.incrementAndGet();
        this.userId = userId;
        this.bookId = bookId;
    }

    public BorrowRecord(long userId, long bookId, int borrowDurationDays) {
        this();
        this.id = idCounter.incrementAndGet();
        this.userId = userId;
        this.bookId = bookId;
        this.dueDate = LocalDate.now().plusDays(borrowDurationDays);
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

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public double getFineAmount() {
        return fineAmount;
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

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    /**
     * Kitabı iade et
     */
    public void returnBook() {
        this.returned = true;
        this.returnDate = LocalDateTime.now();
    }

    /**
     * Gecikme günü sayısını hesapla
     */
    public long getOverdueDays() {
        if (returned && returnDate != null) {
            return Math.max(0, returnDate.toLocalDate().toEpochDay() - dueDate.toEpochDay());
        } else if (!returned) {
            return Math.max(0, LocalDate.now().toEpochDay() - dueDate.toEpochDay());
        }
        return 0;
    }

    /**
     * Kaydın vadesi geçmiş mi?
     */
    public boolean isOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
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
        BorrowRecord that = (BorrowRecord) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", returned=" + returned +
                ", fineAmount=" + fineAmount +
                '}';
    }
}