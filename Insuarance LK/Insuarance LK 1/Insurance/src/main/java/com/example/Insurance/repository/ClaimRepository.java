
package com.example.Insurance.repository;

import com.example.Insurance.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Existing methods
    List<Claim> findByStatus(Claim.ClaimStatus status);
    List<Claim> findByIsDuplicate(Boolean isDuplicate);
    Optional<Claim> findByClaimNumber(String claimNumber);
    Long countByStatus(Claim.ClaimStatus status);

    @Query("SELECT c FROM Claim c ORDER BY c.submittedDate DESC")
    List<Claim> findAllOrderBySubmittedDateDesc();

    @Query("SELECT c FROM Claim c WHERE c.nic = ?1 AND c.vehicleNumber = ?2")
    List<Claim> findPotentialDuplicates(String nic, String vehicleNumber);

    // NEW METHODS TO ADD - for UserClaim integration
    @Query("SELECT c FROM Claim c WHERE c.user.id = ?1 ORDER BY c.submittedDate DESC")
    List<Claim> findByUserIdOrderBySubmittedDateDesc(Long userId);

    @Query("SELECT c FROM Claim c WHERE c.user.id = ?1 AND c.status = ?2 ORDER BY c.submittedDate DESC")
    List<Claim> findByUserIdAndStatusOrderBySubmittedDateDesc(Long userId, Claim.ClaimStatus status);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.user.id = ?1 AND c.status = ?2")
    long countByUserIdAndStatus(Long userId, Claim.ClaimStatus status);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.user.id = ?1")
    long countByUserId(Long userId);

}
