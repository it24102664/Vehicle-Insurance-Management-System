
package com.example.Insurance.repository;

import com.example.Insurance.entity.ClaimForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClaimFormRepository extends JpaRepository<ClaimForm, Long> {

    Optional<ClaimForm> findByFormName(String formName);

    boolean existsByFormName(String formName);
}
