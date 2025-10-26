package com.example.Insurance.repository;

import com.example.Insurance.entity.AdminReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminReportRepository extends JpaRepository<AdminReport, Long> {

    // Find reports by customer NIC
    List<AdminReport> findByCustomerNICOrderByCreatedDateDesc(String customerNIC);

    // Find reports by customer name
    List<AdminReport> findByCustomerNameContainingIgnoreCaseOrderByCreatedDateDesc(String customerName);

    // Find reports by report year
    List<AdminReport> findByReportYearOrderByCreatedDateDesc(Integer reportYear);

    // Find reports by customer NIC and year
    Optional<AdminReport> findByCustomerNICAndReportYear(String customerNIC, Integer reportYear);

    // Find reports created by specific admin
    List<AdminReport> findByCreatedByOrderByCreatedDateDesc(String createdBy);

    // Find reports created within date range
    @Query("SELECT ar FROM AdminReport ar WHERE ar.createdDate BETWEEN :startDate AND :endDate ORDER BY ar.createdDate DESC")
    List<AdminReport> findReportsByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Find reports with pagination
    Page<AdminReport> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // Count total reports
    @Query("SELECT COUNT(ar) FROM AdminReport ar")
    Long countAllReports();

    // Count reports by year
    @Query("SELECT COUNT(ar) FROM AdminReport ar WHERE ar.reportYear = :year")
    Long countReportsByYear(@Param("year") Integer year);

    // Find top customers by report count
    @Query("SELECT ar.customerName, COUNT(ar) as reportCount FROM AdminReport ar GROUP BY ar.customerName ORDER BY reportCount DESC")
    List<Object[]> findTopCustomersByReportCount();

    // Search reports by multiple criteria
    @Query("SELECT ar FROM AdminReport ar WHERE " +
            "(:customerName IS NULL OR LOWER(ar.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:customerNIC IS NULL OR ar.customerNIC = :customerNIC) AND " +
            "(:reportYear IS NULL OR ar.reportYear = :reportYear) " +
            "ORDER BY ar.createdDate DESC")
    List<AdminReport> searchReports(@Param("customerName") String customerName,
                                    @Param("customerNIC") String customerNIC,
                                    @Param("reportYear") Integer reportYear);

    // Check if report exists for customer in specific year
    boolean existsByCustomerNICAndReportYear(String customerNIC, Integer reportYear);
}
