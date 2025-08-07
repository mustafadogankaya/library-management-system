package com.librarysystem.controller;

import com.librarysystem.dto.JwtResponse;
import com.librarysystem.dto.LoginRequest;
import com.librarysystem.dto.RegisterRequest;
import com.librarysystem.model.User;
import com.librarysystem.security.JwtTokenUtil;
import com.librarysystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller handling user registration, login, and JWT token management.
 * Implements secure authentication with proper input validation and error handling.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    /**
     * User login endpoint with secure authentication.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            // Load user details
            final UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);

            // Update login tracking
            userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

            // Get user info for response
            User user = (User) userDetails;
            
            // Return JWT response
            return ResponseEntity.ok(new JwtResponse(
                token, 
                user.getUsername(), 
                user.getEmail(), 
                user.getFullName(), 
                user.getRole().name()
            ));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Account is disabled"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Authentication failed"));
        }
    }

    /**
     * User registration endpoint with comprehensive validation.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Username is already taken"));
            }

            // Check if email exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Email is already registered"));
            }

            // Register new user
            User newUser = userService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFullName()
            );

            // Generate JWT token for immediate login
            final UserDetails userDetails = userService.loadUserByUsername(newUser.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponse(
                    token,
                    newUser.getUsername(),
                    newUser.getEmail(),
                    newUser.getFullName(),
                    newUser.getRole().name()
                ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Registration failed"));
        }
    }

    /**
     * Token validation endpoint.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtTokenUtil.isTokenValid(jwtToken)) {
                    String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                    return ResponseEntity.ok(createSuccessResponse("Token is valid", "username", username));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Token validation failed"));
        }
    }

    /**
     * Get current user information.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                
                User user = userService.findByUsername(username).orElse(null);
                if (user != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("username", user.getUsername());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("fullName", user.getFullName());
                    userInfo.put("role", user.getRole().name());
                    userInfo.put("enabled", user.isEnabled());
                    
                    return ResponseEntity.ok(userInfo);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Failed to get user information"));
        }
    }

    /**
     * Helper method to create standardized error responses.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Helper method to create standardized success responses.
     */
    private Map<String, Object> createSuccessResponse(String message, String key, Object value) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put(key, value);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}