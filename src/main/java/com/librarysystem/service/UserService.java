package com.librarysystem.service;

import com.librarysystem.model.Role;
import com.librarysystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User service implementing Spring Security UserDetailsService.
 * Handles user management, authentication, and security operations.
 */
@Service
public class UserService implements UserDetailsService {

    private final ConcurrentHashMap<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> usersByEmail = new ConcurrentHashMap<>();
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private Validator validator;

    public UserService() {
        // Initialize with a default admin user
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Create default admin user
        User admin = new User("admin", "admin@library.com", "admin123", "System Administrator", Role.ADMIN);
        admin.setPassword(passwordEncoder.encode("admin123")); // Hash the password
        usersByUsername.put(admin.getUsername(), admin);
        usersByEmail.put(admin.getEmail(), admin);
        
        // Create default librarian
        User librarian = new User("librarian", "librarian@library.com", "librarian123", "Library Staff", Role.LIBRARIAN);
        librarian.setPassword(passwordEncoder.encode("librarian123"));
        usersByUsername.put(librarian.getUsername(), librarian);
        usersByEmail.put(librarian.getEmail(), librarian);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = usersByUsername.get(username);
        if (user == null) {
            // Also try to find by email
            user = usersByEmail.get(username);
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        return user;
    }

    /**
     * Register a new user with validation and secure password hashing.
     */
    public User registerUser(String username, String email, String password, String fullName) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        
        // Check if user already exists
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (usersByEmail.containsKey(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User newUser = new User(username, email, password, fullName, Role.USER);
        
        // Validate user object
        Set<ConstraintViolation<User>> violations = validator.validate(newUser);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<User> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException("Validation failed: " + sb.toString());
        }
        
        // Hash password
        newUser.setPassword(passwordEncoder.encode(password));
        
        // Store user
        usersByUsername.put(newUser.getUsername(), newUser);
        usersByEmail.put(newUser.getEmail(), newUser);
        
        return newUser;
    }

    /**
     * Authenticate user and update login tracking.
     */
    public boolean authenticateUser(String username, String password) {
        try {
            User user = (User) loadUserByUsername(username);
            
            if (!user.isAccountNonLocked()) {
                return false; // Account is locked
            }
            
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Successful login
                user.setLastLoginAt(LocalDateTime.now());
                user.resetFailedLoginAttempts();
                return true;
            } else {
                // Failed login
                user.incrementFailedLoginAttempts();
                return false;
            }
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Change user password with validation.
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            User user = (User) loadUserByUsername(username);
            
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return false; // Old password doesn't match
            }
            
            if (newPassword.length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters long");
            }
            
            user.setPassword(passwordEncoder.encode(newPassword));
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get user by username.
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    /**
     * Get user by email.
     */
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    /**
     * Get all users (admin only).
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(usersByUsername.values());
    }

    /**
     * Update user role (admin only).
     */
    public boolean updateUserRole(String username, Role newRole) {
        User user = usersByUsername.get(username);
        if (user != null) {
            user.setRole(newRole);
            return true;
        }
        return false;
    }

    /**
     * Enable/disable user account (admin only).
     */
    public boolean setUserEnabled(String username, boolean enabled) {
        User user = usersByUsername.get(username);
        if (user != null) {
            user.setEnabled(enabled);
            return true;
        }
        return false;
    }

    /**
     * Delete user (admin only).
     */
    public boolean deleteUser(String username) {
        User user = usersByUsername.remove(username);
        if (user != null) {
            usersByEmail.remove(user.getEmail());
            return true;
        }
        return false;
    }

    /**
     * Check if username exists.
     */
    public boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username);
    }

    /**
     * Check if email exists.
     */
    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }
}