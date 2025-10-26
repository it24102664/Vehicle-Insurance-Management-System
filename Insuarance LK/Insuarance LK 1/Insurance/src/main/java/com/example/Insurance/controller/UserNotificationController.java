package com.example.Insurance.controller;

import com.example.Insurance.DTO.UserNotificationDTO;
import com.example.Insurance.entity.UserNotification;
import com.example.Insurance.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserNotificationController {

    @Autowired
    private UserNotificationService userNotificationService;

    // Get all notifications for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> getAllNotifications(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.getAllNotificationsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notifications: " + e.getMessage(), null));
        }
    }

    // Get unread notifications for a user
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> getUnreadNotifications(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.getUnreadNotificationsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving unread notifications: " + e.getMessage(), null));
        }
    }

    // Get read notifications for a user
    @GetMapping("/user/{userId}/read")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> getReadNotifications(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.getReadNotificationsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Read notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving read notifications: " + e.getMessage(), null));
        }
    }

    // Get notifications by priority
    @GetMapping("/user/{userId}/priority/{priority}")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> getNotificationsByPriority(
            @PathVariable Long userId, @PathVariable UserNotification.PriorityLevel priority) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.getNotificationsByUserIdAndPriority(userId, priority);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notifications: " + e.getMessage(), null));
        }
    }

    // Get notifications by type
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> getNotificationsByType(
            @PathVariable Long userId, @PathVariable UserNotification.NotificationType type) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.getNotificationsByUserIdAndType(userId, type);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notifications: " + e.getMessage(), null));
        }
    }

    // Search notifications
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> searchNotifications(
            @PathVariable Long userId, @RequestParam String keyword) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Keyword is required", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.searchNotificationsByUserId(userId, keyword.trim());
            return ResponseEntity.ok(new ApiResponse<>(true, "Search completed successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error searching notifications: " + e.getMessage(), null));
        }
    }

    // Get notification by ID
    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<ApiResponse<UserNotificationDTO>> getNotificationById(
            @PathVariable Long id, @PathVariable Long userId) {
        try {
            if (id == null || id <= 0 || userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification or user ID", null));
            }

            Optional<UserNotificationDTO> notification = userNotificationService.getNotificationByIdAndUserId(id, userId);
            if (notification.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification found", notification.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notification: " + e.getMessage(), null));
        }
    }

    // Mark notification as read
    @PutMapping("/{id}/user/{userId}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id, @PathVariable Long userId) {
        try {
            if (id == null || id <= 0 || userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification or user ID", null));
            }

            boolean success = userNotificationService.markAsRead(id, userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error marking notification as read: " + e.getMessage(), null));
        }
    }

    // Mark notification as unread
    @PutMapping("/{id}/user/{userId}/unread")
    public ResponseEntity<ApiResponse<String>> markAsUnread(@PathVariable Long id, @PathVariable Long userId) {
        try {
            if (id == null || id <= 0 || userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification or user ID", null));
            }

            boolean success = userNotificationService.markAsUnread(id, userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as unread", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error marking notification as unread: " + e.getMessage(), null));
        }
    }

    // Mark all notifications as read for a user
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            boolean success = userNotificationService.markAllAsRead(userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(false, "Failed to mark all notifications as read", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error marking all notifications as read: " + e.getMessage(), null));
        }
    }

    // Delete notification
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id, @PathVariable Long userId) {
        try {
            if (id == null || id <= 0 || userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification or user ID", null));
            }

            boolean success = userNotificationService.deleteNotification(id, userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting notification: " + e.getMessage(), null));
        }
    }

    // Delete all notifications for a user
    @DeleteMapping("/user/{userId}/clear-all")
    public ResponseEntity<ApiResponse<String>> deleteAllNotifications(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            boolean success = userNotificationService.deleteAllNotifications(userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "All notifications deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(false, "Failed to delete all notifications", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting all notifications: " + e.getMessage(), null));
        }
    }

    // Archive notification
    @PutMapping("/{id}/user/{userId}/archive")
    public ResponseEntity<ApiResponse<String>> archiveNotification(@PathVariable Long id, @PathVariable Long userId) {
        try {
            if (id == null || id <= 0 || userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification or user ID", null));
            }

            boolean success = userNotificationService.archiveNotification(id, userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification archived successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error archiving notification: " + e.getMessage(), null));
        }
    }

    // Get notification statistics
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<ApiResponse<UserNotificationService.NotificationStatistics>> getStatistics(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            UserNotificationService.NotificationStatistics stats = userNotificationService.getNotificationStatistics(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Statistics retrieved successfully", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving statistics: " + e.getMessage(), null));
        }
    }

    // Get unread count
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            Long count = userNotificationService.getUnreadCount(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Unread count retrieved successfully", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving unread count: " + e.getMessage(), null));
        }
    }

    // Get notifications by date range
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<ApiResponse<List<UserNotificationDTO>>> getNotificationsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID", null));
            }

            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Start date and end date are required", null));
            }

            List<UserNotificationDTO> notifications = userNotificationService.getNotificationsByDateRange(userId, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notifications: " + e.getMessage(), null));
        }
    }

    // API Response wrapper class
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse() {}

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}
