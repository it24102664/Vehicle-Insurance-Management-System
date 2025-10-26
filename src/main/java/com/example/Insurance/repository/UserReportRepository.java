package com.example.Insurance.repository;

import com.example.Insurance.entity.UserReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    // All queries are CORRECT as they reference the entity property names, not column names
    @Query("SELECT ur FROM UserReport ur WHERE ur.userR.id = :userId ORDER BY ur.createdDate DESC")
    List<UserReport> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserReport ur JOIN ur.userR u WHERE u.nic = :nic ORDER BY ur.createdDate DESC")
    List<UserReport> findByUserNicOrderByCreatedDateDesc(@Param("nic") String nic);

    @Query("SELECT ur FROM UserReport ur WHERE ur.userR.id = :userId AND ur.isFavorite = true ORDER BY ur.createdDate DESC")
    List<UserReport> findByUserIdAndIsFavoriteTrueOrderByCreatedDateDesc(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserReport ur WHERE ur.userR.id = :userId ORDER BY ur.createdDate DESC")
    Page<UserReport> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ur FROM UserReport ur JOIN ur.adminReport ar WHERE ur.userR.id = :userId AND ar.reportYear = :year ORDER BY ur.createdDate DESC")
    List<UserReport> findByUserIdAndReportYear(@Param("userId") Long userId, @Param("year") Integer year);

    @Query("SELECT ur FROM UserReport ur WHERE ur.userR.id = :userId AND ur.createdDate > :sixMonthsAgo ORDER BY ur.createdDate DESC")
    List<UserReport> findRecentReportsByUserId(@Param("userId") Long userId, @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);

    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserReport ur WHERE ur.userR.id = :userId AND ur.adminReport.id = :adminReportId")
    boolean existsByUserIdAndAdminReportId(@Param("userId") Long userId, @Param("adminReportId") Long adminReportId);

    @Query("SELECT ur FROM UserReport ur WHERE ur.userR.id = :userId AND ur.adminReport.id = :adminReportId")
    Optional<UserReport> findByUserIdAndAdminReportId(@Param("userId") Long userId, @Param("adminReportId") Long adminReportId);

    @Query("SELECT COUNT(ur) FROM UserReport ur WHERE ur.userR.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ar.totalAmount), 0.0) FROM UserReport ur JOIN ur.adminReport ar WHERE ur.userR.id = :userId")
    Double getTotalPaymentAmountByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserReport ur WHERE ur.userR.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserReport ur WHERE ur.adminReport.id = :adminReportId")
    void deleteByAdminReportId(@Param("adminReportId") Long adminReportId);

    @Query("SELECT ur FROM UserReport ur JOIN ur.userR u JOIN ur.adminReport ar WHERE u.nic = :nic AND ar.reportYear = :year ORDER BY ur.createdDate DESC")
    List<UserReport> findByUserNicAndReportYear(@Param("nic") String nic, @Param("year") Integer year);
}
