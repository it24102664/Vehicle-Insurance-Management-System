package com.example.Insurance.controller;


import com.example.Insurance.DTO.ApplicationStatsDTO;
import com.example.Insurance.entity.PolicyApplication;
import com.example.Insurance.service.PolicyApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-applications")
@CrossOrigin(origins = "*")
public class AdminApplicationController {

    @Autowired
    private PolicyApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<PolicyApplication>> getAllApplications() {
        List<PolicyApplication> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PolicyApplication>> getPendingApplications() {
        List<PolicyApplication> pendingApplications = applicationService.getPendingApplications();
        return ResponseEntity.ok(pendingApplications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyApplication> getApplicationById(@PathVariable Long id) {
        PolicyApplication application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PolicyApplication> approveApplication(@PathVariable Long id) {
        PolicyApplication approvedApplication = applicationService.approveApplication(id);
        return ResponseEntity.ok(approvedApplication);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<PolicyApplication> rejectApplication(@PathVariable Long id) {
        PolicyApplication rejectedApplication = applicationService.rejectApplication(id);
        return ResponseEntity.ok(rejectedApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<ApplicationStatsDTO> getApplicationStats() {
        ApplicationStatsDTO stats = applicationService.getApplicationStats();
        return ResponseEntity.ok(stats);
    }
}
