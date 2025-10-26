package com.example.Insurance.repository;

import com.example.Insurance.entity.AdminReportPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminReportPaymentRepository extends JpaRepository<AdminReportPayment, Long> {

    // Find payments by admin report ID
    List<AdminReportPayment> findByAdminReportIdOrderByPaymentMonth(Long adminReportId);

    // Find payments by specific month across all reports
    List<AdminReportPayment> findByPaymentMonth(String paymentMonth);

    // Delete all payments for a specific report
    void deleteByAdminReportId(Long adminReportId);

    // Get total amount by report ID
    @Query("SELECT SUM(arp.paymentAmount) FROM AdminReportPayment arp WHERE arp.adminReport.id = :reportId")
    Double getTotalAmountByReportId(@Param("reportId") Long reportId);

    // Get payment count by report ID
    @Query("SELECT COUNT(arp) FROM AdminReportPayment arp WHERE arp.adminReport.id = :reportId")
    Long getPaymentCountByReportId(@Param("reportId") Long reportId);

    // Get average payment amount by report ID
    @Query("SELECT AVG(arp.paymentAmount) FROM AdminReportPayment arp WHERE arp.adminReport.id = :reportId")
    Double getAverageAmountByReportId(@Param("reportId") Long reportId);
}
