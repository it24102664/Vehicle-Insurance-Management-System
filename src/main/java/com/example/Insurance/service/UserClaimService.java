// src/main/java/com/example/Insurance/service/UserClaimService.java
package com.example.Insurance.service;

import com.example.Insurance.DTO.ClaimDTO;
import com.example.Insurance.entity.Claim;
import com.example.Insurance.entity.UserC;
import com.example.Insurance.entity.UserClaimNotification;
import com.example.Insurance.repository.ClaimRepository;
import com.example.Insurance.repository.UserClaimRepository;
import com.example.Insurance.repository.UserClaimNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserClaimRepository userClaimRepository;

    @Autowired
    private UserClaimNotificationRepository userClaimNotificationRepository;

    // Submit new claim - COMPLETELY FIXED
    public ClaimDTO submitClaim(ClaimDTO claimDTO) {
        try {
            System.out.println("🚀 Starting claim submission for: " + claimDTO.getFullName());

            // STEP 1: Get or create user first
            UserC user = getOrCreateUser(claimDTO);
            System.out.println("✅ User obtained/created: " + user.getId() + " - " + user.getFullName());

            // STEP 2: Create claim
            Claim claim = new Claim();
            claim.setFullName(claimDTO.getFullName());
            claim.setAge(claimDTO.getAge());
            claim.setNic(claimDTO.getNic());
            claim.setPhone(claimDTO.getPhone());
            claim.setEmail(claimDTO.getEmail());
            claim.setVehicleNumber(claimDTO.getVehicleNumber());
            claim.setVehicleModel(claimDTO.getVehicleModel());
            claim.setChassisNumber(claimDTO.getChassisNumber());
            claim.setIncidentDate(claimDTO.getIncidentDate());
            claim.setIncidentType(claimDTO.getIncidentType());
            claim.setDescription(claimDTO.getDescription());

            // CRITICAL: Associate with user BEFORE saving
            claim.setUser(user);

            // Generate unique claim number
            claim.setClaimNumber(generateClaimNumber());
            claim.setSubmittedDate(LocalDateTime.now());
            claim.setStatus(Claim.ClaimStatus.PENDING);
            claim.setIsDuplicate(false);

            System.out.println("📝 Claim created with number: " + claim.getClaimNumber());

            // STEP 3: Check for duplicates BEFORE saving
            try {
                List<Claim> potentialDuplicates = claimRepository.findPotentialDuplicates(
                        claim.getNic(), claim.getVehicleNumber()
                );
                if (!potentialDuplicates.isEmpty()) {
                    claim.setIsDuplicate(true);
                    System.out.println("⚠️ Potential duplicate detected!");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error checking duplicates (non-critical): " + e.getMessage());
                // Continue with submission even if duplicate check fails
            }

            // STEP 4: Save claim
            Claim savedClaim = claimRepository.save(claim);
            System.out.println("💾 Claim saved successfully with ID: " + savedClaim.getId());

            // STEP 5: Create notification (non-critical - don't fail if this breaks)
            try {
                createUserClaimNotification(user, savedClaim,
                        "Claim Submitted Successfully",
                        "Your claim #" + savedClaim.getClaimNumber() + " has been submitted and is being reviewed.",
                        UserClaimNotification.NotificationType.CLAIM_SUBMITTED);
                System.out.println("📧 Notification sent to user");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to create notification (non-critical): " + e.getMessage());
                // Don't fail the entire operation for notification issues
            }

            // STEP 6: Convert and return
            ClaimDTO result = convertToDTO(savedClaim);
            System.out.println("🎉 Claim submission completed successfully: " + savedClaim.getClaimNumber());

            return result;

        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in claim submission: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit claim: " + e.getMessage(), e);
        }
    }

    // Get user claims by NIC
    public List<ClaimDTO> getUserClaimsByNic(String nic) {
        try {
            Optional<UserC> userOpt = userClaimRepository.findByNic(nic);
            if (!userOpt.isPresent()) {
                System.out.println("⚠️ User not found with NIC: " + nic);
                return List.of(); // Return empty list if user not found
            }

            UserC user = userOpt.get();
            List<Claim> claims = claimRepository.findByUserIdOrderBySubmittedDateDesc(user.getId());
            System.out.println("📋 Found " + claims.size() + " claims for user: " + user.getFullName());

            return claims.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error getting user claims: " + e.getMessage());
            return List.of();
        }
    }

    // Get user claims by status
    public List<ClaimDTO> getUserClaimsByStatus(String nic, Claim.ClaimStatus status) {
        try {
            Optional<UserC> userOpt = userClaimRepository.findByNic(nic);
            if (!userOpt.isPresent()) {
                return List.of();
            }

            UserC user = userOpt.get();
            List<Claim> claims = claimRepository.findByUserIdAndStatusOrderBySubmittedDateDesc(user.getId(), status);
            return claims.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error getting user claims by status: " + e.getMessage());
            return List.of();
        }
    }

    // Get user claim statistics
    public Map<String, Long> getUserClaimStatistics(String nic) {
        try {
            Optional<UserC> userOpt = userClaimRepository.findByNic(nic);
            Map<String, Long> stats = new HashMap<>();

            if (!userOpt.isPresent()) {
                stats.put("pending", 0L);
                stats.put("approved", 0L);
                stats.put("rejected", 0L);
                stats.put("total", 0L);
                return stats;
            }

            UserC user = userOpt.get();
            stats.put("pending", claimRepository.countByUserIdAndStatus(user.getId(), Claim.ClaimStatus.PENDING));
            stats.put("approved", claimRepository.countByUserIdAndStatus(user.getId(), Claim.ClaimStatus.APPROVED));
            stats.put("rejected", claimRepository.countByUserIdAndStatus(user.getId(), Claim.ClaimStatus.REJECTED));
            stats.put("total", claimRepository.countByUserId(user.getId()));

            return stats;
        } catch (Exception e) {
            System.err.println("❌ Error getting user statistics: " + e.getMessage());
            Map<String, Long> emptyStats = new HashMap<>();
            emptyStats.put("pending", 0L);
            emptyStats.put("approved", 0L);
            emptyStats.put("rejected", 0L);
            emptyStats.put("total", 0L);
            return emptyStats;
        }
    }

    // Get user notifications
    public List<UserClaimNotification> getUserClaimNotifications(String nic) {
        try {
            Optional<UserC> userOpt = userClaimRepository.findByNic(nic);
            if (!userOpt.isPresent()) {
                return List.of();
            }

            UserC user = userOpt.get();
            return userClaimNotificationRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
        } catch (Exception e) {
            System.err.println("❌ Error getting user notifications: " + e.getMessage());
            return List.of();
        }
    }

    // Mark notification as read
    public void markUserClaimNotificationAsRead(Long notificationId) {
        try {
            Optional<UserClaimNotification> notificationOpt = userClaimNotificationRepository.findById(notificationId);
            if (notificationOpt.isPresent()) {
                UserClaimNotification notification = notificationOpt.get();
                notification.setIsRead(true);
                userClaimNotificationRepository.save(notification);
                System.out.println("✅ Notification " + notificationId + " marked as read");
            } else {
                throw new RuntimeException("Notification not found with id: " + notificationId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error marking notification as read: " + e.getMessage());
            throw new RuntimeException("Failed to mark notification as read: " + e.getMessage());
        }
    }

    // FIXED Helper method - proper user creation with validation
    private UserC getOrCreateUser(ClaimDTO claimDTO) {
        try {
            // Try to find existing user by NIC
            Optional<UserC> existingUser = userClaimRepository.findByNic(claimDTO.getNic());

            if (existingUser.isPresent()) {
                UserC user = existingUser.get();
                System.out.println("🔍 Found existing user: " + user.getFullName() + " (ID: " + user.getId() + ")");

                // Update user information if needed
                boolean updated = false;
                if (!user.getEmail().equals(claimDTO.getEmail())) {
                    user.setEmail(claimDTO.getEmail());
                    updated = true;
                }
                if (!user.getFullName().equals(claimDTO.getFullName())) {
                    user.setFullName(claimDTO.getFullName());
                    updated = true;
                }
                if (!user.getPhone().equals(claimDTO.getPhone())) {
                    user.setPhone(claimDTO.getPhone());
                    updated = true;
                }
                if (user.getAge() == null || !user.getAge().equals(claimDTO.getAge())) {
                    user.setAge(claimDTO.getAge());
                    updated = true;
                }

                if (updated) {
                    user = userClaimRepository.save(user);
                    System.out.println("🔄 Updated existing user information");
                }

                return user;
            }

            // Create new user
            UserC newUser = new UserC();
            newUser.setFullName(claimDTO.getFullName());
            newUser.setEmail(claimDTO.getEmail());
            newUser.setNic(claimDTO.getNic());
            newUser.setPhone(claimDTO.getPhone());
            newUser.setAge(claimDTO.getAge());
            newUser.setCreatedDate(LocalDateTime.now());

            UserC savedUser = userClaimRepository.save(newUser);
            System.out.println("✨ Created new user: " + savedUser.getFullName() + " (ID: " + savedUser.getId() + ")");

            return savedUser;

        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR creating/finding user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create or find user: " + e.getMessage(), e);
        }
    }

    // FIXED Notification creation
    public void createUserClaimNotification(UserC user, Claim claim, String title, String message, UserClaimNotification.NotificationType type) {
        try {
            UserClaimNotification notification = new UserClaimNotification();
            notification.setUser(user);
            notification.setClaim(claim);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setIsRead(false);
            notification.setCreatedDate(LocalDateTime.now());

            userClaimNotificationRepository.save(notification);
            System.out.println("📧 Notification created for user: " + user.getFullName() + " - " + title);
        } catch (Exception e) {
            System.err.println("❌ Error creating notification: " + e.getMessage());
            // Don't throw exception - notifications are not critical
        }
    }

    // Convert claim to DTO
    private ClaimDTO convertToDTO(Claim claim) {
        ClaimDTO dto = new ClaimDTO();
        dto.setId(claim.getId());
        dto.setClaimNumber(claim.getClaimNumber());
        dto.setFullName(claim.getFullName());
        dto.setAge(claim.getAge());
        dto.setNic(claim.getNic());
        dto.setPhone(claim.getPhone());
        dto.setEmail(claim.getEmail());
        dto.setVehicleNumber(claim.getVehicleNumber());
        dto.setVehicleModel(claim.getVehicleModel());
        dto.setChassisNumber(claim.getChassisNumber());
        dto.setIncidentDate(claim.getIncidentDate());
        dto.setIncidentType(claim.getIncidentType());
        dto.setDescription(claim.getDescription());
        dto.setStatus(claim.getStatus());
        dto.setAdminReason(claim.getAdminReason());
        dto.setSubmittedDate(claim.getSubmittedDate());
        dto.setProcessedDate(claim.getProcessedDate());
        dto.setIsDuplicate(claim.getIsDuplicate());
        return dto;
    }

    // Generate unique claim number
    private String generateClaimNumber() {
        return "CLM" + System.currentTimeMillis();
    }
}
