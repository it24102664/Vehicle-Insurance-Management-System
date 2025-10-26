package com.example.Insurance.controller;


import com.example.Insurance.DTO.ClaimDTO;
import com.example.Insurance.entity.Claim;
import com.example.Insurance.entity.ClaimPhoto;
import com.example.Insurance.service.AdminClaimsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/claims")
@CrossOrigin(origins = "*")
public class AdminClaimsController {

    @Autowired
    private AdminClaimsService adminClaimsService;

    // Get all claims
    @GetMapping
    public ResponseEntity<List<ClaimDTO>> getAllClaims() {
        List<ClaimDTO> claims = adminClaimsService.getAllClaims();
        return ResponseEntity.ok(claims);
    }

    // Get claims by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClaimDTO>> getClaimsByStatus(@PathVariable String status) {
        try {
            Claim.ClaimStatus claimStatus = Claim.ClaimStatus.valueOf(status.toUpperCase());
            List<ClaimDTO> claims = adminClaimsService.getClaimsByStatus(claimStatus);
            return ResponseEntity.ok(claims);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get duplicate claims
    @GetMapping("/duplicates")
    public ResponseEntity<List<ClaimDTO>> getDuplicateClaims() {
        List<ClaimDTO> duplicateClaims = adminClaimsService.getDuplicateClaims();
        return ResponseEntity.ok(duplicateClaims);
    }

    // Get claim by ID
    @GetMapping("/{id}")
    public ResponseEntity<ClaimDTO> getClaimById(@PathVariable Long id) {
        try {
            ClaimDTO claim = adminClaimsService.getClaimById(id);
            return ResponseEntity.ok(claim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get claim by claim number
    @GetMapping("/number/{claimNumber}")
    public ResponseEntity<ClaimDTO> getClaimByClaimNumber(@PathVariable String claimNumber) {
        try {
            ClaimDTO claim = adminClaimsService.getClaimByClaimNumber(claimNumber);
            return ResponseEntity.ok(claim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Approve claim
    @PutMapping("/{id}/approve")
    public ResponseEntity<ClaimDTO> approveClaim(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String adminReason = request.get("reason");
            ClaimDTO approvedClaim = adminClaimsService.approveClaim(id, adminReason);
            return ResponseEntity.ok(approvedClaim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Reject claim
    @PutMapping("/{id}/reject")
    public ResponseEntity<ClaimDTO> rejectClaim(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String adminReason = request.get("reason");
            ClaimDTO rejectedClaim = adminClaimsService.rejectClaim(id, adminReason);
            return ResponseEntity.ok(rejectedClaim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get claim statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getClaimStatistics() {
        Map<String, Long> statistics = adminClaimsService.getClaimStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Create new claim (for testing)
    @PostMapping
    public ResponseEntity<ClaimDTO> createClaim(@RequestBody ClaimDTO claimDTO) {
        ClaimDTO createdClaim = adminClaimsService.createClaim(claimDTO);
        return ResponseEntity.ok(createdClaim);
    }

    // Update claim
    @PutMapping("/{id}")
    public ResponseEntity<ClaimDTO> updateClaim(@PathVariable Long id, @RequestBody ClaimDTO claimDTO) {
        try {
            ClaimDTO updatedClaim = adminClaimsService.updateClaim(id, claimDTO);
            return ResponseEntity.ok(updatedClaim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete claim
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id) {
        try {
            adminClaimsService.deleteClaim(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get photos for claim
    @GetMapping("/{id}/photos")
    public ResponseEntity<List<ClaimPhoto>> getClaimPhotos(@PathVariable Long id) {
        List<ClaimPhoto> photos = adminClaimsService.getClaimPhotos(id);
        return ResponseEntity.ok(photos);
    }
}
