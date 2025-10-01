package com.librarysystem.service;

import com.librarysystem.model.User;
import com.librarysystem.model.Role;
import com.librarysystem.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kullanıcı yönetimi için servis sınıfı.
 */
@Service
public class UserService {
    
    private final DataStorage dataStorage;
    private final ConcurrentHashMap<String, User> usersByEmail; // Email ile hızlı erişim
    private final ConcurrentHashMap<Long, User> usersById; // ID ile hızlı erişim

    @Autowired
    public UserService(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.usersByEmail = new ConcurrentHashMap<>();
        this.usersById = new ConcurrentHashMap<>();
        loadUsers();
    }

    /**
     * Veritabanından kullanıcıları yükler ve ID sayacını ayarlar.
     */
    private void loadUsers() {
        List<User> users = dataStorage.loadUsers();
        usersByEmail.clear();
        usersById.clear();
        
        long maxId = 0;
        for (User user : users) {
            usersByEmail.put(user.getEmail(), user);
            usersById.put(user.getId(), user);
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        
        // ID sayacını ayarla
        User.syncIdCounter(maxId);
        System.out.println(users.size() + " kullanıcı başarıyla yüklendi.");
    }

    /**
     * Yeni kullanıcı kaydı oluşturur.
     */
    public User registerUser(String name, String email, String phone, String address, String password) throws IllegalArgumentException {
        // Email benzersizliği kontrolü
        if (usersByEmail.containsKey(email.toLowerCase())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanılmaktadır");
        }

        User user = new User(name, email, phone, address, password);
        usersByEmail.put(user.getEmail(), user);
        usersById.put(user.getId(), user);
        saveUsers();
        
        return user;
    }

    /**
     * Kullanıcı bilgilerini günceller.
     */
    public User updateUser(long userId, String name, String phone, String address) {
        User user = usersById.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı");
        }

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone);
        }
        if (address != null) {
            user.setAddress(address);
        }

        saveUsers();
        return user;
    }

    /**
     * Kullanıcının şifresini değiştirir.
     */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        User user = usersById.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı");
        }

        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Eski şifre yanlış");
        }

        user.setPassword(newPassword);
        saveUsers();
    }

    /**
     * Kullanıcının rolünü değiştirir (sadece admin işlemi).
     */
    public void changeRole(long userId, Role newRole) {
        User user = usersById.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı");
        }

        user.setRole(newRole);
        saveUsers();
    }

    /**
     * Kullanıcıyı aktif/pasif yapar.
     */
    public void setUserActive(long userId, boolean active) {
        User user = usersById.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı");
        }

        user.setActive(active);
        saveUsers();
    }

    /**
     * Email ve şifre ile kullanıcı doğrulaması.
     */
    public Optional<User> authenticate(String email, String password) {
        User user = usersByEmail.get(email.toLowerCase());
        if (user != null && user.isActive() && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * ID ile kullanıcı bulur.
     */
    public Optional<User> findById(long id) {
        return Optional.ofNullable(usersById.get(id));
    }

    /**
     * Email ile kullanıcı bulur.
     */
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
    }

    /**
     * Tüm kullanıcıları listeler.
     */
    public List<User> getAllUsers() {
        return usersById.values().stream().toList();
    }

    /**
     * Aktif kullanıcıları listeler.
     */
    public List<User> getActiveUsers() {
        return usersById.values().stream()
                .filter(User::isActive)
                .toList();
    }

    /**
     * Belirli role sahip kullanıcıları listeler.
     */
    public List<User> getUsersByRole(Role role) {
        return usersById.values().stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    /**
     * İsimde arama yapar.
     */
    public List<User> searchByName(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return usersById.values().stream()
                .filter(user -> user.getName().toLowerCase().contains(lowerSearchTerm))
                .toList();
    }

    /**
     * Kullanıcı sayısını döndürür.
     */
    public int getUserCount() {
        return usersById.size();
    }

    /**
     * Aktif kullanıcı sayısını döndürür.
     */
    public int getActiveUserCount() {
        return (int) usersById.values().stream()
                .filter(User::isActive)
                .count();
    }

    /**
     * Kullanıcıları dosyaya kaydeder.
     */
    private void saveUsers() {
        dataStorage.saveUsers(usersById.values().stream().toList());
    }
}
