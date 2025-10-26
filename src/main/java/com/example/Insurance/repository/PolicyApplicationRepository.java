package com.example.Insurance.repository;

import com.example.Insurance.Enums.ApplicationStatus;
import com.example.Insurance.entity.PolicyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApplicationRepository extends JpaRepository<PolicyApplication, Long> {
    List<PolicyApplication> findByStatus(ApplicationStatus status);
    List<PolicyApplication> findByPolicyId(Long policyId);
    List<PolicyApplication> findByApplicantNameAndPolicyId(String name, Long policyId);
    Long countByStatus(ApplicationStatus status);
    List<PolicyApplication> findByOrderByApplicationDateDesc();
}
