package com.example.Insurance.repository;

import com.example.Insurance.entity.AdminNotification;
import com.example.Insurance.entity.AdminNotification.NotificationStatus;
import com.example.Insurance.entity.AdminNotification.PriorityLevel;
import com.example.Insurance.entity.AdminNotification.TargetAudience;
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
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    // Find all active notifications
    List<AdminNotification> findByIsActiveTrueOrderByCreatedDesc();

    // Find notifications by status
    List<AdminNotification> findByStatusAndIsActiveTrueOrderByCreatedDesc(NotificationStatus status);

    // Find notifications by priority
    List<AdminNotification> findByPriorityAndIsActiveTrueOrderByCreatedDesc(PriorityLevel priority);

    // Find notifications by target audience
    List<AdminNotification> findByTargetAndIsActiveTrueOrderByCreatedDesc(TargetAudience target);

    // Find scheduled notifications that are ready to send
    @Query("SELECT n FROM AdminNotification n WHERE n.status = 'SCHEDULED' AND n.scheduleDate <= :currentTime AND n.isActive = true")
    List<AdminNotification> findScheduledNotificationsReadyToSend(@Param("currentTime") LocalDateTime currentTime);

    // Find expired notifications
    @Query("SELECT n FROM AdminNotification n WHERE n.expiryDate IS NOT NULL AND n.expiryDate <= :currentTime AND n.status != 'EXPIRED' AND n.isActive = true")
    List<AdminNotification> findExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);

    // Find notifications by date range
    @Query("SELECT n FROM AdminNotification n WHERE n.created BETWEEN :startDate AND :endDate AND n.isActive = true ORDER BY n.created DESC")
    List<AdminNotification> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count notifications by status
    @Query("SELECT COUNT(n) FROM AdminNotification n WHERE n.status = :status AND n.isActive = true")
    Long countByStatus(@Param("status") NotificationStatus status);

    // Hard delete notification by ID
    @Transactional
    void deleteById(@Param("id") Long id);

    // Hard delete all notifications
    @Transactional
    void deleteAll();

    // Find notification by ID and active status
    Optional<AdminNotification> findByIdAndIsActiveTrue(Long id);

    // Update notification status
    @Modifying
    @Query("UPDATE AdminNotification n SET n.status = :status, n.sentCount = :sentCount, n.updatedDate = :updateTime WHERE n.id = :id")
    void updateNotificationStatus(@Param("id") Long id, @Param("status") NotificationStatus status,
                                  @Param("sentCount") Integer sentCount, @Param("updateTime") LocalDateTime updateTime);

    // Search notifications by title or message
    @Query("SELECT n FROM AdminNotification n WHERE (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND n.isActive = true ORDER BY n.created DESC")
    List<AdminNotification> searchByKeyword(@Param("keyword") String keyword);
}