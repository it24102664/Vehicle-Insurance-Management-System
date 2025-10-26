package com.example.Insurance.repository;

import com.example.Insurance.entity.Policy;
import com.example.Insurance.Enums.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // Now these methods will work with the new fields
    List<Policy> findByApplicantIdAndStatus(Long applicantId, PolicyStatus status);

    List<Policy> findByApplicantId(Long applicantId);

    List<Policy> findByStatus(PolicyStatus status);

    // Query to find policies by policy application ID
    @Query("SELECT p FROM Policy p WHERE p.policyApplication.id = :applicationId AND p.status = :status")
    List<Policy> findByPolicyApplicationIdAndStatus(@Param("applicationId") Long applicationId, @Param("status") PolicyStatus status);

    // Find policies ready for payments
    @Query("SELECT p FROM Policy p WHERE p.status = 'APPROVED' AND p.applicantId IS NOT NULL")
    List<Policy> findPoliciesReadyForPayments();
}
