package com.librarysystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kütüphane kullanıcısını temsil eden sınıf.
 */
public class User {
    private static final AtomicLong idCounter = new AtomicLong();

    private long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    @JsonIgnore
    private String password; // Şifre JSON'da gözükmemeli
    private Role role;
    private LocalDateTime registrationDate;
    private boolean active;

    // Jackson için varsayılan constructor
    public User() {
        this.role = Role.MEMBER; // Varsayılan rol
        this.registrationDate = LocalDateTime.now();
        this.active = true;
    }

    public User(String name, String email, String phone, String address, String password) {
        this();
        this.id = idCounter.incrementAndGet();
        setName(name);
        setEmail(email);
        setPhone(phone);
        this.address = address;
        setPassword(password);
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    // Setters with validation
    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı boş olamaz");
        }
        this.name = name.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-posta boş olamaz");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Geçerli bir e-posta adresi giriniz");
        }
        this.email = email.trim().toLowerCase();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefon numarası boş olamaz");
        }
        this.phone = phone.trim();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalıdır");
        }
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        User user = (User) obj;
        return id == user.id && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
