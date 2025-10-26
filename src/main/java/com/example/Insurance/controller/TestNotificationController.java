package com.example.Insurance.controller;

import com.example.Insurance.entity.UserNotification;
import com.example.Insurance.repository.UserNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestNotificationController {

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @PostMapping("/create-sample-notifications/{userId}")
    public ResponseEntity<String> createSampleNotifications(@PathVariable Long userId) {
        try {
            // Clear existing notifications for this user first (hard delete)
            userNotificationRepository.deleteAllByUserId(userId);

            List<UserNotification> sampleNotifications = Arrays.asList(
                    createSampleNotification(userId, 1L, "Welcome to MOTORCARE LK!",
                            "Thank you for joining our insurance platform. Explore your dashboard to get started.",
                            UserNotification.NotificationType.GENERAL, UserNotification.PriorityLevel.MEDIUM),

                    createSampleNotification(userId, 2L, "System Maintenance Alert",
                            "Our system will undergo maintenance on Sunday from 2:00 AM to 4:00 AM.",
                            UserNotification.NotificationType.MAINTENANCE, UserNotification.PriorityLevel.HIGH),

                    createSampleNotification(userId, 3L, "Premium Payment Due",
                            "Your premium payment is due in 3 days. Please complete your payment.",
                            UserNotification.NotificationType.GENERAL, UserNotification.PriorityLevel.URGENT),

                    createSampleNotification(userId, 4L, "New Feature Available",
                            "Check out our new claim tracking feature in your dashboard.",
                            UserNotification.NotificationType.UPDATE, UserNotification.PriorityLevel.LOW),

                    createSampleNotification(userId, 5L, "Security Alert",
                            "Login detected from a new device. If this wasn't you, please contact support.",
                            UserNotification.NotificationType.SECURITY, UserNotification.PriorityLevel.URGENT)
            );

            userNotificationRepository.saveAll(sampleNotifications);

            return ResponseEntity.ok("✅ Sample notifications created successfully for user " + userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Error creating sample notifications: " + e.getMessage());
        }
    }

    private UserNotification createSampleNotification(Long userId, Long adminNotificationId, String title, String message,
                                                      UserNotification.NotificationType type, UserNotification.PriorityLevel priority) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setAdminNotificationId(adminNotificationId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setSentBy("System Admin");
        notification.setCreatedAt(LocalDateTime.now().minusHours((long) (Math.random() * 24)));

        // Make some notifications unread
        if (Math.random() > 0.5) {
            notification.setIsRead(false);
        } else {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now().minusHours((long) (Math.random() * 12)));
        }

        return notification;
    }
}