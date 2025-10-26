package com.example.Insurance.observer;

import com.example.Insurance.entity.AdminNotification;
import com.example.Insurance.entity.UserNotification;
import com.example.Insurance.repository.UserNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DatabaseNotificationObserver implements Observer {

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Override
    public void update(AdminNotification adminNotification) {
        System.out.println("📝 DatabaseObserver: Saving notifications to database...");

        try {
            List<Long> userIds = getUserIdsByTarget(adminNotification.getTarget());

            for (Long userId : userIds) {
                Optional<UserNotification> existingNotification =
                        userNotificationRepository.findByAdminNotificationIdAndUserId(
                                adminNotification.getId(), userId);

                if (existingNotification.isEmpty()) {
                    UserNotification userNotification = new UserNotification();
                    userNotification.setUserId(userId);
                    userNotification.setAdminNotificationId(adminNotification.getId());
                    userNotification.setTitle(adminNotification.getTitle());
                    userNotification.setMessage(adminNotification.getMessage());
                    userNotification.setType(convertToUserNotificationType(adminNotification.getType()));
                    userNotification.setPriority(convertToUserNotificationPriority(adminNotification.getPriority()));
                    userNotification.setSentBy(adminNotification.getCreatedBy());
                    userNotification.setExpiryDate(adminNotification.getExpiryDate());

                    userNotificationRepository.save(userNotification);
                }
            }

            System.out.println("✅ DatabaseObserver: Saved notifications for " + userIds.size() + " users");

        } catch (Exception e) {
            System.err.println("❌ DatabaseObserver error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Long> getUserIdsByTarget(AdminNotification.TargetAudience target) {
        switch (target) {
            case ALL: return Arrays.asList(1L, 2L, 3L, 4L, 5L);
            case ACTIVE: return Arrays.asList(1L, 2L, 3L);
            case INACTIVE: return Arrays.asList(4L, 5L);
            case PREMIUM: return Arrays.asList(1L, 3L);
            case NEW: return Arrays.asList(5L);
            default: return Arrays.asList(1L);
        }
    }

    private UserNotification.NotificationType convertToUserNotificationType(
            AdminNotification.NotificationType adminType) {
        switch (adminType) {
            case GENERAL: return UserNotification.NotificationType.GENERAL;
            case UPDATE: return UserNotification.NotificationType.UPDATE;
            case PROMOTION: return UserNotification.NotificationType.PROMOTION;
            case MAINTENANCE: return UserNotification.NotificationType.MAINTENANCE;
            case SECURITY: return UserNotification.NotificationType.SECURITY;
            default: return UserNotification.NotificationType.GENERAL;
        }
    }

    private UserNotification.PriorityLevel convertToUserNotificationPriority(
            AdminNotification.PriorityLevel adminPriority) {
        switch (adminPriority) {
            case LOW: return UserNotification.PriorityLevel.LOW;
            case MEDIUM: return UserNotification.PriorityLevel.MEDIUM;
            case HIGH: return UserNotification.PriorityLevel.HIGH;
            case URGENT: return UserNotification.PriorityLevel.URGENT;
            default: return UserNotification.PriorityLevel.MEDIUM;
        }
    }
}
