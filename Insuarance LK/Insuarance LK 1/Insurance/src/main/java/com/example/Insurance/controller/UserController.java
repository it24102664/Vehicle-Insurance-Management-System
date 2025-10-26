package com.example.Insurance.controller;

import com.example.Insurance.model.User;
import com.example.Insurance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            User savedUser = userService.createUser(user);
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", savedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();

        String email = loginData.get("email");
        String password = loginData.get("password");

        boolean isAuthenticated = userService.authenticateUser(email, password);

        if (isAuthenticated) {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("role", "USER");
                response.put("user", user);
            }
        } else {
            response.put("success", false);
            response.put("message", "Invalid email or password");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            response.put("success", true);
            response.put("user", userOpt.get());
        } else {
            response.put("success", false);
            response.put("message", "User not found");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Load the existing user object by email
            Optional<User> existingOpt = userService.findByEmail(user.getEmail());
            if (!existingOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }
            User existingUser = existingOpt.get();

            // Preserve password, role, enabled status if not provided
            user.setPassword(existingUser.getPassword());
            user.setRole(existingUser.getRole());
            user.setEnabled(existingUser.isEnabled());
            user.setId(existingUser.getId()); // Ensure correct ID is used

            User updatedUser = userService.updateUser(user);
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", updatedUser);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> passwordData) {
        Map<String, Object> response = new HashMap<>();

        String email = passwordData.get("email");
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        try {
            boolean success = userService.changePassword(email, oldPassword, newPassword);
            if (success) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
            } else {
                response.put("success", false);
                response.put("message", "Invalid current password");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}

