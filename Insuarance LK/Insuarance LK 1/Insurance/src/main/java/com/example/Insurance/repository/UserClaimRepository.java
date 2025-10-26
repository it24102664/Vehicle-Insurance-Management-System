package com.example.Insurance.repository;

import com.example.Insurance.entity.UserC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserClaimRepository extends JpaRepository<UserC, Long> {

    Optional<UserC> findByEmail(String email);
    Optional<UserC> findByNic(String nic);
    boolean existsByEmail(String email);
    boolean existsByNic(String nic);
}
