// src/main/java/com/example/Insurance/repository/UserClaimNotificationRepository.java
package com.example.Insurance.repository;

import com.example.Insurance.entity.UserClaimNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserClaimNotificationRepository extends JpaRepository<UserClaimNotification, Long> {

    List<UserClaimNotification> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("SELECT COUNT(n) FROM UserClaimNotification n WHERE n.user.id = ?1 AND n.isRead = false")
    long countUnreadByUserId(Long userId);

    List<UserClaimNotification> findByUserIdAndIsReadFalseOrderByCreatedDateDesc(Long userId);
}
