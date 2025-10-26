package com.example.Insurance.repository;

import com.example.Insurance.entity.Payment;
import com.example.Insurance.Enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payments by user ID - works with your existing Payment entity
    List<Payment> findByUserIdOrderBySubmittedDateDesc(Long userId);

    // Find payments by status - for admin functionality
    List<Payment> findByStatusOrderBySubmittedDateDesc(PaymentStatus status);

    // Find payments by policy ID
    List<Payment> findByPolicyIdOrderBySubmittedDateDesc(Long policyId);

    // Check if payment exists for policy and month with specific status
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.policy.id = :policyId AND p.paymentMonth = :month AND p.status = :status")
    boolean existsByPolicyIdAndPaymentMonthAndStatus(@Param("policyId") Long policyId, @Param("month") String month, @Param("status") PaymentStatus status);

    // Find payments by payment method
    List<Payment> findByPaymentMethod(String paymentMethod);
}
