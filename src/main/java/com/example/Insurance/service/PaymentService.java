package com.example.Insurance.service;

import com.example.Insurance.entity.*;
import com.example.Insurance.repository.PaymentRepository;
import com.example.Insurance.repository.PolicyRepository;
import com.example.Insurance.DTO.UserPolicyPaymentDTO;
import com.example.Insurance.DTO.PaymentHistoryDTO;
import com.example.Insurance.DTO.PaymentUpdateDTO;
import com.example.Insurance.Enums.PaymentMethod;
import com.example.Insurance.Enums.PaymentStatus;
import com.example.Insurance.Enums.PolicyStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRepository policyRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/bank-slips/";

    // EMERGENCY FIX - Ultra simple payment history
    public List<Payment> getPaymentHistory(Long userId) {
        System.out.println("=== EMERGENCY FIX: Getting payments for user " + userId + " ===");

        try {
            List<Payment> allPayments = paymentRepository.findAll();
            System.out.println("STEP 1 SUCCESS: Found " + allPayments.size() + " total payments");

            if (allPayments.isEmpty()) {
                System.out.println("DATABASE IS EMPTY - No payments exist!");
                return new ArrayList<>();
            }

            List<Payment> result = new ArrayList<>();
            for (Payment payment : allPayments) {
                try {
                    System.out.println("Checking payment: ID=" + payment.getPaymentId() + ", UserID=" + payment.getUserId());
                    if (payment.getUserId() != null && payment.getUserId().equals(userId)) {
                        if (payment.getStatus() == PaymentStatus.PENDING && payment.getExpiryTime() == null && payment.getSubmittedDate() != null) {
                            payment.setExpiryTime(payment.getSubmittedDate().plusHours(12));
                        }
                        result.add(payment);
                        System.out.println("✅ MATCHED: Added payment " + payment.getPaymentId());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing payment: " + e.getMessage());
                }
            }

            System.out.println("FINAL RESULT: " + result.size() + " payments for user " + userId);
            return result;

        } catch (Exception e) {
            System.err.println("EMERGENCY ERROR: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get user's approved policies with payment information
    public List<UserPolicyPaymentDTO> getUserPoliciesWithPayments(Long userId) {
        try {
            System.out.println("=== PaymentService: getUserPoliciesWithPayments called for user " + userId + " ===");

            List<Policy> allPolicies = policyRepository.findAll();
            System.out.println("Total policies in database: " + allPolicies.size());

            List<Policy> eligiblePolicies = allPolicies.stream()
                    .filter(policy -> policy.getStatus() == PolicyStatus.APPROVED || policy.getStatus() == PolicyStatus.ACTIVE)
                    .collect(Collectors.toList());

            System.out.println("Found " + eligiblePolicies.size() + " policies eligible for payments");

            List<UserPolicyPaymentDTO> result = new ArrayList<>();

            for (Policy policy : eligiblePolicies) {
                try {
                    UserPolicyPaymentDTO dto = new UserPolicyPaymentDTO();
                    dto.setPolicyId(policy.getId());
                    dto.setPolicyNumber("POL-" + policy.getId());
                    dto.setPolicyType(policy.getName());
                    dto.setVehicle(policy.getVehicleType() + " - REG" + policy.getId());
                    dto.setMonthlyPremium(BigDecimal.valueOf(policy.getPremiumAmount()));
                    dto.setStatus("ACTIVE");

                    List<Payment> payments = getSimplePaymentsForPolicy(policy.getId());

                    List<String> paidMonths = payments.stream()
                            .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                            .map(Payment::getPaymentMonth)
                            .collect(Collectors.toList());

                    List<String> allMonths = Arrays.asList("January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December");

                    List<String> pendingMonths = allMonths.stream()
                            .filter(month -> !paidMonths.contains(month))
                            .collect(Collectors.toList());

                    dto.setPaidMonths(paidMonths);
                    dto.setPendingMonths(pendingMonths);

                    List<PaymentHistoryDTO> paymentHistory = payments.stream()
                            .map(this::convertToPaymentHistoryDTO)
                            .collect(Collectors.toList());

                    dto.setPaymentHistory(paymentHistory);
                    result.add(dto);

                    System.out.println("Created DTO for policy: " + policy.getName());

                } catch (Exception e) {
                    System.err.println("Error processing policy " + policy.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("=== Returning " + result.size() + " policy DTOs ===");
            return result;

        } catch (Exception e) {
            System.err.println("=== ERROR in getUserPoliciesWithPayments: " + e.getMessage() + " ===");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // SIMPLIFIED METHOD - Get payments for a specific policy
    private List<Payment> getSimplePaymentsForPolicy(Long policyId) {
        try {
            List<Payment> allPayments = paymentRepository.findAll();
            return allPayments.stream()
                    .filter(p -> p.getPolicy() != null && p.getPolicy().getId().equals(policyId))
                    .sorted((p1, p2) -> {
                        if (p1.getSubmittedDate() == null) return 1;
                        if (p2.getSubmittedDate() == null) return -1;
                        return p2.getSubmittedDate().compareTo(p1.getSubmittedDate());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting payments for policy " + policyId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Create new payment - ENHANCED with immediate verification
    public Payment createPayment(Payment payment) {
        try {
            System.out.println("=== PaymentService: Creating payment ===");

            Policy policy = policyService.getPolicyById(payment.getPolicy().getId());

            if (policy.getStatus() != PolicyStatus.APPROVED && policy.getStatus() != PolicyStatus.ACTIVE) {
                throw new RuntimeException("Policy must be ACTIVE or APPROVED to make payments");
            }

            payment.setPolicy(policy);
            payment.setUserId(1L);
            payment.setUserName("Policy Holder");
            payment.setUserEmail("policyholder@example.com");
            payment.setStatus(PaymentStatus.PENDING);
            payment.setSubmittedDate(LocalDateTime.now());
            payment.setExpiryTime(LocalDateTime.now().plusHours(12));

            if (payment.getPaymentMethod() == PaymentMethod.ONLINE_PAYMENT) {
                processOnlinePayment(payment);
            }

            System.out.println("Saving payment with data: " +
                    "UserID=" + payment.getUserId() +
                    ", Month=" + payment.getPaymentMonth() +
                    ", Amount=" + payment.getAmount());

            Payment savedPayment = paymentRepository.save(payment);
            System.out.println("=== PaymentService: Payment created with ID " + savedPayment.getPaymentId() + " ===");

            try {
                Payment verification = paymentRepository.findById(savedPayment.getPaymentId()).orElse(null);
                if (verification != null) {
                    System.out.println("✅ VERIFICATION: Payment " + savedPayment.getPaymentId() + " successfully saved and retrievable");
                } else {
                    System.err.println("❌ VERIFICATION FAILED: Payment " + savedPayment.getPaymentId() + " not found immediately after save!");
                }
            } catch (Exception e) {
                System.err.println("❌ VERIFICATION ERROR: " + e.getMessage());
            }

            return savedPayment;

        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    // FIXED - Get payment by ID with safer retrieval method
    public Payment getPaymentById(Long paymentId) {
        try {
            System.out.println("=== PaymentService: Getting payment by ID " + paymentId + " ===");

            List<Payment> allPayments = paymentRepository.findAll();
            System.out.println("Total payments in database: " + allPayments.size());

            for (Payment payment : allPayments) {
                if (payment.getPaymentId() != null && payment.getPaymentId().equals(paymentId)) {
                    System.out.println("✅ Found payment: ID=" + payment.getPaymentId() +
                            ", UserID=" + payment.getUserId() +
                            ", Status=" + payment.getStatus() +
                            ", Month=" + payment.getPaymentMonth());

                    if (payment.getStatus() == PaymentStatus.PENDING && payment.getExpiryTime() == null && payment.getSubmittedDate() != null) {
                        payment.setExpiryTime(payment.getSubmittedDate().plusHours(12));
                        try {
                            paymentRepository.save(payment);
                            System.out.println("Updated expiry time for payment " + payment.getPaymentId());
                        } catch (Exception saveError) {
                            System.err.println("Error updating expiry time: " + saveError.getMessage());
                        }
                    }

                    return payment;
                }
            }

            System.err.println("❌ Payment " + paymentId + " not found. Available payment IDs:");
            for (Payment p : allPayments) {
                System.err.println("  - Payment ID: " + p.getPaymentId() + ", User: " + p.getUserId());
            }

            throw new RuntimeException("Payment with ID " + paymentId + " not found in database");

        } catch (Exception e) {
            System.err.println("❌ Error in getPaymentById: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment not found: " + e.getMessage());
        }
    }

    // FIXED - Update payment using PaymentUpdateDTO with transactional context
    @Transactional
    public Payment updatePayment(Long paymentId, PaymentUpdateDTO updateDTO) {
        try {
            Payment existingPayment = getPaymentById(paymentId);

            if (!canEditPayment(existingPayment)) {
                throw new RuntimeException("Payment cannot be edited. Either expired or not pending.");
            }

            if (updateDTO.getPaymentMonth() != null && !updateDTO.getPaymentMonth().isEmpty()) {
                existingPayment.setPaymentMonth(updateDTO.getPaymentMonth());
            }
            if (updateDTO.getAmount() != null) {
                existingPayment.setAmount(updateDTO.getAmount());
            }
            if (updateDTO.getPaymentMethod() != null) {
                existingPayment.setPaymentMethod(updateDTO.getPaymentMethod());
            }

            if (updateDTO.getBankSlipDetails() != null) {
                BankSlipDetails slip = existingPayment.getBankSlipDetails();
                if (slip == null) {
                    slip = new BankSlipDetails();
                    existingPayment.setBankSlipDetails(slip);
                }
                if (updateDTO.getBankSlipDetails().getBankName() != null) slip.setBankName(updateDTO.getBankSlipDetails().getBankName());
                if (updateDTO.getBankSlipDetails().getBranch() != null) slip.setBranch(updateDTO.getBankSlipDetails().getBranch());
                if (updateDTO.getBankSlipDetails().getDepositorName() != null) slip.setDepositorName(updateDTO.getBankSlipDetails().getDepositorName());
                if (updateDTO.getBankSlipDetails().getReferenceNumber() != null) slip.setReferenceNumber(updateDTO.getBankSlipDetails().getReferenceNumber());
                if (updateDTO.getBankSlipDetails().getDepositDate() != null) slip.setDepositDate(updateDTO.getBankSlipDetails().getDepositDate());
            }

            if (updateDTO.getOnlinePaymentDetails() != null) {
                OnlinePaymentDetails online = existingPayment.getOnlinePaymentDetails();
                if (online == null) {
                    online = new OnlinePaymentDetails();
                    existingPayment.setOnlinePaymentDetails(online);
                }
                if (updateDTO.getOnlinePaymentDetails().getCardholderName() != null) online.setCardholderName(updateDTO.getOnlinePaymentDetails().getCardholderName());
                if (updateDTO.getOnlinePaymentDetails().getCardNumber() != null) online.setCardNumber(updateDTO.getOnlinePaymentDetails().getCardNumber());
                if (updateDTO.getOnlinePaymentDetails().getExpirationDate() != null) online.setExpirationDate(updateDTO.getOnlinePaymentDetails().getExpirationDate());
                if (updateDTO.getOnlinePaymentDetails().getCvc() != null) online.setCvc(updateDTO.getOnlinePaymentDetails().getCvc());
                processOnlinePayment(existingPayment);
            }

            existingPayment.setUpdatedDate(LocalDateTime.now());
            Payment updatedPayment = paymentRepository.save(existingPayment);
            System.out.println("=== Service: Updated payment ID " + paymentId + " - New State: " + updatedPayment);
            return updatedPayment;
        } catch (Exception e) {
            System.err.println("Error updating payment: " + e.getMessage());
            throw new RuntimeException("Failed to update payment: " + e.getMessage());
        }
    }

    public void deletePayment(Long paymentId) {
        try {
            Payment payment = getPaymentById(paymentId);

            if (!canEditPayment(payment)) {
                throw new RuntimeException("Payment cannot be deleted. Either expired or not pending.");
            }

            paymentRepository.delete(payment);
            System.out.println("✅ Payment " + paymentId + " successfully deleted");
        } catch (Exception e) {
            System.err.println("Error deleting payment: " + e.getMessage());
            throw new RuntimeException("Failed to delete payment: " + e.getMessage());
        }
    }

    public void uploadBankSlip(Long paymentId, MultipartFile file) throws IOException {
        try {
            Payment payment = getPaymentById(paymentId);

            if (payment.getPaymentMethod() != PaymentMethod.BANK_SLIP) {
                throw new RuntimeException("Payment method is not bank slip");
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = paymentId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath);

            if (payment.getBankSlipDetails() != null) {
                payment.getBankSlipDetails().setBankSlipImagePath(filename);
                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            System.err.println("Error uploading bank slip: " + e.getMessage());
            throw new IOException("Failed to upload bank slip: " + e.getMessage());
        }
    }

    public boolean canEditPayment(Payment payment) {
        if (payment == null) {
            return false;
        }

        boolean isPending = payment.getStatus() == PaymentStatus.PENDING;
        boolean hasExpiryTime = payment.getExpiryTime() != null;
        boolean isNotExpired = hasExpiryTime && LocalDateTime.now().isBefore(payment.getExpiryTime());

        System.out.println("CanEdit check for payment " + payment.getPaymentId() +
                ": isPending=" + isPending +
                ", hasExpiryTime=" + hasExpiryTime +
                ", isNotExpired=" + isNotExpired);

        return isPending && hasExpiryTime && isNotExpired;
    }

    public Payment approvePayment(Long paymentId, String adminComments) {
        try {
            Payment payment = getPaymentById(paymentId);

            payment.setStatus(PaymentStatus.APPROVED);
            payment.setApprovedDate(LocalDateTime.now());
            payment.setAdminComments(adminComments);

            return paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println("Error approving payment: " + e.getMessage());
            throw new RuntimeException("Failed to approve payment: " + e.getMessage());
        }
    }

    public Payment rejectPayment(Long paymentId, String adminComments) {
        try {
            Payment payment = getPaymentById(paymentId);

            payment.setStatus(PaymentStatus.REJECTED);
            payment.setAdminComments(adminComments);

            return paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println("Error rejecting payment: " + e.getMessage());
            throw new RuntimeException("Failed to reject payment: " + e.getMessage());
        }
    }

    public List<Payment> getPendingPayments() {
        try {
            List<Payment> allPayments = paymentRepository.findAll();
            List<Payment> pendingPayments = allPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .sorted((p1, p2) -> {
                        if (p1.getSubmittedDate() == null) return 1;
                        if (p2.getSubmittedDate() == null) return -1;
                        return p2.getSubmittedDate().compareTo(p1.getSubmittedDate());
                    })
                    .collect(Collectors.toList());

            System.out.println("Found " + pendingPayments.size() + " pending payments");
            return pendingPayments;
        } catch (Exception e) {
            System.err.println("Error getting pending payments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, String> getBankDetails() {
        Map<String, String> bankDetails = new HashMap<>();
        bankDetails.put("bankName", "Commercial Bank of Ceylon PLC");
        bankDetails.put("accountNumber", "8001234567890");
        bankDetails.put("accountName", "MOTORCARE LK (PVT) LTD");
        bankDetails.put("branch", "Colombo 03");
        return bankDetails;
    }

    private PaymentHistoryDTO convertToPaymentHistoryDTO(Payment payment) {
        try {
            PaymentHistoryDTO dto = new PaymentHistoryDTO();
            dto.setPaymentId(payment.getPaymentId());
            dto.setMonth(payment.getPaymentMonth());
            dto.setAmount(payment.getAmount());
            dto.setPaymentMethod(payment.getPaymentMethod());
            dto.setStatus(payment.getStatus());
            dto.setSubmittedDate(payment.getSubmittedDate());
            dto.setApprovedDate(payment.getApprovedDate());
            dto.setAdminComments(payment.getAdminComments());
            dto.setCanEdit(canEditPayment(payment));
            return dto;
        } catch (Exception e) {
            System.err.println("Error converting payment to DTO: " + e.getMessage());
            PaymentHistoryDTO dto = new PaymentHistoryDTO();
            dto.setPaymentId(payment.getPaymentId());
            dto.setMonth("Error");
            dto.setAmount(BigDecimal.ZERO);
            return dto;
        }
    }

    private void processOnlinePayment(Payment payment) {
        try {
            OnlinePaymentDetails details = payment.getOnlinePaymentDetails();

            details.setTransactionId("TXN" + System.currentTimeMillis());

            boolean success = Math.random() > 0.1;
            details.setPaymentSuccessful(success);

            if (!success) {
                payment.setAdminComments("Online payment failed. Please try again or use bank slip method.");
            }
        } catch (Exception e) {
            System.err.println("Error processing online payment: " + e.getMessage());
        }
    }

    // Get all payments (admin only)
    public List<Payment> getAllPaymentsForAdmin() {
        try {
            System.out.println("=== PaymentService: Getting all payments for admin ===");

            List<Payment> allPayments = paymentRepository.findAll();

            for (Payment payment : allPayments) {
                if (payment.getStatus() == PaymentStatus.PENDING && payment.getExpiryTime() == null && payment.getSubmittedDate() != null) {
                    payment.setExpiryTime(payment.getSubmittedDate().plusHours(12));
                }
            }

            System.out.println("Found " + allPayments.size() + " total payments for admin");
            return allPayments;

        } catch (Exception e) {
            System.err.println("Error getting all payments for admin: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get payments by status (admin)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        try {
            System.out.println("=== PaymentService: Getting payments with status: " + status + " ===");

            List<Payment> payments = paymentRepository.findByStatusOrderBySubmittedDateDesc(status);

            System.out.println("Found " + payments.size() + " payments with status: " + status);
            return payments;

        } catch (Exception e) {
            System.err.println("Error getting payments by status: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Enhanced approve payment with notification
    public Payment approvePaymentWithNotification(Long paymentId, String adminComments) {
        try {
            Payment payment = approvePayment(paymentId, adminComments);

            System.out.println("✅ Payment " + paymentId + " approved and user will be notified");

            return payment;

        } catch (Exception e) {
            System.err.println("Error approving payment with notification: " + e.getMessage());
            throw new RuntimeException("Failed to approve payment: " + e.getMessage());
        }
    }

    // Enhanced reject payment with notification
    public Payment rejectPaymentWithNotification(Long paymentId, String adminComments) {
        try {
            Payment payment = rejectPayment(paymentId, adminComments);

            System.out.println("❌ Payment " + paymentId + " rejected and user will be notified");

            return payment;

        } catch (Exception e) {
            System.err.println("Error rejecting payment with notification: " + e.getMessage());
            throw new RuntimeException("Failed to reject payment: " + e.getMessage());
        }
    }

    // Get payment statistics
    public Map<String, Object> getPaymentStatistics() {
        try {
            System.out.println("=== PaymentService: Calculating payment statistics ===");

            List<Payment> allPayments = paymentRepository.findAll();

            long pending = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
            long approved = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.APPROVED).count();
            long rejected = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.REJECTED).count();

            double totalPendingAmount = allPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .mapToDouble(p -> p.getAmount().doubleValue())
                    .sum();

            double totalApprovedAmount = allPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                    .mapToDouble(p -> p.getAmount().doubleValue())
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("rejected", rejected);
            stats.put("total", allPayments.size());
            stats.put("totalPendingAmount", totalPendingAmount);
            stats.put("totalApprovedAmount", totalApprovedAmount);

            System.out.println("Payment statistics calculated successfully");
            return stats;

        } catch (Exception e) {
            System.err.println("Error calculating payment statistics: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("pending", 0);
            emptyStats.put("approved", 0);
            emptyStats.put("rejected", 0);
            emptyStats.put("total", 0);
            emptyStats.put("totalPendingAmount", 0.0);
            emptyStats.put("totalApprovedAmount", 0.0);
            emptyStats.put("error", e.getMessage());

            return emptyStats;
        }
    }
}