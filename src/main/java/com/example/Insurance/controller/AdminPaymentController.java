package com.example.Insurance.controller;

import com.example.Insurance.entity.Payment;
import com.example.Insurance.entity.Policy;
import com.example.Insurance.repository.PaymentRepository;
import com.example.Insurance.service.PaymentService;
import com.example.Insurance.Enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/payments")
@CrossOrigin(origins = "*")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    // Test connection
    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Admin Payment controller is working! Current time: " + LocalDateTime.now());
    }

    // Get all payments for admin dashboard
    @GetMapping("/all")
    public ResponseEntity<?> getAllPayments() {
        try {
            System.out.println("=== AdminController: Getting all payments for admin dashboard ===");

            List<Payment> allPayments = paymentRepository.findAll();

            // Convert to admin-friendly format
            List<Map<String, Object>> adminPayments = allPayments.stream()
                    .map(this::convertToAdminPaymentDTO)
                    .sorted((p1, p2) -> {
                        // Sort by submitted date (newest first)
                        LocalDateTime date1 = (LocalDateTime) p1.get("submittedDate");
                        LocalDateTime date2 = (LocalDateTime) p2.get("submittedDate");
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;
                        return date2.compareTo(date1);
                    })
                    .collect(Collectors.toList());

            System.out.println("Admin dashboard returning " + adminPayments.size() + " payments");

            return ResponseEntity.ok(Map.of(
                    "totalPayments", allPayments.size(),
                    "payments", adminPayments,
                    "stats", calculatePaymentStats(allPayments)
            ));

        } catch (Exception e) {
            System.err.println("Error getting all payments for admin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "totalPayments", 0,
                    "payments", Collections.emptyList(),
                    "stats", Map.of(
                            "pending", 0,
                            "approved", 0,
                            "rejected", 0,
                            "total", 0
                    ),
                    "error", e.getMessage()
            ));
        }
    }

    // Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable String status) {
        try {
            System.out.println("=== AdminController: Getting payments with status: " + status + " ===");

            List<Payment> payments;

            if ("all".equalsIgnoreCase(status)) {
                payments = paymentRepository.findAll();
            } else {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
                payments = paymentRepository.findByStatusOrderBySubmittedDateDesc(paymentStatus);
            }

            List<Map<String, Object>> adminPayments = payments.stream()
                    .map(this::convertToAdminPaymentDTO)
                    .collect(Collectors.toList());

            System.out.println("Found " + payments.size() + " payments with status: " + status);

            return ResponseEntity.ok(Map.of(
                    "payments", adminPayments,
                    "count", payments.size(),
                    "status", status
            ));

        } catch (Exception e) {
            System.err.println("Error getting payments by status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "payments", Collections.emptyList(),
                    "count", 0,
                    "error", e.getMessage()
            ));
        }
    }

    // Get pending payments
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPayments() {
        try {
            System.out.println("=== AdminController: Getting pending payments ===");

            List<Payment> pendingPayments = paymentRepository.findByStatusOrderBySubmittedDateDesc(PaymentStatus.PENDING);

            List<Map<String, Object>> adminPayments = pendingPayments.stream()
                    .map(this::convertToAdminPaymentDTO)
                    .collect(Collectors.toList());

            System.out.println("Found " + pendingPayments.size() + " pending payments");

            return ResponseEntity.ok(Map.of(
                    "payments", adminPayments,
                    "count", pendingPayments.size()
            ));

        } catch (Exception e) {
            System.err.println("Error getting pending payments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "payments", Collections.emptyList(),
                    "count", 0,
                    "error", e.getMessage()
            ));
        }
    }

    // Approve payment
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<?> approvePayment(@PathVariable Long paymentId, @RequestBody Map<String, String> request) {
        try {
            System.out.println("=== AdminController: Approving payment " + paymentId + " ===");

            String adminComments = request.getOrDefault("adminComments", "Payment approved by admin");

            Payment approvedPayment = paymentService.approvePayment(paymentId, adminComments);

            System.out.println("✅ Payment " + paymentId + " approved successfully");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment approved successfully",
                    "payment", convertToAdminPaymentDTO(approvedPayment)
            ));

        } catch (Exception e) {
            System.err.println("❌ Error approving payment " + paymentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to approve payment: " + e.getMessage()
            ));
        }
    }

    // Reject payment
    @PostMapping("/{paymentId}/reject")
    public ResponseEntity<?> rejectPayment(@PathVariable Long paymentId, @RequestBody Map<String, String> request) {
        try {
            System.out.println("=== AdminController: Rejecting payment " + paymentId + " ===");

            String adminComments = request.get("adminComments");
            if (adminComments == null || adminComments.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Admin comments are required for rejection"
                ));
            }

            Payment rejectedPayment = paymentService.rejectPayment(paymentId, adminComments);

            System.out.println("❌ Payment " + paymentId + " rejected successfully");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment rejected successfully",
                    "payment", convertToAdminPaymentDTO(rejectedPayment)
            ));

        } catch (Exception e) {
            System.err.println("❌ Error rejecting payment " + paymentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to reject payment: " + e.getMessage()
            ));
        }
    }

    // Get payment details for admin
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable Long paymentId) {
        try {
            System.out.println("=== AdminController: Getting payment details for " + paymentId + " ===");

            Payment payment = paymentService.getPaymentById(paymentId);
            Map<String, Object> detailedPayment = convertToAdminPaymentDTO(payment);

            // Add additional admin-specific details
            detailedPayment.put("canApprove", payment.getStatus() == PaymentStatus.PENDING);
            detailedPayment.put("canReject", payment.getStatus() == PaymentStatus.PENDING);
            detailedPayment.put("isExpired", !paymentService.canEditPayment(payment));

            System.out.println("✅ Payment details retrieved for " + paymentId);

            return ResponseEntity.ok(detailedPayment);

        } catch (Exception e) {
            System.err.println("❌ Error getting payment details for " + paymentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    // Update payment status (generic method for approve/reject)
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long paymentId, @RequestBody Map<String, String> request) {
        try {
            System.out.println("=== AdminController: Updating payment status for " + paymentId + " ===");

            String status = request.get("status");
            String adminComments = request.get("adminComments");

            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Status is required"
                ));
            }

            Payment updatedPayment;

            switch (status.toUpperCase()) {
                case "APPROVED":
                    updatedPayment = paymentService.approvePayment(paymentId, adminComments);
                    break;
                case "REJECTED":
                    if (adminComments == null || adminComments.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "error", "Admin comments are required for rejection"
                        ));
                    }
                    updatedPayment = paymentService.rejectPayment(paymentId, adminComments);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "error", "Invalid status. Use APPROVED or REJECTED"
                    ));
            }

            System.out.println("✅ Payment " + paymentId + " status updated to " + status);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment status updated successfully",
                    "payment", convertToAdminPaymentDTO(updatedPayment)
            ));

        } catch (Exception e) {
            System.err.println("❌ Error updating payment status for " + paymentId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to update payment status: " + e.getMessage()
            ));
        }
    }

    // Get payment statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getPaymentStats() {
        try {
            System.out.println("=== AdminController: Getting payment statistics ===");

            List<Payment> allPayments = paymentRepository.findAll();
            Map<String, Object> stats = calculatePaymentStats(allPayments);

            System.out.println("✅ Payment statistics calculated");

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println("❌ Error getting payment statistics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "pending", 0,
                    "approved", 0,
                    "rejected", 0,
                    "total", 0,
                    "error", e.getMessage()
            ));
        }
    }

    // Bulk approve payments
    @PostMapping("/bulk/approve")
    public ResponseEntity<?> bulkApprovePayments(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== AdminController: Bulk approving payments ===");

            @SuppressWarnings("unchecked")
            List<Long> paymentIds = (List<Long>) request.get("paymentIds");
            String adminComments = (String) request.getOrDefault("adminComments", "Bulk approved by admin");

            if (paymentIds == null || paymentIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Payment IDs are required"
                ));
            }

            List<Map<String, Object>> approvedPayments = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Long paymentId : paymentIds) {
                try {
                    Payment approved = paymentService.approvePayment(paymentId, adminComments);
                    approvedPayments.add(convertToAdminPaymentDTO(approved));
                } catch (Exception e) {
                    errors.add("Payment " + paymentId + ": " + e.getMessage());
                }
            }

            System.out.println("✅ Bulk approval completed: " + approvedPayments.size() + " approved, " + errors.size() + " errors");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "approvedCount", approvedPayments.size(),
                    "approvedPayments", approvedPayments,
                    "errors", errors
            ));

        } catch (Exception e) {
            System.err.println("❌ Error in bulk approval: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to process bulk approval: " + e.getMessage()
            ));
        }
    }

    // Search payments
    @GetMapping("/search")
    public ResponseEntity<?> searchPayments(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String policyId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            System.out.println("=== AdminController: Searching payments ===");

            List<Payment> allPayments = paymentRepository.findAll();

            // Apply filters
            List<Payment> filteredPayments = allPayments.stream()
                    .filter(payment -> {
                        // User ID filter
                        if (userId != null && !userId.isEmpty()) {
                            if (!payment.getUserId().toString().contains(userId)) {
                                return false;
                            }
                        }

                        // Policy ID filter
                        if (policyId != null && !policyId.isEmpty()) {
                            if (payment.getPolicy() == null || !payment.getPolicy().getId().toString().contains(policyId)) {
                                return false;
                            }
                        }

                        // Month filter
                        if (month != null && !month.isEmpty()) {
                            if (!payment.getPaymentMonth().toLowerCase().contains(month.toLowerCase())) {
                                return false;
                            }
                        }

                        // Status filter
                        if (status != null && !status.isEmpty()) {
                            if (!payment.getStatus().toString().equalsIgnoreCase(status)) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> searchResults = filteredPayments.stream()
                    .map(this::convertToAdminPaymentDTO)
                    .collect(Collectors.toList());

            System.out.println("✅ Search completed: " + searchResults.size() + " results found");

            return ResponseEntity.ok(Map.of(
                    "payments", searchResults,
                    "count", searchResults.size(),
                    "totalCount", allPayments.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error searching payments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "payments", Collections.emptyList(),
                    "count", 0,
                    "error", e.getMessage()
            ));
        }
    }

    // Helper method to convert Payment to admin-friendly DTO
    private Map<String, Object> convertToAdminPaymentDTO(Payment payment) {
        Map<String, Object> dto = new HashMap<>();

        try {
            dto.put("paymentId", payment.getPaymentId());
            dto.put("userId", payment.getUserId());
            dto.put("userName", payment.getUserName());
            dto.put("userEmail", payment.getUserEmail());

            // Policy information
            if (payment.getPolicy() != null) {
                Map<String, Object> policyInfo = new HashMap<>();
                policyInfo.put("id", payment.getPolicy().getId());
                policyInfo.put("policyNumber", "POL-" + payment.getPolicy().getId());
                policyInfo.put("name", payment.getPolicy().getName());
                policyInfo.put("vehicleType", payment.getPolicy().getVehicleType());
                dto.put("policy", policyInfo);
            } else {
                dto.put("policy", null);
            }

            dto.put("paymentMonth", payment.getPaymentMonth());
            dto.put("amount", payment.getAmount());
            dto.put("paymentMethod", payment.getPaymentMethod());
            dto.put("status", payment.getStatus());
            dto.put("submittedDate", payment.getSubmittedDate());
            dto.put("approvedDate", payment.getApprovedDate());
            dto.put("expiryTime", payment.getExpiryTime());
            dto.put("adminComments", payment.getAdminComments());

            // Bank slip details
            if (payment.getBankSlipDetails() != null) {
                Map<String, Object> bankSlip = new HashMap<>();
                bankSlip.put("bankName", payment.getBankSlipDetails().getBankName());
                bankSlip.put("branch", payment.getBankSlipDetails().getBranch());
                bankSlip.put("depositDate", payment.getBankSlipDetails().getDepositDate());
                bankSlip.put("referenceNumber", payment.getBankSlipDetails().getReferenceNumber());
                bankSlip.put("depositorName", payment.getBankSlipDetails().getDepositorName());
                dto.put("bankSlipDetails", bankSlip);
            }

            // Online payment details
            if (payment.getOnlinePaymentDetails() != null) {
                Map<String, Object> onlinePayment = new HashMap<>();
                onlinePayment.put("cardholderName", payment.getOnlinePaymentDetails().getCardholderName());
                onlinePayment.put("cardNumber", "****" + payment.getOnlinePaymentDetails().getCardNumber().substring(Math.max(0, payment.getOnlinePaymentDetails().getCardNumber().length() - 4)));
                onlinePayment.put("transactionId", payment.getOnlinePaymentDetails().getTransactionId());
                onlinePayment.put("paymentSuccessful", payment.getOnlinePaymentDetails().getPaymentSuccessful());
                dto.put("onlinePaymentDetails", onlinePayment);
            }

            // Admin-specific flags
            dto.put("canEdit", paymentService.canEditPayment(payment));
            dto.put("isPending", payment.getStatus() == PaymentStatus.PENDING);
            dto.put("timeRemaining", paymentService.canEditPayment(payment) ? calculateTimeRemaining(payment.getExpiryTime()) : "Expired");

        } catch (Exception e) {
            System.err.println("Error converting payment to admin DTO: " + e.getMessage());
            dto.put("error", "Error processing payment data");
        }

        return dto;
    }

    // Helper method to calculate payment statistics
    private Map<String, Object> calculatePaymentStats(List<Payment> payments) {
        Map<String, Object> stats = new HashMap<>();

        long pending = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
        long approved = payments.stream().filter(p -> p.getStatus() == PaymentStatus.APPROVED).count();
        long rejected = payments.stream().filter(p -> p.getStatus() == PaymentStatus.REJECTED).count();

        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("total", payments.size());

        // Calculate total amount by status
        double totalPendingAmount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();

        double totalApprovedAmount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();

        stats.put("totalPendingAmount", totalPendingAmount);
        stats.put("totalApprovedAmount", totalApprovedAmount);

        return stats;
    }

    // Helper method to calculate time remaining
    private String calculateTimeRemaining(LocalDateTime expiryTime) {
        if (expiryTime == null) return "N/A";

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiryTime)) return "Expired";

        java.time.Duration duration = java.time.Duration.between(now, expiryTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
