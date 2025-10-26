package com.example.Insurance.controller;


import com.example.Insurance.DTO.PolicyApplicationDTO;
import com.example.Insurance.entity.Policy;
import com.example.Insurance.entity.PolicyApplication;
import com.example.Insurance.service.PolicyApplicationService;
import com.example.Insurance.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-policies")
@CrossOrigin(origins = "*")
public class UserPolicyController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<Policy>> getActivePolicies() {
        List<Policy> activePolicies = policyService.getAllActivePolicies();
        return ResponseEntity.ok(activePolicies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Policy> getPolicyById(@PathVariable Long id) {
        Policy policy = policyService.getPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    @PostMapping("/applications")
    public ResponseEntity<PolicyApplication> submitApplication(@RequestBody PolicyApplicationDTO applicationDTO) {
        PolicyApplication application = new PolicyApplication();
        Policy policy = policyService.getPolicyById(applicationDTO.getPolicyId());

        application.setPolicy(policy);
        application.setApplicantName(applicationDTO.getApplicantName());
        application.setAge(applicationDTO.getAge());
        application.setNic(applicationDTO.getNic());
        application.setAddress(applicationDTO.getAddress());
        application.setPhone(applicationDTO.getPhone());
        application.setEmail(applicationDTO.getEmail());
        application.setVehicleDetails(applicationDTO.getVehicleDetails());
        application.setAdditionalNotes(applicationDTO.getAdditionalNotes());

        PolicyApplication savedApplication = applicationService.submitApplication(application);
        return ResponseEntity.ok(savedApplication);
    }

    @GetMapping("/download/{policyId}")
    public ResponseEntity<byte[]> downloadPolicy(@PathVariable Long policyId) {
        Policy policy = policyService.getPolicyById(policyId);

        // Generate PDF content (simplified example)
        String pdfContent = generatePolicyPDF(policy);
        byte[] pdfBytes = pdfContent.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", policy.getName() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private String generatePolicyPDF(Policy policy) {
        return "Policy Document: " + policy.getName() + "\n" +
                "Description: " + policy.getDescription() + "\n" +
                "Benefits: " + String.join(", ", policy.getBenefits());
    }
}
