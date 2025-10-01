-- Initial database schema for Library Management System
-- Creates the books table with proper constraints and indexes

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publication_year INTEGER NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_status ON books(status);

-- Add check constraint for status
ALTER TABLE books ADD CONSTRAINT chk_books_status 
    CHECK (status IN ('AVAILABLE', 'BORROWED'));

-- Add check constraint for publication year (reasonable range)
ALTER TABLE books ADD CONSTRAINT chk_books_year 
    CHECK (publication_year > 0 AND publication_year <= YEAR(CURRENT_DATE));