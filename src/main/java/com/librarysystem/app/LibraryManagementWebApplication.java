package com.librarysystem.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// Diğer paketlerdeki @Component, @Service, @Repository, @RestController gibi anotasyonları taraması için:
@ComponentScan(basePackages = "com.librarysystem")
public class LibraryManagementWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementWebApplication.class, args);
    }
}
