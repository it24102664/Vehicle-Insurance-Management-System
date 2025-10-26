package com.example.Insurance.service;

import com.example.Insurance.DTO.AdminReportDTO;
import com.example.Insurance.DTO.AdminReportPaymentDTO;
import com.example.Insurance.entity.AdminReport;
import com.example.Insurance.entity.AdminReportPayment;
import com.example.Insurance.entity.UserR;
import com.example.Insurance.entity.UserReport;
import com.example.Insurance.model.User; // Your existing User model
import com.example.Insurance.repository.AdminReportRepository;
import com.example.Insurance.repository.AdminReportPaymentRepository;
import com.example.Insurance.repository.userRRepository;
import com.example.Insurance.repository.UserReportRepository;
import com.example.Insurance.repository.UserRepository; // Your existing UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminReportService {

    @Autowired
    private AdminReportRepository adminReportRepository;

    @Autowired
    private AdminReportPaymentRepository adminReportPaymentRepository;

    @Autowired
    private userRRepository userRRepository; // Report system users

    @Autowired
    private UserRepository userRepository; // Your existing website users

    @Autowired
    private UserReportRepository userReportRepository;

    // Create admin report and send to existing website user by NIC
    @Transactional(rollbackFor = Exception.class)
    public AdminReportDTO createAdminReport(AdminReportDTO adminReportDTO) {
        try {
            System.out.println("🔄 Creating admin report for website user: " + adminReportDTO.getCustomerName());
            System.out.println("📋 Target NIC: " + adminReportDTO.getCustomerNIC());

            // Validate input data
            validateAdminReportData(adminReportDTO);

            // Check if report already exists
            Optional<AdminReport> existingReport = adminReportRepository
                    .findByCustomerNICAndReportYear(adminReportDTO.getCustomerNIC(), adminReportDTO.getReportYear());

            if (existingReport.isPresent()) {
                throw new RuntimeException("Report already exists for NIC " +
                        adminReportDTO.getCustomerNIC() + " in year " +
                        adminReportDTO.getReportYear());
            }

            // Step 1: Verify user exists in your website
            Optional<User> websiteUser = userRepository.findByNic(adminReportDTO.getCustomerNIC());
            if (websiteUser.isEmpty()) {
                throw new RuntimeException("No website user found with NIC: " + adminReportDTO.getCustomerNIC() +
                        ". Please ensure the user is registered on your website first.");
            }

            User existingWebsiteUser = websiteUser.get();
            System.out.println("✅ Found website user: " + existingWebsiteUser.getName() + " (ID: " + existingWebsiteUser.getId() + ")");

            // Step 2: Create admin report
            AdminReport savedReport = createAndSaveAdminReport(adminReportDTO);

            // Step 3: Create payment records
            createPaymentRecords(adminReportDTO, savedReport);

            // Step 4: Link to your website user through report system
            linkReportToWebsiteUser(savedReport, existingWebsiteUser);

            System.out.println("🎯 SUCCESS: Report sent to website user: " + existingWebsiteUser.getName() + " (" + adminReportDTO.getCustomerNIC() + ")");

            return convertToDTO(savedReport);

        } catch (Exception e) {
            System.err.println("❌ ERROR creating admin report: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create admin report: " + e.getMessage(), e);
        }
    }

    // Link report to your existing website user
    private void linkReportToWebsiteUser(AdminReport adminReport, User websiteUser) {
        System.out.println("🔗 Linking report to website user: " + websiteUser.getName() + " (NIC: " + websiteUser.getNic() + ")");

        // Find or create UserR entry for report system
        UserR reportUser = findOrCreateReportUser(websiteUser);

        // Check if UserReport link already exists
        Optional<UserReport> existingLink = userReportRepository
                .findByUserIdAndAdminReportId(reportUser.getId(), adminReport.getId());

        if (existingLink.isEmpty()) {
            UserReport userReport = new UserReport();
            userReport.setUserR(reportUser);
            userReport.setAdminReport(adminReport);
            userReport.setCreatedDate(LocalDateTime.now());
            userReport.setIsFavorite(false);

            UserReport savedUserReport = userReportRepository.save(userReport);
            System.out.println("✅ Successfully linked report to website user:");
            System.out.println("   - UserReport ID: " + savedUserReport.getId());
            System.out.println("   - Website User: " + websiteUser.getName() + " (" + websiteUser.getNic() + ")");
            System.out.println("   - Report: " + adminReport.getReportYear() + " - " + adminReport.getCustomerName());
            System.out.println("🎯 Website user can now see this report in their dashboard!");
        } else {
            System.out.println("ℹ️ Report already linked to website user");
        }
    }

    // Find or create UserR entry based on your website User
    private UserR findOrCreateReportUser(User websiteUser) {
        Optional<UserR> existingReportUser = userRRepository.findByNic(websiteUser.getNic());

        if (existingReportUser.isPresent()) {
            UserR reportUser = existingReportUser.get();
            System.out.println("✅ Found existing report user: " + reportUser.getFullName() + " (ID: " + reportUser.getId() + ")");
            return reportUser;
        } else {
            // Create UserR entry based on website User data
            UserR newReportUser = new UserR();
            newReportUser.setFullName(websiteUser.getName());
            newReportUser.setNic(websiteUser.getNic());
            newReportUser.setPhone(websiteUser.getTelephone());
            newReportUser.setEmail(websiteUser.getEmail());
            newReportUser.setAddress(websiteUser.getAddress());
            newReportUser.setCreatedDate(LocalDateTime.now());
            newReportUser.setIsActive(true);

            UserR savedReportUser = userRRepository.save(newReportUser);
            System.out.println("✅ Created report user entry: " + savedReportUser.getFullName() + " (ID: " + savedReportUser.getId() + ")");
            return savedReportUser;
        }
    }

    // Create admin report
    private AdminReport createAndSaveAdminReport(AdminReportDTO adminReportDTO) {
        System.out.println("📋 Creating admin report...");

        // Calculate totals
        double totalAmount = adminReportDTO.getPaymentData().stream()
                .mapToDouble(AdminReportPaymentDTO::getAmount)
                .sum();

        int paidMonths = adminReportDTO.getPaymentData().size();
        double averageAmount = paidMonths > 0 ? totalAmount / paidMonths : 0;

        // Create AdminReport entity
        AdminReport adminReport = new AdminReport();
        adminReport.setCustomerName(adminReportDTO.getCustomerName());
        adminReport.setCustomerNIC(adminReportDTO.getCustomerNIC());
        adminReport.setCustomerPhone(adminReportDTO.getCustomerPhone());
        adminReport.setReportYear(adminReportDTO.getReportYear());
        adminReport.setAppliedPolicies(adminReportDTO.getAppliedPolicies());
        adminReport.setClaimsInfo(adminReportDTO.getClaimsInfo());
        adminReport.setClaimDate(adminReportDTO.getClaimDate());
        adminReport.setPaidMonths(paidMonths);
        adminReport.setTotalAmount(totalAmount);
        adminReport.setAverageAmount(averageAmount);
        adminReport.setCreatedBy(adminReportDTO.getCreatedBy());
        adminReport.setCreatedDate(LocalDateTime.now());

        AdminReport savedReport = adminReportRepository.save(adminReport);
        System.out.println("✅ AdminReport saved with ID: " + savedReport.getId());
        return savedReport;
    }

    // Create payment records
    private void createPaymentRecords(AdminReportDTO adminReportDTO, AdminReport savedReport) {
        System.out.println("💰 Creating payment records...");

        List<AdminReportPayment> payments = adminReportDTO.getPaymentData().stream()
                .map(paymentDTO -> {
                    AdminReportPayment payment = new AdminReportPayment();
                    payment.setPaymentMonth(paymentDTO.getMonth());
                    payment.setPaymentAmount(paymentDTO.getAmount());
                    payment.setAdminReport(savedReport);
                    return payment;
                })
                .collect(Collectors.toList());

        List<AdminReportPayment> savedPayments = adminReportPaymentRepository.saveAll(payments);
        System.out.println("✅ Created " + savedPayments.size() + " payment records");

        savedReport.setPayments(savedPayments);
    }

    // Get all website users for admin selection
    public List<User> getAllWebsiteUsers() {
        return userRepository.findByEnabled(true);
    }


    // Add this complete method to your AdminReportService.java

    @Transactional(rollbackFor = Exception.class)
    public AdminReportDTO updateAdminReport(Long id, AdminReportDTO adminReportDTO) {
        try {
            AdminReport existingReport = adminReportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin report not found with id: " + id));

            System.out.println("🔄 Updating report: " + existingReport.getCustomerName() + " (ID: " + id + ")");

            // Validate input data
            validateAdminReportData(adminReportDTO);

            // Update basic information (keep NIC to maintain user link)
            existingReport.setCustomerName(adminReportDTO.getCustomerName());
            existingReport.setCustomerPhone(adminReportDTO.getCustomerPhone());
            existingReport.setReportYear(adminReportDTO.getReportYear());
            existingReport.setAppliedPolicies(adminReportDTO.getAppliedPolicies());
            existingReport.setClaimsInfo(adminReportDTO.getClaimsInfo());
            existingReport.setClaimDate(adminReportDTO.getClaimDate());
            existingReport.setUpdatedDate(LocalDateTime.now());

            // Delete existing payments
            adminReportPaymentRepository.deleteByAdminReportId(id);

            // Calculate new totals
            double totalAmount = adminReportDTO.getPaymentData().stream()
                    .mapToDouble(AdminReportPaymentDTO::getAmount)
                    .sum();

            int paidMonths = adminReportDTO.getPaymentData().size();
            double averageAmount = paidMonths > 0 ? totalAmount / paidMonths : 0;

            existingReport.setPaidMonths(paidMonths);
            existingReport.setTotalAmount(totalAmount);
            existingReport.setAverageAmount(averageAmount);

            // Create new payment records
            List<AdminReportPayment> newPayments = adminReportDTO.getPaymentData().stream()
                    .map(paymentDTO -> {
                        AdminReportPayment payment = new AdminReportPayment();
                        payment.setPaymentMonth(paymentDTO.getMonth());
                        payment.setPaymentAmount(paymentDTO.getAmount());
                        payment.setAdminReport(existingReport);
                        return payment;
                    })
                    .collect(Collectors.toList());

            adminReportPaymentRepository.saveAll(newPayments);
            AdminReport updatedReport = adminReportRepository.save(existingReport);
            updatedReport.setPayments(newPayments);

            System.out.println("✅ Report updated successfully - changes visible in user dashboard");

            return convertToDTO(updatedReport);

        } catch (Exception e) {
            System.err.println("❌ Error updating admin report: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update admin report: " + e.getMessage(), e);
        }
    }





    // Search website users by name or NIC
    public List<User> searchWebsiteUsers(String query) {
        List<User> allUsers = userRepository.findByEnabled(true);

        if (query == null || query.trim().isEmpty()) {
            return allUsers;
        }

        String lowerQuery = query.toLowerCase();
        return allUsers.stream()
                .filter(user ->
                        user.getName().toLowerCase().contains(lowerQuery) ||
                                user.getNic().toLowerCase().contains(lowerQuery) ||
                                user.getEmail().toLowerCase().contains(lowerQuery)
                )
                .collect(Collectors.toList());
    }

    // All other existing methods remain the same...
    @Transactional(readOnly = true)
    public Page<AdminReportDTO> getAllAdminReports(Pageable pageable) {
        return adminReportRepository.findAllByOrderByCreatedDateDesc(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AdminReportDTO> getAdminReportById(Long id) {
        return adminReportRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminReport(Long id) {
        try {
            AdminReport report = adminReportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin report not found with id: " + id));

            System.out.println("🗑️ Deleting report for: " + report.getCustomerName() + " (" + report.getCustomerNIC() + ")");

            // Delete in proper order
            userReportRepository.deleteByAdminReportId(id);
            adminReportPaymentRepository.deleteByAdminReportId(id);
            adminReportRepository.deleteById(id);

            System.out.println("✅ Report deleted and removed from user dashboard");

        } catch (Exception e) {
            System.err.println("❌ Error deleting admin report: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete admin report: " + e.getMessage(), e);
        }
    }

    // Validate admin report data
    private void validateAdminReportData(AdminReportDTO adminReportDTO) {
        if (adminReportDTO.getCustomerName() == null || adminReportDTO.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }

        if (adminReportDTO.getCustomerNIC() == null || adminReportDTO.getCustomerNIC().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer NIC is required");
        }

        if (adminReportDTO.getCustomerPhone() == null || adminReportDTO.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone is required");
        }

        if (adminReportDTO.getReportYear() == null) {
            throw new IllegalArgumentException("Report year is required");
        }

        if (adminReportDTO.getPaymentData() == null || adminReportDTO.getPaymentData().isEmpty()) {
            throw new IllegalArgumentException("Payment data is required");
        }
    }

    // Convert entity to DTO
    private AdminReportDTO convertToDTO(AdminReport adminReport) {
        AdminReportDTO dto = new AdminReportDTO();
        dto.setId(adminReport.getId());
        dto.setCustomerName(adminReport.getCustomerName());
        dto.setCustomerNIC(adminReport.getCustomerNIC());
        dto.setCustomerPhone(adminReport.getCustomerPhone());
        dto.setReportYear(adminReport.getReportYear());
        dto.setAppliedPolicies(adminReport.getAppliedPolicies());
        dto.setClaimsInfo(adminReport.getClaimsInfo());
        dto.setClaimDate(adminReport.getClaimDate());
        dto.setPaidMonths(adminReport.getPaidMonths());
        dto.setTotalAmount(adminReport.getTotalAmount());
        dto.setAverageAmount(adminReport.getAverageAmount());
        dto.setCreatedDate(adminReport.getCreatedDate());
        dto.setUpdatedDate(adminReport.getUpdatedDate());
        dto.setCreatedBy(adminReport.getCreatedBy());

        // Convert payments
        if (adminReport.getPayments() != null) {
            List<AdminReportPaymentDTO> paymentDTOs = adminReport.getPayments().stream()
                    .map(payment -> new AdminReportPaymentDTO(
                            payment.getPaymentMonth(),
                            payment.getPaymentAmount()))
                    .collect(Collectors.toList());
            dto.setPaymentData(paymentDTOs);
        }

        return dto;
    }
}
