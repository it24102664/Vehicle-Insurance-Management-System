package com.example.Insurance.service;

import com.example.Insurance.Enums.PolicyStatus;
import com.example.Insurance.entity.Policy;
import com.example.Insurance.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PolicyService {

    private static final Logger logger = LoggerFactory.getLogger(PolicyService.class);

    @Autowired
    private PolicyRepository policyRepository;

    public List<Policy> getAllActivePolicies() {
        logger.debug("Fetching all active policies");
        List<Policy> activePolicies = policyRepository.findByStatus(PolicyStatus.ACTIVE);
        logger.info("Found {} active policies", activePolicies.size());
        return activePolicies;
    }

    public List<Policy> getAllPolicies() {
        logger.debug("Fetching all policies for admin");
        List<Policy> allPolicies = policyRepository.findAll();
        logger.info("Found {} total policies", allPolicies.size());
        return allPolicies;
    }

    public Policy getPolicyById(Long id) {
        logger.debug("Fetching policy with id: {}", id);

        Optional<Policy> policyOpt = policyRepository.findById(id);
        if (policyOpt.isEmpty()) {
            logger.error("Policy not found with id: {}", id);
            throw new RuntimeException("Policy not found with id: " + id);
        }

        Policy policy = policyOpt.get();
        logger.info("Found policy: {}", policy.getName());
        return policy;
    }

    public Policy createPolicy(Policy policy) {
        logger.debug("Creating new policy: {}", policy.getName());

        validatePolicy(policy);

        Policy savedPolicy = policyRepository.save(policy);
        logger.info("Successfully created policy with id: {} and name: {}",
                savedPolicy.getId(), savedPolicy.getName());

        return savedPolicy;
    }

    public Policy updatePolicy(Long id, Policy updatedPolicy) {
        logger.debug("Updating policy with id: {}", id);

        Policy existingPolicy = getPolicyById(id);
        validatePolicy(updatedPolicy);

        existingPolicy.setName(updatedPolicy.getName());

        existingPolicy.setDescription(updatedPolicy.getDescription());
        existingPolicy.setBenefits(updatedPolicy.getBenefits());
        existingPolicy.setPremiumAmount(updatedPolicy.getPremiumAmount());
        existingPolicy.setCoverageAmount(updatedPolicy.getCoverageAmount());
        existingPolicy.setVehicleType(updatedPolicy.getVehicleType());
        existingPolicy.setStatus(updatedPolicy.getStatus());

        Policy savedPolicy = policyRepository.save(existingPolicy);
        logger.info("Successfully updated policy: {}", savedPolicy.getName());

        return savedPolicy;
    }

    public void deletePolicy(Long id) {
        logger.debug("Deleting policy with id: {}", id);

        Policy policy = getPolicyById(id);
        policyRepository.delete(policy);
        logger.info("Successfully deleted policy: {} (id: {})", policy.getName(), id);
    }

    public List<Policy> getPoliciesByStatus(PolicyStatus status) {
        logger.debug("Fetching policies with status: {}", status);
        List<Policy> policies = policyRepository.findByStatus(status);
        logger.info("Found {} policies with status: {}", policies.size(), status);
        return policies;
    }

    public boolean existsById(Long id) {
        boolean exists = policyRepository.existsById(id);
        logger.debug("Policy with id {} exists: {}", id, exists);
        return exists;
    }

    // REMOVED OR COMMENTED OUT - causing the compilation error
    /*
    public long countPoliciesByStatus(PolicyStatus status) {
        long count = policyRepository.countByStatus(status);
        logger.debug("Count of {} policies: {}", status, count);
        return count;
    }
    */

    private void validatePolicy(Policy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }

        if (policy.getName() == null || policy.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Policy name is required");
        }

        if (policy.getDescription() == null || policy.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Policy description is required");
        }

        if (policy.getPremiumAmount() == null || policy.getPremiumAmount() <= 0) {
            throw new IllegalArgumentException("Premium amount must be greater than 0");
        }

        if (policy.getCoverageAmount() == null || policy.getCoverageAmount() <= 0) {
            throw new IllegalArgumentException("Coverage amount must be greater than 0");
        }

        if (policy.getVehicleType() == null || policy.getVehicleType().trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type is required");
        }

        if (policy.getStatus() == null) {
            policy.setStatus(PolicyStatus.ACTIVE);
        }

        logger.debug("Policy validation passed for: {}", policy.getName());
    }

    public List<Policy> getPoliciesByUserIdAndStatus(Long userId, PolicyStatus applicationStatus) {
        return List.of();
    }

}
