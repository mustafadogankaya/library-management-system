package com.librarysystem.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// Diğer paketlerdeki @Component, @Service, @Repository, @RestController gibi anotasyonları taraması için:
@ComponentScan(basePackages = "com.librarysystem")
@EnableJpaRepositories(basePackages = "com.librarysystem.repository")
@EntityScan(basePackages = "com.librarysystem.model")
public class LibraryManagementWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementWebApplication.class, args);
    }
}
