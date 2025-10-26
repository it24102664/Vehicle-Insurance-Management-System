package com.example.Insurance.service;

import com.example.Insurance.DTO.UserDTO;
import com.example.Insurance.DTO.UserReportDTO;
import com.example.Insurance.DTO.AdminReportPaymentDTO;
import com.example.Insurance.entity.UserR;
import com.example.Insurance.entity.UserReport;
import com.example.Insurance.entity.AdminReport;
import com.example.Insurance.repository.userRRepository;
import com.example.Insurance.repository.UserReportRepository;
import com.example.Insurance.repository.AdminReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserReportService {

    @Autowired
    private userRRepository userRepository;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private AdminReportRepository adminReportRepository;

    // Search users method - made non-static and properly implemented
    public List<UserDTO> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Return all active users if no query
            Pageable pageable = PageRequest.of(0, 50);
            return userRepository.findByIsActiveTrue(pageable)
                    .getContent()
                    .stream()
                    .map(this::convertUserToDTO)
                    .collect(Collectors.toList());
        }

        // Search by name or NIC
        List<UserR> users = userRepository.searchByNameOrNic(query);

        return users.stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
    }

    // Add this method to your UserReportService.java

    /**
     * Check if a report belongs to a specific user by NIC
     * This ensures privacy - users can only access their own reports
     */
    public boolean isReportOwnedByUser(Long reportId, String userNIC) {
        try {
            System.out.println("🔒 OWNERSHIP CHECK: Report " + reportId + " for NIC: " + userNIC);

            // Find the user by NIC
            Optional<UserR> userOpt = userRepository.findByNic(userNIC);
            if (userOpt.isEmpty()) {
                System.out.println("❌ OWNERSHIP CHECK FAILED: User not found for NIC: " + userNIC);
                return false;
            }

            // Check if the report exists and belongs to this user
            Optional<UserReport> userReportOpt = userReportRepository.findById(reportId);
            if (userReportOpt.isEmpty()) {
                System.out.println("❌ OWNERSHIP CHECK FAILED: Report not found: " + reportId);
                return false;
            }

            UserReport userReport = userReportOpt.get();
            boolean isOwner = userReport.getUserR().getNic().equals(userNIC);

            if (isOwner) {
                System.out.println("✅ OWNERSHIP CHECK PASSED: User owns report " + reportId);
            } else {
                System.out.println("🚫 OWNERSHIP CHECK FAILED: Report " + reportId + " belongs to " +
                        userReport.getUserR().getNic() + ", not " + userNIC);
            }

            return isOwner;

        } catch (Exception e) {
            System.err.println("❌ OWNERSHIP CHECK ERROR: " + e.getMessage());
            return false;
        }
    }



    // Get user reports by NIC with better error handling
    public List<UserReportDTO> getUserReportsByNic(String nic) {
        System.out.println("🔄 Fetching reports for NIC: " + nic);

        // First check if user exists, if not create them
        UserR user = userRepository.findByNic(nic).orElse(null);

        if (user == null) {
            System.out.println("❌ User not found with NIC: " + nic);

            // Check if there are admin reports for this NIC
            List<AdminReport> adminReports = adminReportRepository.findByCustomerNICOrderByCreatedDateDesc(nic);

            if (!adminReports.isEmpty()) {
                System.out.println("✅ Found " + adminReports.size() + " admin reports, creating user automatically");

                // Create user from first admin report
                AdminReport firstReport = adminReports.get(0);
                user = new UserR();
                user.setFullName(firstReport.getCustomerName());
                user.setNic(firstReport.getCustomerNIC());
                user.setPhone(firstReport.getCustomerPhone());
                user.setCreatedDate(LocalDateTime.now());
                user.setIsActive(true);

                user = userRepository.save(user);
                System.out.println("✅ Created user: " + user.getFullName() + " with ID: " + user.getId());

                // Link all admin reports to this user
                for (AdminReport adminReport : adminReports) {
                    if (!userReportRepository.existsByUserIdAndAdminReportId(user.getId(), adminReport.getId())) {
                        UserReport userReport = new UserReport();
                        userReport.setUserR(user); // Fixed: use setUserR instead of setUser
                        userReport.setAdminReport(adminReport);
                        userReport.setCreatedDate(LocalDateTime.now());
                        userReport.setIsFavorite(false);

                        userReportRepository.save(userReport);
                        System.out.println("✅ Linked admin report " + adminReport.getId() + " to user");
                    }
                }
            } else {
                System.out.println("❌ No admin reports found for NIC: " + nic);
                return List.of(); // Return empty list instead of throwing exception
            }
        }

        List<UserReport> userReports = userReportRepository.findByUserNicOrderByCreatedDateDesc(nic);
        System.out.println("✅ Found " + userReports.size() + " user reports for NIC: " + nic);

        return userReports.stream()
                .map(this::convertUserReportToDTO)
                .collect(Collectors.toList());
    }

    // Dashboard Statistics with null safety
    public UserDashboardStatsDTO getUserDashboardStats(String nic) {
        System.out.println("🔄 Getting dashboard stats for NIC: " + nic);

        Optional<UserR> userOpt = userRepository.findByNic(nic);

        if (userOpt.isEmpty()) {
            System.out.println("❌ User not found for dashboard stats, returning zeros");
            return new UserDashboardStatsDTO(0L, 0.0, 0L, 0L);
        }

        UserR user = userOpt.get();

        Long totalReports = userReportRepository.countByUserId(user.getId());
        Double totalPayments = userReportRepository.getTotalPaymentAmountByUserId(user.getId());

        if (totalPayments == null) totalPayments = 0.0;

        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<UserReport> recentReports = userReportRepository.findRecentReportsByUserId(user.getId(), sixMonthsAgo);
        Long recentReportsCount = (long) recentReports.size();

        List<UserReport> favoriteReports = userReportRepository.findByUserIdAndIsFavoriteTrueOrderByCreatedDateDesc(user.getId());
        Long favoriteReportsCount = (long) favoriteReports.size();

        System.out.println("✅ Dashboard stats - Total: " + totalReports + ", Payments: " + totalPayments + ", Recent: " + recentReportsCount + ", Favorites: " + favoriteReportsCount);

        return new UserDashboardStatsDTO(totalReports, totalPayments, recentReportsCount, favoriteReportsCount);
    }

    // Create user
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByNic(userDTO.getNic())) {
            throw new RuntimeException("User with NIC " + userDTO.getNic() + " already exists");
        }

        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("User with email " + userDTO.getEmail() + " already exists");
        }

        UserR user = new UserR();
        user.setFullName(userDTO.getFullName());
        user.setNic(userDTO.getNic());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setDateOfBirth(userDTO.getDateOfBirth());

        UserR savedUser = userRepository.save(user);
        return convertUserToDTO(savedUser);
    }

    // Get user by ID
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertUserToDTO);
    }

    // Get user by NIC
    public Optional<UserDTO> getUserByNic(String nic) {
        return userRepository.findByNic(nic)
                .map(this::convertUserToDTO);
    }

    // Get all users - FIXED METHOD
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        System.out.println("🔄 Getting all users with pageable: " + pageable);

        Page<UserR> users = userRepository.findByIsActiveTrue(pageable);
        System.out.println("✅ Found " + users.getTotalElements() + " active users");

        return users.map(this::convertUserToDTO);
    }

    // Update user
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        UserR user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setUpdatedDate(LocalDateTime.now());

        UserR updatedUser = userRepository.save(user);
        return convertUserToDTO(updatedUser);
    }

    // Delete user
    public void deleteUser(Long id) {
        UserR user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setIsActive(false);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
    }

    // Other user report operations
    public List<UserReportDTO> getUserReportsByUserId(Long userId) {
        List<UserReport> userReports = userReportRepository.findByUserIdOrderByCreatedDateDesc(userId);
        return userReports.stream()
                .map(this::convertUserReportToDTO)
                .collect(Collectors.toList());
    }

    public List<UserReportDTO> getUserReportsByNicAndYear(String nic, Integer year) {
        List<UserReport> userReports = userReportRepository.findByUserNicAndReportYear(nic, year);
        return userReports.stream()
                .map(this::convertUserReportToDTO)
                .collect(Collectors.toList());
    }

    public List<UserReportDTO> getRecentUserReports(String nic) {
        UserR user = userRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("User not found with NIC: " + nic));

        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<UserReport> userReports = userReportRepository.findRecentReportsByUserId(user.getId(), sixMonthsAgo);

        return userReports.stream()
                .map(this::convertUserReportToDTO)
                .collect(Collectors.toList());
    }

    public List<UserReportDTO> getFavoriteUserReports(String nic) {
        UserR user = userRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("User not found with NIC: " + nic));

        List<UserReport> userReports = userReportRepository.findByUserIdAndIsFavoriteTrueOrderByCreatedDateDesc(user.getId());
        return userReports.stream()
                .map(this::convertUserReportToDTO)
                .collect(Collectors.toList());
    }

    public UserReportDTO updateUserReportNotes(Long userReportId, String notes) {
        UserReport userReport = userReportRepository.findById(userReportId)
                .orElseThrow(() -> new RuntimeException("User report not found with id: " + userReportId));

        userReport.setUserNotes(notes);
        userReport.setUpdatedDate(LocalDateTime.now());

        UserReport updatedUserReport = userReportRepository.save(userReport);
        return convertUserReportToDTO(updatedUserReport);
    }

    public UserReportDTO toggleFavoriteReport(Long userReportId) {
        UserReport userReport = userReportRepository.findById(userReportId)
                .orElseThrow(() -> new RuntimeException("User report not found with id: " + userReportId));

        userReport.setIsFavorite(!userReport.getIsFavorite());
        userReport.setUpdatedDate(LocalDateTime.now());

        UserReport updatedUserReport = userReportRepository.save(userReport);
        return convertUserReportToDTO(updatedUserReport);
    }

    public void markReportAsViewed(Long userReportId) {
        UserReport userReport = userReportRepository.findById(userReportId)
                .orElseThrow(() -> new RuntimeException("User report not found with id: " + userReportId));

        userReport.setViewedDate(LocalDateTime.now());
        userReportRepository.save(userReport);
    }

    public void deleteUserReport(Long userReportId) {
        if (!userReportRepository.existsById(userReportId)) {
            throw new RuntimeException("User report not found with id: " + userReportId);
        }
        userReportRepository.deleteById(userReportId);
    }

    // Conversion methods
    private UserDTO convertUserToDTO(UserR user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setNic(user.getNic());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setUpdatedDate(user.getUpdatedDate());
        dto.setIsActive(user.getIsActive());

        // Calculate statistics safely
        Long totalReports = userReportRepository.countByUserId(user.getId());
        Double totalPayments = userReportRepository.getTotalPaymentAmountByUserId(user.getId());

        dto.setTotalReports(totalReports != null ? totalReports.intValue() : 0);
        dto.setTotalPayments(totalPayments != null ? totalPayments : 0.0);

        return dto;
    }

    private UserReportDTO convertUserReportToDTO(UserReport userReport) {
        UserReportDTO dto = new UserReportDTO();
        dto.setId(userReport.getId());
        dto.setUserId(userReport.getUserR().getId()); // Fixed: use getUserR()
        dto.setAdminReportId(userReport.getAdminReport().getId());
        dto.setUserNotes(userReport.getUserNotes());
        dto.setIsFavorite(userReport.getIsFavorite());
        dto.setViewedDate(userReport.getViewedDate());
        dto.setCreatedDate(userReport.getCreatedDate());
        dto.setUpdatedDate(userReport.getUpdatedDate());

        // Copy admin report details
        AdminReport adminReport = userReport.getAdminReport();
        dto.setCustomerName(adminReport.getCustomerName());
        dto.setCustomerNIC(adminReport.getCustomerNIC());
        dto.setCustomerPhone(adminReport.getCustomerPhone());
        dto.setReportYear(adminReport.getReportYear());
        dto.setAppliedPolicies(adminReport.getAppliedPolicies());
        dto.setClaimsInfo(adminReport.getClaimsInfo());
        dto.setClaimDate(adminReport.getClaimDate() != null ? adminReport.getClaimDate().atStartOfDay() : null);
        dto.setPaidMonths(adminReport.getPaidMonths());
        dto.setTotalAmount(adminReport.getTotalAmount());
        dto.setAverageAmount(adminReport.getAverageAmount());

        // Convert payment data
        if (adminReport.getPayments() != null) {
            List<AdminReportPaymentDTO> paymentDTOs = adminReport.getPayments().stream()
                    .map(payment -> {
                        AdminReportPaymentDTO paymentDTO = new AdminReportPaymentDTO();
                        paymentDTO.setMonth(payment.getPaymentMonth());
                        paymentDTO.setAmount(payment.getPaymentAmount());
                        return paymentDTO;
                    })
                    .collect(Collectors.toList());
            dto.setPaymentData(paymentDTOs);
        }

        return dto;
    }

    // Dashboard Stats DTO
    public static class UserDashboardStatsDTO {
        private Long totalReports;
        private Double totalPayments;
        private Long recentReports;
        private Long favoriteReports;

        public UserDashboardStatsDTO(Long totalReports, Double totalPayments, Long recentReports, Long favoriteReports) {
            this.totalReports = totalReports;
            this.totalPayments = totalPayments;
            this.recentReports = recentReports;
            this.favoriteReports = favoriteReports;
        }

        // Getters and Setters
        public Long getTotalReports() { return totalReports; }
        public void setTotalReports(Long totalReports) { this.totalReports = totalReports; }

        public Double getTotalPayments() { return totalPayments; }
        public void setTotalPayments(Double totalPayments) { this.totalPayments = totalPayments; }

        public Long getRecentReports() { return recentReports; }
        public void setRecentReports(Long recentReports) { this.recentReports = recentReports; }

        public Long getFavoriteReports() { return favoriteReports; }
        public void setFavoriteReports(Long favoriteReports) { this.favoriteReports = favoriteReports; }
    }
}
