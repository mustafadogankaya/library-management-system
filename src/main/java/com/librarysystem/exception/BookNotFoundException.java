package com.librarysystem.exception;

/**
 * Belirtilen ISBN ile kitap bulunamadığında fırlatılan exception.
 */
public class BookNotFoundException extends Exception {
    public BookNotFoundException(String message) {
        super(message);
    }
}
