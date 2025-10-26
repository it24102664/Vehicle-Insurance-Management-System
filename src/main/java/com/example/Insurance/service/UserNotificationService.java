package com.example.Insurance.service;

import com.example.Insurance.DTO.UserNotificationDTO;
import com.example.Insurance.entity.UserNotification;
import com.example.Insurance.repository.UserNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserNotificationService {

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    // Get notification statistics for a user
    public NotificationStatistics getNotificationStatistics(Long userId) {
        try {
            List<UserNotification> allNotifications = userNotificationRepository.findByUserIdAndActiveOrderByCreatedAtDesc(userId);

            Long total = (long) allNotifications.size();
            Long unread = allNotifications.stream().mapToLong(n -> n.getIsRead() ? 0 : 1).sum();
            Long urgent = allNotifications.stream().mapToLong(n -> n.getPriority() == UserNotification.PriorityLevel.URGENT ? 1 : 0).sum();

            LocalDate today = LocalDate.now();
            Long todayCount = allNotifications.stream()
                    .mapToLong(n -> {
                        if (n.getCreatedAt() != null && n.getCreatedAt().toLocalDate().equals(today)) {
                            return 1;
                        }
                        return 0;
                    }).sum();

            return new NotificationStatistics(total, unread, urgent, todayCount);

        } catch (Exception e) {
            System.err.println("Error getting notification statistics: " + e.getMessage());
            e.printStackTrace();
            return new NotificationStatistics(0L, 0L, 0L, 0L);
        }
    }

    // Get all active notifications for a user
    public List<UserNotificationDTO> getAllNotificationsByUserId(Long userId) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndActiveOrderByCreatedAtDesc(userId);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting all notifications for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get unread notifications for a user
    public List<UserNotificationDTO> getUnreadNotificationsByUserId(Long userId) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndUnreadOrderByCreatedAtDesc(userId);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting unread notifications for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get read notifications for a user
    public List<UserNotificationDTO> getReadNotificationsByUserId(Long userId) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting read notifications for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get notifications by priority for a user
    public List<UserNotificationDTO> getNotificationsByUserIdAndPriority(Long userId, UserNotification.PriorityLevel priority) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(userId, priority);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting notifications by priority for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get notifications by type for a user
    public List<UserNotificationDTO> getNotificationsByUserIdAndType(Long userId, UserNotification.NotificationType type) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting notifications by type for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Search notifications for a user
    public List<UserNotificationDTO> searchNotificationsByUserId(Long userId, String keyword) {
        try {
            List<UserNotification> notifications = userNotificationRepository.searchByUserIdAndKeyword(userId, keyword);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching notifications for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get notification by ID for a user
    public Optional<UserNotificationDTO> getNotificationByIdAndUserId(Long id, Long userId) {
        try {
            Optional<UserNotification> notification = userNotificationRepository.findByIdAndUserIdAndActive(id, userId);
            return notification.map(this::convertToDTO);
        } catch (Exception e) {
            System.err.println("Error getting notification by ID for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Create notification for user
    public UserNotificationDTO createNotificationForUser(UserNotificationDTO notificationDTO) {
        try {
            UserNotification notification = convertToEntity(notificationDTO);
            UserNotification savedNotification = userNotificationRepository.save(notification);
            return convertToDTO(savedNotification);
        } catch (Exception e) {
            System.err.println("Error creating notification for user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Mark notification as read
    public boolean markAsRead(Long id, Long userId) {
        try {
            userNotificationRepository.markAsRead(id, userId, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Mark notification as unread
    public boolean markAsUnread(Long id, Long userId) {
        try {
            userNotificationRepository.markAsUnread(id, userId);
            return true;
        } catch (Exception e) {
            System.err.println("Error marking notification as unread: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Mark all notifications as read for a user
    public boolean markAllAsRead(Long userId) {
        try {
            userNotificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Hard delete notification
    public boolean deleteNotification(Long id, Long userId) {
        try {
            if (userNotificationRepository.findByIdAndUserIdAndActive(id, userId).isPresent()) {
                userNotificationRepository.deleteByIdAndUserId(id, userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Hard delete all notifications for a user
    public boolean deleteAllNotifications(Long userId) {
        try {
            userNotificationRepository.deleteAllByUserId(userId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting all notifications: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Archive notification
    public boolean archiveNotification(Long id, Long userId) {
        try {
            userNotificationRepository.archiveByIdAndUserId(id, userId, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            System.err.println("Error archiving notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get archived notifications for a user
    public List<UserNotificationDTO> getArchivedNotificationsByUserId(Long userId) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndArchivedOrderByArchivedAtDesc(userId);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting archived notifications: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get notifications by date range
    public List<UserNotificationDTO> getNotificationsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<UserNotification> notifications = userNotificationRepository.findByUserIdAndDateRange(userId, startDate, endDate);
            return notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting notifications by date range: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Get unread count for a user
    public Long getUnreadCount(Long userId) {
        try {
            Long count = userNotificationRepository.countUnreadByUserId(userId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    // Send notification to specific users
    public void sendNotificationToUsers(List<Long> userIds, Long adminNotificationId, String title, String message,
                                        UserNotification.NotificationType type, UserNotification.PriorityLevel priority,
                                        String sentBy, LocalDateTime expiryDate) {
        try {
            for (Long userId : userIds) {
                Optional<UserNotification> existingNotification =
                        userNotificationRepository.findByAdminNotificationIdAndUserId(adminNotificationId, userId);

                if (existingNotification.isEmpty()) {
                    UserNotification notification = new UserNotification();
                    notification.setUserId(userId);
                    notification.setAdminNotificationId(adminNotificationId);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setType(type);
                    notification.setPriority(priority);
                    notification.setSentBy(sentBy);
                    notification.setExpiryDate(expiryDate);

                    userNotificationRepository.save(notification);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending notification to users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convert Entity to DTO
    private UserNotificationDTO convertToDTO(UserNotification notification) {
        UserNotificationDTO dto = new UserNotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setAdminNotificationId(notification.getAdminNotificationId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setPriority(notification.getPriority());
        dto.setIsRead(notification.getIsRead());
        dto.setSentBy(notification.getSentBy());
        dto.setReadAt(notification.getReadAt());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReceivedAt(notification.getReceivedAt());
        dto.setExpiryDate(notification.getExpiryDate());
        dto.setIsArchived(notification.getIsArchived());
        dto.setIsDeleted(notification.getIsDeleted());
        return dto;
    }

    // Convert DTO to Entity
    private UserNotification convertToEntity(UserNotificationDTO dto) {
        UserNotification notification = new UserNotification();
        if (dto.getId() != null) {
            notification.setId(dto.getId());
        }
        notification.setUserId(dto.getUserId());
        notification.setAdminNotificationId(dto.getAdminNotificationId());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setPriority(dto.getPriority());
        notification.setIsRead(dto.getIsRead());
        notification.setSentBy(dto.getSentBy());
        notification.setExpiryDate(dto.getExpiryDate());
        return notification;
    }

    // Inner class for statistics
    public static class NotificationStatistics {
        private Long total;
        private Long unread;
        private Long urgent;
        private Long today;

        public NotificationStatistics(Long total, Long unread, Long urgent, Long today) {
            this.total = total;
            this.unread = unread;
            this.urgent = urgent;
            this.today = today;
        }

        // Getters
        public Long getTotal() { return total; }
        public Long getUnread() { return unread; }
        public Long getUrgent() { return urgent; }
        public Long getToday() { return today; }

        // Setters
        public void setTotal(Long total) { this.total = total; }
        public void setUnread(Long unread) { this.unread = unread; }
        public void setUrgent(Long urgent) { this.urgent = urgent; }
        public void setToday(Long today) { this.today = today; }
    }
}