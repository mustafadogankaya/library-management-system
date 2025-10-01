package com.librarysystem.controller;

import com.librarysystem.model.User;
import com.librarysystem.model.Role;
import com.librarysystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Kullanıcı kaydı oluşturur
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> userData) {
        try {
            String name = userData.get("name");
            String email = userData.get("email");
            String phone = userData.get("phone");
            String address = userData.get("address");
            String password = userData.get("password");

            if (name == null || email == null || phone == null || password == null) {
                return ResponseEntity.badRequest().body("Tüm zorunlu alanlar doldurulmalıdır");
            }

            User user = userService.registerUser(name, email, phone, address, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı kaydı sırasında hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı girişi
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body("Email ve şifre gereklidir");
            }

            Optional<User> userOpt = userService.authenticate(email, password);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Geçersiz email veya şifre");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Giriş sırasında hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı bilgilerini getirir
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable long id) {
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı bilgileri alınırken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı bilgilerini günceller
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody Map<String, String> userData) {
        try {
            String name = userData.get("name");
            String phone = userData.get("phone");
            String address = userData.get("address");

            User updatedUser = userService.updateUser(id, name, phone, address);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı bilgileri güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Şifre değiştirir
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable long id, @RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Eski ve yeni şifre gereklidir");
            }

            userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok().body("Şifre başarıyla değiştirildi");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Şifre değiştirilirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Tüm kullanıcıları listeler (admin işlemi)
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcılar listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Aktif kullanıcıları listeler
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveUsers() {
        try {
            List<User> users = userService.getActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Aktif kullanıcılar listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Role göre kullanıcıları listeler
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRole(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Geçersiz rol: " + role);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcılar listelenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * İsimde arama yapar
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Arama terimi gereklidir");
            }
            List<User> users = userService.searchByName(name);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı araması sırasında hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı rolünü değiştirir (admin işlemi)
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable long id, @RequestBody Map<String, String> roleData) {
        try {
            String roleStr = roleData.get("role");
            if (roleStr == null) {
                return ResponseEntity.badRequest().body("Rol gereklidir");
            }

            Role role = Role.valueOf(roleStr.toUpperCase());
            userService.changeRole(id, role);
            return ResponseEntity.ok().body("Kullanıcı rolü başarıyla değiştirildi");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı rolü değiştirilirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcıyı aktif/pasif yapar (admin işlemi)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> setUserStatus(@PathVariable long id, @RequestBody Map<String, Boolean> statusData) {
        try {
            Boolean active = statusData.get("active");
            if (active == null) {
                return ResponseEntity.badRequest().body("Aktiflik durumu gereklidir");
            }

            userService.setUserActive(id, active);
            return ResponseEntity.ok().body("Kullanıcı durumu başarıyla değiştirildi");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kullanıcı durumu değiştirilirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı istatistikleri
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = Map.of(
                "totalUsers", userService.getUserCount(),
                "activeUsers", userService.getActiveUserCount(),
                "memberCount", userService.getUsersByRole(Role.MEMBER).size(),
                "librarianCount", userService.getUsersByRole(Role.LIBRARIAN).size(),
                "adminCount", userService.getUsersByRole(Role.ADMIN).size()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("İstatistikler alınırken hata oluştu: " + e.getMessage());
        }
    }

    // Exception handlers
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}