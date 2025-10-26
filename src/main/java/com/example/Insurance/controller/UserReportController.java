package com.example.Insurance.controller;

import com.example.Insurance.DTO.UserDTO;
import com.example.Insurance.DTO.UserReportDTO;
import com.example.Insurance.service.UserReportService;
import com.example.Insurance.service.UserService; // Your existing UserService
import com.example.Insurance.model.User; // Your existing User model
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UserReportController {

    @Autowired
    private UserReportService userReportService;

    @Autowired
    private UserService userService; // Your existing UserService

    // SECURE: Get user reports by NIC with authentication
    @PostMapping("/secure/my-reports")
    public ResponseEntity<Map<String, Object>> getMyReports(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");
            String requestedNIC = request.get("nic");

            System.out.println("🔐 SECURE ACCESS: User requesting reports for NIC: " + requestedNIC);

            // Step 1: Authenticate the user
            if (!userService.authenticateUser(email, password)) {
                System.out.println("❌ AUTHENTICATION FAILED for email: " + email);
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Step 2: Get authenticated user details
            Optional<User> authenticatedUserOpt = userService.findByEmail(email);
            if (authenticatedUserOpt.isEmpty()) {
                System.out.println("❌ USER NOT FOUND after authentication: " + email);
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User authenticatedUser = authenticatedUserOpt.get();

            // Step 3: PRIVACY CHECK - User can only access their own reports
            if (!authenticatedUser.getNic().equals(requestedNIC)) {
                System.out.println("🚫 PRIVACY VIOLATION ATTEMPT:");
                System.out.println("   - Authenticated User NIC: " + authenticatedUser.getNic());
                System.out.println("   - Requested NIC: " + requestedNIC);
                System.out.println("   - User Email: " + email);

                response.put("success", false);
                response.put("message", "Access denied: You can only view your own reports");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            System.out.println("✅ PRIVACY CHECK PASSED: User " + email + " accessing their own reports");

            // Step 4: Get user's reports
            List<UserReportDTO> reports = userReportService.getUserReportsByNic(authenticatedUser.getNic());

            response.put("success", true);
            response.put("data", reports);
            response.put("count", reports.size());
            response.put("user", Map.of(
                    "name", authenticatedUser.getName(),
                    "nic", authenticatedUser.getNic(),
                    "email", authenticatedUser.getEmail()
            ));

            System.out.println("✅ SECURE ACCESS GRANTED: Returned " + reports.size() + " reports for " + authenticatedUser.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ SECURE ACCESS ERROR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to retrieve your reports: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // SECURE: Get dashboard statistics with authentication
    @PostMapping("/secure/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getMyDashboardStats(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");

            // Authenticate user
            if (!userService.authenticateUser(email, password)) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get authenticated user
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            UserReportService.UserDashboardStatsDTO stats = userReportService.getUserDashboardStats(user.getNic());

            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve dashboard stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // SECURE: Update report notes with authentication and ownership check
    @PutMapping("/secure/{id}/notes")
    public ResponseEntity<Map<String, Object>> updateMyReportNotes(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");
            String notes = request.get("notes");

            // Authenticate user
            if (!userService.authenticateUser(email, password)) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get authenticated user
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();

            // Check if report belongs to this user
            if (!userReportService.isReportOwnedByUser(id, user.getNic())) {
                System.out.println("🚫 UNAUTHORIZED ACCESS ATTEMPT: User " + email + " trying to access report " + id);
                response.put("success", false);
                response.put("message", "Access denied: This report does not belong to you");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            UserReportDTO updatedReport = userReportService.updateUserReportNotes(id, notes);

            response.put("success", true);
            response.put("data", updatedReport);
            response.put("message", "Notes updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update notes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // SECURE: Toggle favorite with authentication and ownership check
    @PutMapping("/secure/{id}/favorite")
    public ResponseEntity<Map<String, Object>> toggleMyFavorite(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");

            // Authenticate user
            if (!userService.authenticateUser(email, password)) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get authenticated user
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();

            // Check if report belongs to this user
            if (!userReportService.isReportOwnedByUser(id, user.getNic())) {
                response.put("success", false);
                response.put("message", "Access denied: This report does not belong to you");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            UserReportDTO updatedReport = userReportService.toggleFavoriteReport(id);

            response.put("success", true);
            response.put("data", updatedReport);
            response.put("message", "Favorite status updated");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to toggle favorite: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // SECURE: Mark report as viewed with authentication and ownership check
    @PutMapping("/secure/{id}/viewed")
    public ResponseEntity<Map<String, Object>> markMyReportAsViewed(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");

            // Authenticate user
            if (!userService.authenticateUser(email, password)) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get authenticated user
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();

            // Check if report belongs to this user
            if (!userReportService.isReportOwnedByUser(id, user.getNic())) {
                response.put("success", false);
                response.put("message", "Access denied: This report does not belong to you");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            userReportService.markReportAsViewed(id);

            response.put("success", true);
            response.put("message", "Marked as viewed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to mark as viewed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }
}
