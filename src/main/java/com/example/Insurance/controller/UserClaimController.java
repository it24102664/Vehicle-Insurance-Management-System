// src/main/java/com/example/Insurance/controller/UserClaimController.java
package com.example.Insurance.controller;

import com.example.Insurance.DTO.ClaimDTO;
import com.example.Insurance.entity.Claim;
import com.example.Insurance.entity.ClaimForm;
import com.example.Insurance.entity.UserClaimNotification;
import com.example.Insurance.service.UserClaimService;
import com.example.Insurance.service.ClaimFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/claims")
@CrossOrigin(origins = "*")
public class UserClaimController {

    @Autowired
    private UserClaimService userClaimService;

    @Autowired
    private ClaimFormService claimFormService;

    // Get ONLY ACTIVE claim forms for users (real-time sync with admin)
    @GetMapping("/forms")
    public ResponseEntity<List<ClaimForm>> getAvailableClaimForms() {
        try {
            List<ClaimForm> forms = claimFormService.getAllForms(); // This returns only ACTIVE forms
            System.out.println("📋 USER FORMS REQUEST: Returning " + forms.size() + " active forms");
            return ResponseEntity.ok(forms);
        } catch (Exception e) {
            System.err.println("❌ Error getting claim forms for user: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Get specific ACTIVE claim form by ID (users can only access active forms)
    @GetMapping("/forms/{formId}")
    public ResponseEntity<?> getClaimFormById(@PathVariable Long formId) {
        try {
            ClaimForm form = claimFormService.getFormById(formId); // This only returns ACTIVE forms
            System.out.println("✅ USER FORM ACCESS: " + form.getFormName());
            return ResponseEntity.ok(form);
        } catch (Exception e) {
            System.err.println("❌ User tried to access unavailable form ID: " + formId + " - " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                    "error", "FORM_NOT_AVAILABLE",
                    "message", "This form is no longer available. Please choose another form or contact support."
            ));
        }
    }

    // Check if form is still available (real-time validation)
    @GetMapping("/forms/{formId}/availability")
    public ResponseEntity<Map<String, Boolean>> checkFormAvailability(@PathVariable Long formId) {
        try {
            boolean isAvailable = claimFormService.isFormAvailable(formId);
            System.out.println("🔍 Form availability check for ID " + formId + ": " + (isAvailable ? "Available" : "Not available"));
            return ResponseEntity.ok(Map.of("available", isAvailable));
        } catch (Exception e) {
            System.err.println("❌ Error checking form availability: " + e.getMessage());
            return ResponseEntity.ok(Map.of("available", false));
        }
    }

    // Submit new claim - FIXED with better validation
    @PostMapping("/submit")
    public ResponseEntity<?> submitUserClaim(@RequestBody ClaimDTO claimDTO) {
        try {
            System.out.println("📝 Received user claim submission for: " + claimDTO.getFullName());

            // Enhanced validation
            if (claimDTO.getFullName() == null || claimDTO.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Full name is required"));
            }
            if (claimDTO.getEmail() == null || claimDTO.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (claimDTO.getNic() == null || claimDTO.getNic().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "NIC is required"));
            }
            if (claimDTO.getPhone() == null || claimDTO.getPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone is required"));
            }

            ClaimDTO submittedClaim = userClaimService.submitClaim(claimDTO);
            System.out.println("✅ Claim submitted successfully: " + submittedClaim.getClaimNumber());
            return ResponseEntity.ok(submittedClaim);
        } catch (Exception e) {
            System.err.println("❌ Error submitting user claim: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to submit claim: " + e.getMessage()
            ));
        }
    }

    // Get user claims by NIC
    @GetMapping("/user/{nic}")
    public ResponseEntity<List<ClaimDTO>> getUserClaims(@PathVariable String nic) {
        try {
            List<ClaimDTO> claims = userClaimService.getUserClaimsByNic(nic);
            System.out.println("📋 Retrieved " + claims.size() + " claims for NIC: " + nic);
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            System.err.println("❌ Error getting user claims: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Get user claims by status
    @GetMapping("/user/{nic}/status/{status}")
    public ResponseEntity<List<ClaimDTO>> getUserClaimsByStatus(
            @PathVariable String nic,
            @PathVariable String status) {
        try {
            Claim.ClaimStatus claimStatus = Claim.ClaimStatus.valueOf(status.toUpperCase());
            List<ClaimDTO> claims = userClaimService.getUserClaimsByStatus(nic, claimStatus);
            System.out.println("📋 Retrieved " + claims.size() + " " + status + " claims for NIC: " + nic);
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            System.err.println("❌ Error getting user claims by status: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Get user claim statistics
    @GetMapping("/user/{nic}/statistics")
    public ResponseEntity<Map<String, Long>> getUserClaimStatistics(@PathVariable String nic) {
        try {
            Map<String, Long> stats = userClaimService.getUserClaimStatistics(nic);
            System.out.println("📊 Statistics for NIC " + nic + ": " + stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Error getting user claim statistics: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Get user notifications
    @GetMapping("/user/{nic}/notifications")
    public ResponseEntity<List<UserClaimNotification>> getUserClaimNotifications(@PathVariable String nic) {
        try {
            List<UserClaimNotification> notifications = userClaimService.getUserClaimNotifications(nic);
            System.out.println("🔔 Retrieved " + notifications.size() + " notifications for NIC: " + nic);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("❌ Error getting user notifications: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Mark notification as read
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markUserClaimNotificationAsRead(@PathVariable Long notificationId) {
        try {
            userClaimService.markUserClaimNotificationAsRead(notificationId);
            System.out.println("✅ Notification " + notificationId + " marked as read");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ Error marking notification as read: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
