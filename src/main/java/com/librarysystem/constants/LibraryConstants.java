package com.librarysystem.constants;

/**
 * Application constants for the Library Management System.
 * Centralizes magic numbers, strings, and other reusable values.
 */
public final class LibraryConstants {

    private LibraryConstants() {
        // Private constructor to prevent instantiation
    }

    // Error Messages
    public static final String ERROR_BOOK_NOT_FOUND = "Kitap bulunamadı";
    public static final String ERROR_DUPLICATE_ISBN = "Bu ISBN numarasıyla zaten bir kitap mevcut";
    public static final String ERROR_INVALID_ISBN = "Geçersiz ISBN formatı";
    public static final String ERROR_INVALID_YEAR = "Geçersiz yıl formatı";
    public static final String ERROR_INVALID_STATUS = "Geçersiz durum formatı";
    public static final String ERROR_INVALID_FILTER_FORMAT = "Geçersiz filtre formatı. Format: alan:değer (örnek: author:Orwell)";
    public static final String ERROR_INVALID_SORT_FIELD = "Geçersiz sıralama alanı";

    // Success Messages
    public static final String SUCCESS_BOOK_ADDED = "Kitap başarıyla eklendi";
    public static final String SUCCESS_BOOK_UPDATED = "Kitap başarıyla güncellendi";
    public static final String SUCCESS_BOOK_DELETED = "Kitap başarıyla silindi";

    // Filter Fields
    public static final String FILTER_FIELD_TITLE = "title";
    public static final String FILTER_FIELD_AUTHOR = "author";
    public static final String FILTER_FIELD_YEAR = "year";
    public static final String FILTER_FIELD_STATUS = "status";
    public static final String FILTER_FIELD_ISBN = "isbn";

    // Sort Fields
    public static final String SORT_FIELD_TITLE = "title";
    public static final String SORT_FIELD_AUTHOR = "author";
    public static final String SORT_FIELD_YEAR = "year";
    public static final String SORT_FIELD_ISBN = "isbn";
    public static final String SORT_FIELD_ID = "id";

    // Other Constants
    public static final String FILTER_DELIMITER = ":";
    public static final int FILTER_PARTS_COUNT = 2;
    public static final int MIN_PUBLICATION_YEAR = 1;
}