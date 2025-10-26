package com.example.Insurance.service;

import com.example.Insurance.DTO.ApplicationStatsDTO;
import com.example.Insurance.Enums.ApplicationStatus;
import com.example.Insurance.entity.PolicyApplication;
import com.example.Insurance.repository.PolicyApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PolicyApplicationService {

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private PolicyService policyService;

    public PolicyApplication submitApplication(PolicyApplication application) {
        // Check for duplicates
        List<PolicyApplication> existing = applicationRepository
                .findByApplicantNameAndPolicyId(application.getApplicantName(),
                        application.getPolicy().getId());

        for (PolicyApplication existingApp : existing) {
            if (existingApp.getStatus() == ApplicationStatus.PENDING ||
                    existingApp.getStatus() == ApplicationStatus.APPROVED) {
                application.setStatus(ApplicationStatus.DUPLICATE);
                break;
            }
        }

        if (application.getStatus() == null) {
            application.setStatus(ApplicationStatus.PENDING);
        }

        application.setApplicationDate(LocalDate.now());
        return applicationRepository.save(application);
    }

    public List<PolicyApplication> getAllApplications() {
        return applicationRepository.findByOrderByApplicationDateDesc();
    }

    public List<PolicyApplication> getPendingApplications() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    public PolicyApplication getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
    }

    public PolicyApplication approveApplication(Long id) {
        PolicyApplication application = getApplicationById(id);
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedDate(LocalDate.now());
        return applicationRepository.save(application);
    }

    public PolicyApplication rejectApplication(Long id) {
        PolicyApplication application = getApplicationById(id);
        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedDate(LocalDate.now());
        return applicationRepository.save(application);
    }

    public void deleteApplication(Long id) {
        PolicyApplication application = getApplicationById(id);
        applicationRepository.delete(application);
    }

    public ApplicationStatsDTO getApplicationStats() {
        long pending = applicationRepository.countByStatus(ApplicationStatus.PENDING);
        long approved = applicationRepository.countByStatus(ApplicationStatus.APPROVED);
        long rejected = applicationRepository.countByStatus(ApplicationStatus.REJECTED);
        long duplicates = applicationRepository.countByStatus(ApplicationStatus.DUPLICATE);

        return new ApplicationStatsDTO(pending, approved, rejected, duplicates);
    }
}
