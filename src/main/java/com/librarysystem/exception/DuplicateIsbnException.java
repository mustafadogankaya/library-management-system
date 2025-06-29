package com.librarysystem.exception;

/**
 * Aynı ISBN numarasına sahip bir kitap eklenmeye çalışıldığında fırlatılan exception.
 */
public class DuplicateIsbnException extends Exception {
    public DuplicateIsbnException(String message) {
        super(message);
    }
}
