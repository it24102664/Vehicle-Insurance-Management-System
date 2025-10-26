package com.example.Insurance.service;

import com.example.Insurance.DTO.ClaimDTO;
import com.example.Insurance.entity.Claim;
import com.example.Insurance.entity.ClaimPhoto;
import com.example.Insurance.entity.UserC;
import com.example.Insurance.entity.UserClaimNotification;
import com.example.Insurance.repository.ClaimRepository;
import com.example.Insurance.repository.ClaimPhotoRepository;
import com.example.Insurance.repository.UserClaimRepository;
import com.example.Insurance.repository.UserClaimNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminClaimsService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimPhotoRepository claimPhotoRepository;

    @Autowired
    private UserClaimRepository userClaimRepository;

    @Autowired
    private UserClaimNotificationRepository userClaimNotificationRepository;

    // Get all claims
    public List<ClaimDTO> getAllClaims() {
        List<Claim> claims = claimRepository.findAllOrderBySubmittedDateDesc();
        return claims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get claims by status
    public List<ClaimDTO> getClaimsByStatus(Claim.ClaimStatus status) {
        List<Claim> claims = claimRepository.findByStatus(status);
        return claims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get duplicate claims
    public List<ClaimDTO> getDuplicateClaims() {
        List<Claim> claims = claimRepository.findByIsDuplicate(true);
        return claims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get claim by ID
    public ClaimDTO getClaimById(Long id) {
        Optional<Claim> claim = claimRepository.findById(id);
        if (claim.isPresent()) {
            return convertToDTO(claim.get());
        }
        throw new RuntimeException("Claim not found with id: " + id);
    }

    // Get claim by claim number
    public ClaimDTO getClaimByClaimNumber(String claimNumber) {
        Optional<Claim> claim = claimRepository.findByClaimNumber(claimNumber);
        if (claim.isPresent()) {
            return convertToDTO(claim.get());
        }
        throw new RuntimeException("Claim not found with claim number: " + claimNumber);
    }

    // Approve claim with user notification
    public ClaimDTO approveClaim(Long id, String adminReason) {
        Optional<Claim> optionalClaim = claimRepository.findById(id);
        if (optionalClaim.isPresent()) {
            Claim claim = optionalClaim.get();
            claim.setStatus(Claim.ClaimStatus.APPROVED);
            claim.setAdminReason(adminReason);
            claim.setProcessedDate(LocalDateTime.now());
            Claim savedClaim = claimRepository.save(claim);

            // Send notification to user
            sendUserNotification(claim, "Claim Approved! 🎉",
                    "Great news! Your claim #" + claim.getClaimNumber() + " has been approved. " +
                            (adminReason != null && !adminReason.isEmpty() ? "Reason: " + adminReason : "Processing will begin shortly."),
                    UserClaimNotification.NotificationType.CLAIM_APPROVED);

            return convertToDTO(savedClaim);
        }
        throw new RuntimeException("Claim not found with id: " + id);
    }

    // Reject claim with user notification
    public ClaimDTO rejectClaim(Long id, String adminReason) {
        Optional<Claim> optionalClaim = claimRepository.findById(id);
        if (optionalClaim.isPresent()) {
            Claim claim = optionalClaim.get();
            claim.setStatus(Claim.ClaimStatus.REJECTED);
            claim.setAdminReason(adminReason);
            claim.setProcessedDate(LocalDateTime.now());
            Claim savedClaim = claimRepository.save(claim);

            // Send notification to user
            sendUserNotification(claim, "Claim Update",
                    "Your claim #" + claim.getClaimNumber() + " has been rejected. " +
                            (adminReason != null && !adminReason.isEmpty() ? "Reason: " + adminReason : "Please contact support for more information."),
                    UserClaimNotification.NotificationType.CLAIM_REJECTED);

            return convertToDTO(savedClaim);
        }
        throw new RuntimeException("Claim not found with id: " + id);
    }

    // Get claim statistics
    public Map<String, Long> getClaimStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", claimRepository.countByStatus(Claim.ClaimStatus.PENDING));
        stats.put("approved", claimRepository.countByStatus(Claim.ClaimStatus.APPROVED));
        stats.put("rejected", claimRepository.countByStatus(Claim.ClaimStatus.REJECTED));
        stats.put("duplicates", (long) claimRepository.findByIsDuplicate(true).size());
        return stats;
    }

    // Create new claim (for testing) - Updated to include user relationship
    public ClaimDTO createClaim(ClaimDTO claimDTO) {
        Claim claim = convertToEntity(claimDTO);
        claim.setClaimNumber(generateClaimNumber());
        claim.setSubmittedDate(LocalDateTime.now());

        // Get or create user for the claim
        UserC user = getOrCreateUser(claimDTO);
        claim.setUser(user);

        // Check for duplicates
        List<Claim> potentialDuplicates = claimRepository.findPotentialDuplicates(
                claim.getNic(), claim.getVehicleNumber()
        );
        if (!potentialDuplicates.isEmpty()) {
            claim.setIsDuplicate(true);
        }

        Claim savedClaim = claimRepository.save(claim);

        // Send notification to user
        sendUserNotification(savedClaim, "Claim Submitted Successfully",
                "Your claim #" + savedClaim.getClaimNumber() + " has been submitted and is being reviewed by our team.",
                UserClaimNotification.NotificationType.CLAIM_SUBMITTED);

        return convertToDTO(savedClaim);
    }

    // Update claim
    public ClaimDTO updateClaim(Long id, ClaimDTO claimDTO) {
        Optional<Claim> optionalClaim = claimRepository.findById(id);
        if (optionalClaim.isPresent()) {
            Claim existingClaim = optionalClaim.get();
            updateClaimFromDTO(existingClaim, claimDTO);
            Claim updatedClaim = claimRepository.save(existingClaim);
            return convertToDTO(updatedClaim);
        }
        throw new RuntimeException("Claim not found with id: " + id);
    }

    // Delete claim
    public void deleteClaim(Long id) {
        if (claimRepository.existsById(id)) {
            claimRepository.deleteById(id);
        } else {
            throw new RuntimeException("Claim not found with id: " + id);
        }
    }

    // Get photos for claim
    public List<ClaimPhoto> getClaimPhotos(Long claimId) {
        return claimPhotoRepository.findByClaimId(claimId);
    }

    // Helper method to get or create user
    private UserC getOrCreateUser(ClaimDTO claimDTO) {
        return userClaimRepository.findByNic(claimDTO.getNic())
                .orElseGet(() -> {
                    UserC newUser = new UserC();
                    newUser.setFullName(claimDTO.getFullName());
                    newUser.setEmail(claimDTO.getEmail());
                    newUser.setNic(claimDTO.getNic());
                    newUser.setPhone(claimDTO.getPhone());
                    newUser.setAge(claimDTO.getAge());
                    return userClaimRepository.save(newUser);
                });
    }

    // Helper method to send user notifications
    private void sendUserNotification(Claim claim, String title, String message, UserClaimNotification.NotificationType type) {
        try {
            UserC user = null;

            // Try to get user from claim relationship first
            if (claim.getUser() != null) {
                user = claim.getUser();
            } else {
                // If no user relationship, try to find by NIC
                Optional<UserC> userOptional = userClaimRepository.findByNic(claim.getNic());
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    // Update the claim with user relationship
                    claim.setUser(user);
                    claimRepository.save(claim);
                }
            }

            // Send notification if user exists
            if (user != null) {
                UserClaimNotification notification = new UserClaimNotification(user, claim, title, message, type);
                userClaimNotificationRepository.save(notification);
                System.out.println("Notification sent to user: " + user.getFullName() + " for claim: " + claim.getClaimNumber());
            } else {
                System.out.println("Warning: Could not find user for claim: " + claim.getClaimNumber());
            }
        } catch (Exception e) {
            System.err.println("Error sending notification for claim " + claim.getClaimNumber() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper methods
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

    private Claim convertToEntity(ClaimDTO dto) {
        Claim claim = new Claim();
        claim.setId(dto.getId());
        claim.setClaimNumber(dto.getClaimNumber());
        claim.setFullName(dto.getFullName());
        claim.setAge(dto.getAge());
        claim.setNic(dto.getNic());
        claim.setPhone(dto.getPhone());
        claim.setEmail(dto.getEmail());
        claim.setVehicleNumber(dto.getVehicleNumber());
        claim.setVehicleModel(dto.getVehicleModel());
        claim.setChassisNumber(dto.getChassisNumber());
        claim.setIncidentDate(dto.getIncidentDate());
        claim.setIncidentType(dto.getIncidentType());
        claim.setDescription(dto.getDescription());
        claim.setStatus(dto.getStatus());
        claim.setAdminReason(dto.getAdminReason());
        claim.setSubmittedDate(dto.getSubmittedDate());
        claim.setProcessedDate(dto.getProcessedDate());
        claim.setIsDuplicate(dto.getIsDuplicate());
        return claim;
    }

    private void updateClaimFromDTO(Claim claim, ClaimDTO dto) {
        claim.setFullName(dto.getFullName());
        claim.setAge(dto.getAge());
        claim.setNic(dto.getNic());
        claim.setPhone(dto.getPhone());
        claim.setEmail(dto.getEmail());
        claim.setVehicleNumber(dto.getVehicleNumber());
        claim.setVehicleModel(dto.getVehicleModel());
        claim.setChassisNumber(dto.getChassisNumber());
        claim.setIncidentDate(dto.getIncidentDate());
        claim.setIncidentType(dto.getIncidentType());
        claim.setDescription(dto.getDescription());
    }

    private String generateClaimNumber() {
        return "CLM" + System.currentTimeMillis();
    }
}
