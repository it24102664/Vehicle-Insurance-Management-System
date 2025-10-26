
package com.example.Insurance.repository;

import com.example.Insurance.entity.ClaimPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimPhotoRepository extends JpaRepository<ClaimPhoto, Long> {

    List<ClaimPhoto> findByClaimId(Long claimId);

    void deleteByClaimId(Long claimId);
}
