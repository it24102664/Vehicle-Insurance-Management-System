package com.example.Insurance.controller;

import com.example.Insurance.DTO.AdminReportDTO;
import com.example.Insurance.service.AdminReportService;
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

@RestController
@RequestMapping("/api/admin-reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminReportController {

    @Autowired
    private AdminReportService adminReportService;

    // Get all website users for selection
    @GetMapping("/website-users")
    public ResponseEntity<Map<String, Object>> getWebsiteUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔄 Fetching website users for report assignment");

            List<User> users = adminReportService.getAllWebsiteUsers();

            response.put("success", true);
            response.put("data", users);
            response.put("count", users.size());

            System.out.println("✅ Found " + users.size() + " website users");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to fetch website users: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to retrieve website users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Search website users
    @GetMapping("/search-website-users")
    public ResponseEntity<Map<String, Object>> searchWebsiteUsers(@RequestParam(required = false) String query) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔍 Searching website users with query: " + query);

            List<User> users = adminReportService.searchWebsiteUsers(query);

            response.put("success", true);
            response.put("data", users);
            response.put("count", users.size());

            System.out.println("✅ Found " + users.size() + " website users matching query");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to search website users: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to search website users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Create new admin report for website user
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdminReport(@RequestBody AdminReportDTO adminReportDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔄 CREATE REQUEST: Creating report for website user: " + adminReportDTO.getCustomerName());
            System.out.println("📋 Target NIC: " + adminReportDTO.getCustomerNIC());

            AdminReportDTO createdReport = adminReportService.createAdminReport(adminReportDTO);

            response.put("success", true);
            response.put("message", "Report successfully sent to website user: " + adminReportDTO.getCustomerName());
            response.put("data", createdReport);

            System.out.println("✅ CREATE SUCCESS: Report sent to website user successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            System.err.println("❌ CREATE ERROR: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("❌ CREATE UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to send report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // FIXED: Update admin report (This was missing!)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAdminReport(@PathVariable Long id,
                                                                 @RequestBody AdminReportDTO adminReportDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔄 UPDATE REQUEST: Updating report ID: " + id);
            System.out.println("📋 Update data for: " + adminReportDTO.getCustomerName());
            System.out.println("📊 Payment data count: " + (adminReportDTO.getPaymentData() != null ? adminReportDTO.getPaymentData().size() : 0));

            AdminReportDTO updatedReport = adminReportService.updateAdminReport(id, adminReportDTO);

            response.put("success", true);
            response.put("message", "Report updated successfully for " + adminReportDTO.getCustomerName());
            response.put("data", updatedReport);

            System.out.println("✅ UPDATE SUCCESS: Report updated successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ UPDATE ERROR: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            System.err.println("❌ UPDATE UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to update report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get admin report by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdminReportById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            return adminReportService.getAdminReportById(id)
                    .map(report -> {
                        response.put("success", true);
                        response.put("data", report);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                            "success", false,
                            "message", "Admin report not found with id: " + id
                    )));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve admin report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get all admin reports
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAdminReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AdminReportDTO> reports = adminReportService.getAllAdminReports(pageable);

            response.put("success", true);
            response.put("data", reports.getContent());
            response.put("pagination", Map.of(
                    "currentPage", reports.getNumber(),
                    "totalPages", reports.getTotalPages(),
                    "totalElements", reports.getTotalElements(),
                    "size", reports.getSize()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve admin reports: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Delete admin report
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdminReport(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🗑️ DELETE REQUEST: Deleting report ID: " + id);

            adminReportService.deleteAdminReport(id);
            response.put("success", true);
            response.put("message", "Report deleted successfully");

            System.out.println("✅ DELETE SUCCESS: Report deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ DELETE ERROR: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            System.err.println("❌ DELETE UNEXPECTED ERROR: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to delete report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Handle OPTIONS requests
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }
}
