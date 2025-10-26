package com.example.Insurance.controller;

import com.example.Insurance.entity.Payment;
import com.example.Insurance.entity.Policy;
import com.example.Insurance.repository.PaymentRepository;
import com.example.Insurance.repository.PolicyRepository;
import com.example.Insurance.DTO.UserPolicyPaymentDTO;
import com.example.Insurance.DTO.PaymentUpdateDTO; // Added
import com.example.Insurance.service.PaymentService;
import com.example.Insurance.Enums.PolicyStatus;
import com.example.Insurance.Enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/payments")
@CrossOrigin(origins = "*")
public class UserPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // DEBUG ENDPOINTS
    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Payment controller is working! Current time: " + LocalDateTime.now());
    }

    @GetMapping("/test-policies-count")
    public ResponseEntity<String> testPoliciesCount() {
        try {
            List<Policy> allPolicies = policyRepository.findAll();
            List<Policy> approvedPolicies = allPolicies.stream()
                    .filter(policy -> policy.getStatus() == PolicyStatus.APPROVED || policy.getStatus() == PolicyStatus.ACTIVE)
                    .collect(Collectors.toList());

            String result = "Database Connection: SUCCESS\n" +
                    "Total policies: " + allPolicies.size() + "\n" +
                    "Approved/Active policies: " + approvedPolicies.size() + "\n\n" +
                    "Policy Details:\n";

            for (Policy policy : allPolicies) {
                result += "ID: " + policy.getId() +
                        ", Name: " + policy.getName() +
                        ", Status: " + policy.getStatus() +
                        ", Premium: " + policy.getPremiumAmount() + "\n";
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok("Database Error: " + e.getMessage());
        }
    }

    // FIXED - Get user's approved policies with payment information
    @GetMapping("/policies/{userId}")
    public ResponseEntity<?> getUserPolicies(@PathVariable Long userId) {
        try {
            System.out.println("=== Controller: getUserPolicies called for user " + userId + " ===");

            List<UserPolicyPaymentDTO> policies = paymentService.getUserPoliciesWithPayments(userId);

            System.out.println("=== Controller: Returning " + policies.size() + " policies ===");
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            System.err.println("=== Controller Error in getUserPolicies: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long userId) {
        System.out.println("=== Controller: Getting history for user " + userId + " ===");

        try {
            List<Payment> payments = paymentService.getPaymentHistory(userId);
            System.out.println("Found " + payments.size() + " payments");

            List<Map<String, Object>> simplePayments = new ArrayList<>();
            for (Payment p : payments) {
                try {
                    Map<String, Object> simple = convertPaymentToMap(p);
                    simplePayments.add(simple);
                } catch (Exception e) {
                    System.err.println("Error processing payment " + p.getPaymentId() + ": " + e.getMessage());
                }
            }

            return ResponseEntity.ok(simplePayments);
        } catch (Exception e) {
            System.err.println("Controller error in getPaymentHistory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping
    public ResponseEntity<?> submitPayment(@RequestBody Payment payment) {
        try {
            System.out.println("=== Controller: submitPayment called ===");
            Payment savedPayment = paymentService.createPayment(payment);
            System.out.println("Payment created with ID: " + savedPayment.getPaymentId());
            return ResponseEntity.ok(convertPaymentToMap(savedPayment));
        } catch (Exception e) {
            System.err.println("Controller error in submitPayment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create payment: " + e.getMessage()));
        }
    }

    // CRITICAL FIX - Enhanced getPayment endpoint with proper error handling
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable Long paymentId) {
        System.out.println("=== Controller: getPayment called for ID " + paymentId + " ===");

        try {
            // Direct repository check first
            Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
            if (!paymentOptional.isPresent()) {
                System.err.println("❌ Payment " + paymentId + " not found in database");

                List<Payment> allPayments = paymentRepository.findAll();
                List<Long> availableIds = allPayments.stream()
                        .map(Payment::getPaymentId)
                        .collect(Collectors.toList());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "error", "Payment with ID " + paymentId + " not found",
                        "availablePaymentIds", availableIds,
                        "totalPayments", availableIds.size()
                ));
            }

            Payment payment = paymentOptional.get();

            // Safe conversion to map
            Map<String, Object> paymentMap;
            try {
                paymentMap = convertPaymentToMap(payment);
            } catch (Exception e) {
                System.err.println("Error converting payment to map: " + e.getMessage());
                paymentMap = new HashMap<>();
                paymentMap.put("paymentId", payment.getPaymentId());
                paymentMap.put("error", "Partial data due to serialization issue");
                paymentMap.put("paymentMonth", payment.getPaymentMonth());
                paymentMap.put("amount", payment.getAmount());
                paymentMap.put("status", payment.getStatus().toString());
            }

            // Add canEdit flag
            try {
                paymentMap.put("canEdit", paymentService.canEditPayment(payment));
            } catch (Exception e) {
                System.err.println("Error checking canEdit: " + e.getMessage());
                paymentMap.put("canEdit", false);
            }

            System.out.println("✅ Payment found: " + paymentId + ", Status: " + payment.getStatus());
            return ResponseEntity.ok(paymentMap);

        } catch (Exception e) {
            System.err.println("=== CRITICAL ERROR in getPayment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Server error: " + e.getMessage(),
                    "paymentId", paymentId
            ));
        }
    }

    // FIXED - Update payment using PaymentUpdateDTO
    @PutMapping("/{paymentId}")
    public ResponseEntity<?> updatePayment(@PathVariable Long paymentId, @RequestBody PaymentUpdateDTO paymentDTO) {
        try {
            System.out.println("=== Controller: updatePayment called for ID " + paymentId + " ===");
            Payment updatedPayment = paymentService.updatePayment(paymentId, paymentDTO);
            System.out.println("✅ Controller: Payment " + paymentId + " updated successfully");
            return ResponseEntity.ok(convertPaymentToMap(updatedPayment));
        } catch (Exception e) {
            System.err.println("Controller error in updatePayment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to update payment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable Long paymentId) {
        try {
            System.out.println("=== Controller: deletePayment called for ID " + paymentId + " ===");
            paymentService.deletePayment(paymentId);
            return ResponseEntity.ok(Map.of("message", "Payment deleted successfully"));
        } catch (Exception e) {
            System.err.println("Controller error in deletePayment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to delete payment: " + e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/upload-slip")
    public ResponseEntity<?> uploadBankSlip(@PathVariable Long paymentId, @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== Controller: uploadBankSlip called for payment " + paymentId + " ===");
            paymentService.uploadBankSlip(paymentId, file);
            return ResponseEntity.ok(Map.of("message", "Bank slip uploaded successfully"));
        } catch (Exception e) {
            System.err.println("Controller error in uploadBankSlip: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/bank-details")
    public ResponseEntity<?> getBankDetails() {
        try {
            Map<String, String> bankDetails = paymentService.getBankDetails();
            return ResponseEntity.ok(bankDetails);
        } catch (Exception e) {
            System.err.println("Controller error in getBankDetails: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "bankName", "Commercial Bank of Ceylon PLC",
                    "accountNumber", "8001234567890",
                    "accountName", "MOTORCARE LK (PVT) LTD",
                    "branch", "Colombo 03"
            ));
        }
    }

    @GetMapping("/pending/{userId}")
    public ResponseEntity<?> getPendingPayments(@PathVariable Long userId) {
        try {
            List<Payment> allUserPayments = paymentService.getPaymentHistory(userId);
            List<Payment> pendingPayments = allUserPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .collect(Collectors.toList());

            List<Map<String, Object>> result = new ArrayList<>();
            for (Payment p : pendingPayments) {
                result.add(convertPaymentToMap(p));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Controller error in getPendingPayments: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // CRITICAL FIX - Safe conversion method
    private Map<String, Object> convertPaymentToMap(Payment payment) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("paymentId", payment.getPaymentId());
            map.put("userId", payment.getUserId() != null ? payment.getUserId() : null);

            // Safe field access
            try {
                map.put("userName", payment.getUserName() != null ? payment.getUserName() : "N/A");
            } catch (Exception e) {
                map.put("userName", "N/A");
            }

            try {
                map.put("userEmail", payment.getUserEmail() != null ? payment.getUserEmail() : "N/A");
            } catch (Exception e) {
                map.put("userEmail", "N/A");
            }

            map.put("paymentMonth", payment.getPaymentMonth());
            map.put("amount", payment.getAmount());
            map.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "UNKNOWN");

            try {
                map.put("paymentMethod", payment.getPaymentMethod() != null ?
                        payment.getPaymentMethod().toString() : "UNKNOWN");
            } catch (Exception e) {
                map.put("paymentMethod", "UNKNOWN");
            }

            map.put("submittedDate", payment.getSubmittedDate() != null ?
                    payment.getSubmittedDate().toString() : null);
            map.put("expiryTime", payment.getExpiryTime() != null ?
                    payment.getExpiryTime().toString() : null);

            try {
                map.put("adminComments", payment.getAdminComments() != null ?
                        payment.getAdminComments() : null);
            } catch (Exception e) {
                map.put("adminComments", null);
            }

            try {
                map.put("approvedDate", payment.getApprovedDate() != null ?
                        payment.getApprovedDate().toString() : null);
            } catch (Exception e) {
                map.put("approvedDate", null);
            }

            // Safe policy handling
            try {
                if (payment.getPolicy() != null && payment.getPolicy().getId() != null) {
                    Map<String, Object> policyInfo = new HashMap<>();
                    policyInfo.put("id", payment.getPolicy().getId());
                    policyInfo.put("policyNumber", "POL-" + payment.getPolicy().getId());
                    map.put("policy", policyInfo);
                } else {
                    map.put("policy", Map.of("id", null, "policyNumber", "N/A"));
                }
            } catch (Exception e) {
                map.put("policy", Map.of("id", null, "policyNumber", "N/A"));
                System.err.println("Error processing policy: " + e.getMessage());
            }

            // Safe details handling
            try {
                if (payment.getBankSlipDetails() != null) {
                    Map<String, Object> bankDetails = new HashMap<>();
                    bankDetails.put("depositorName", payment.getBankSlipDetails().getDepositorName());
                    bankDetails.put("depositDate", payment.getBankSlipDetails().getDepositDate());
                    bankDetails.put("referenceNumber", payment.getBankSlipDetails().getReferenceNumber());
                    bankDetails.put("bankName", payment.getBankSlipDetails().getBankName());
                    bankDetails.put("branch", payment.getBankSlipDetails().getBranch());
                    map.put("bankSlipDetails", bankDetails);
                }
            } catch (Exception e) {
                System.err.println("Error processing bank slip details: " + e.getMessage());
            }

            try {
                if (payment.getOnlinePaymentDetails() != null) {
                    Map<String, Object> onlineDetails = new HashMap<>();
                    onlineDetails.put("cardholderName", payment.getOnlinePaymentDetails().getCardholderName());
                    onlineDetails.put("cardNumber", payment.getOnlinePaymentDetails().getCardNumber());
                    onlineDetails.put("expirationDate", payment.getOnlinePaymentDetails().getExpirationDate());
                    map.put("onlinePaymentDetails", onlineDetails);
                }
            } catch (Exception e) {
                System.err.println("Error processing online payment details: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Critical error in convertPaymentToMap: " + e.getMessage());
            e.printStackTrace();
            map.put("error", "Serialization failed: " + e.getMessage());
        }

        return map;
    }

    // Debug endpoints
    @GetMapping("/debug/all-payments")
    public ResponseEntity<?> getAllPayments() {
        try {
            List<Payment> allPayments = paymentRepository.findAll();
            List<Map<String, Object>> debugInfo = new ArrayList<>();

            for (Payment payment : allPayments) {
                debugInfo.add(convertPaymentToMap(payment));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalPayments", allPayments.size(),
                    "payments", debugInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "totalPayments", 0,
                    "payments", new ArrayList<>()
            ));
        }
    }

    @PostMapping("/create-test-payment")
    public ResponseEntity<?> createTestPayment() {
        try {
            Policy testPolicy = policyRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PolicyStatus.APPROVED)
                    .findFirst().orElseGet(() -> {
                        Policy policy = new Policy();
                        policy.setName("Test Policy");
                        policy.setPremiumAmount(15000.0);
                        policy.setStatus(PolicyStatus.APPROVED);
                        return policyRepository.save(policy);
                    });

            Payment testPayment = new Payment();
            testPayment.setPolicy(testPolicy);
            testPayment.setUserId(1L);
            testPayment.setPaymentMonth("January");
            testPayment.setAmount(java.math.BigDecimal.valueOf(15000));
            testPayment.setStatus(PaymentStatus.PENDING);
            testPayment.setSubmittedDate(LocalDateTime.now());
            testPayment.setExpiryTime(LocalDateTime.now().plusHours(12));

            Payment saved = paymentRepository.save(testPayment);
            return ResponseEntity.ok(convertPaymentToMap(saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}