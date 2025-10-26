package com.example.Insurance.repository;

import com.example.Insurance.entity.UserR;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface userRRepository extends JpaRepository<UserR, Long> {

    // Find user by NIC
    Optional<UserR> findByNic(String nic);

    // Find user by phone
    Optional<UserR> findByPhone(String phone);

    // Find user by email
    Optional<UserR> findByEmail(String email);

    // Find active users
    Page<UserR> findByIsActiveTrue(Pageable pageable);

    // Search users by name (case insensitive)
    Page<UserR> findByFullNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    // Check if user exists by NIC
    boolean existsByNic(String nic);

    // Check if user exists by email
    boolean existsByEmail(String email);

    // Get user statistics
    @Query("SELECT COUNT(u) FROM UserR u WHERE u.isActive = true")
    Long countActiveUsers();

    // Find users with reports count
    @Query("SELECT u FROM UserR u LEFT JOIN FETCH u.userReports ur WHERE u.nic = :nic")
    Optional<UserR> findByNicWithReports(@Param("nic") String nic);

    // Search users by name or NIC - REQUIRED METHOD
    @Query("SELECT u FROM UserR u WHERE u.isActive = true AND " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.nic) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<UserR> searchByNameOrNic(@Param("query") String query);
}
