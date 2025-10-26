package com.example.Insurance.controller;

import com.example.Insurance.model.Admin;
import com.example.Insurance.model.User;
import com.example.Insurance.service.AdminService;
import com.example.Insurance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerAdmin(@RequestBody Admin admin) {
        Map<String, Object> response = new HashMap<>();

        try {
            Admin savedAdmin = adminService.createAdmin(admin);
            response.put("success", true);
            response.put("message", "Admin registered successfully");
            response.put("admin", savedAdmin);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();

        String email = loginData.get("email");
        String password = loginData.get("password");

        boolean isAuthenticated = adminService.authenticateAdmin(email, password);

        if (isAuthenticated) {
            Optional<Admin> adminOpt = adminService.findByEmail(email);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                response.put("success", true);
                response.put("message", "Admin login successful");
                response.put("role", "ADMIN");
                response.put("admin", admin);
            }
        } else {
            response.put("success", false);
            response.put("message", "Invalid admin credentials");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> allUsers = userService.findAllUsers();
            response.put("success", true);
            response.put("totalUsers", allUsers.size());
            response.put("users", allUsers);
            response.put("message", "Dashboard data loaded successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> users = userService.findAllUsers();
            response.put("success", true);
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.deleteUser(id);
            response.put("success", true);
            response.put("message", "User deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
