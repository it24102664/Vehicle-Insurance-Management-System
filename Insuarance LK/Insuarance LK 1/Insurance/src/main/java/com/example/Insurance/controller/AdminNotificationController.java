package com.example.Insurance.controller;

import com.example.Insurance.DTO.AdminNotificationDTO;
import com.example.Insurance.entity.AdminNotification;
import com.example.Insurance.service.AdminNotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminNotificationController {

    @Autowired
    private AdminNotificationService adminNotificationService;

    // Create new notification (draft)
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdminNotificationDTO>> createNotification(
            @RequestBody AdminNotificationDTO notificationDTO) {
        try {
            // Validate required fields manually
            if (notificationDTO.getTitle() == null || notificationDTO.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Title is required", null));
            }

            if (notificationDTO.getMessage() == null || notificationDTO.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Message is required", null));
            }

            AdminNotificationDTO createdNotification = adminNotificationService.createNotification(notificationDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification created successfully", createdNotification));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating notification: " + e.getMessage(), null));
        }
    }

    // Send notification immediately
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<AdminNotificationDTO>> sendNotification(
            @RequestBody AdminNotificationDTO notificationDTO) {
        try {
            // Validate required fields
            if (notificationDTO.getTitle() == null || notificationDTO.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Title is required", null));
            }

            if (notificationDTO.getMessage() == null || notificationDTO.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Message is required", null));
            }

            AdminNotificationDTO sentNotification = adminNotificationService.sendNewNotification(notificationDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification sent successfully", sentNotification));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error sending notification: " + e.getMessage(), null));
        }
    }

    // Schedule notification
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<AdminNotificationDTO>> scheduleNotification(
            @RequestBody AdminNotificationDTO notificationDTO) {
        try {
            // Validate required fields
            if (notificationDTO.getTitle() == null || notificationDTO.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Title is required", null));
            }

            if (notificationDTO.getMessage() == null || notificationDTO.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Message is required", null));
            }

            if (notificationDTO.getScheduleDate() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Schedule date is required for scheduling", null));
            }

            AdminNotificationDTO scheduledNotification = adminNotificationService.scheduleNotification(notificationDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification scheduled successfully", scheduledNotification));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error scheduling notification: " + e.getMessage(), null));
        }
    }

    // Get all notifications
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AdminNotificationDTO>>> getAllNotifications() {
        try {
            List<AdminNotificationDTO> notifications = adminNotificationService.getAllNotifications();
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notifications: " + e.getMessage(), null));
        }
    }

    // Get notification by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminNotificationDTO>> getNotificationById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification ID", null));
            }

            Optional<AdminNotificationDTO> notification = adminNotificationService.getNotificationById(id);
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

    // Update notification - FIXED VERSION
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminNotificationDTO>> updateNotification(
            @PathVariable Long id, @RequestBody AdminNotificationDTO notificationDTO) {
        try {
            // Validate path parameter
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification ID", null));
            }

            // Validate required fields
            if (notificationDTO.getTitle() == null || notificationDTO.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Title is required", null));
            }

            if (notificationDTO.getMessage() == null || notificationDTO.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Message is required", null));
            }

            // Set default values if not provided
            if (notificationDTO.getType() == null) {
                notificationDTO.setType(AdminNotification.NotificationType.GENERAL);
            }

            if (notificationDTO.getPriority() == null) {
                notificationDTO.setPriority(AdminNotification.PriorityLevel.MEDIUM);
            }

            if (notificationDTO.getTarget() == null) {
                notificationDTO.getTarget(AdminNotification.TargetAudience.ALL);
            }

            System.out.println("Updating notification ID: " + id);
            System.out.println("Update data: " + notificationDTO.toString());

            Optional<AdminNotificationDTO> updatedNotification = adminNotificationService.updateNotification(id, notificationDTO);
            if (updatedNotification.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification updated successfully", updatedNotification.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating notification: " + e.getMessage(), null));
        }
    }

    // Delete notification
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification ID", null));
            }

            boolean deleted = adminNotificationService.deleteNotification(id);
            if (deleted) {
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

    // Send existing notification (from history)
    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<AdminNotificationDTO>> sendExistingNotification(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid notification ID", null));
            }

            Optional<AdminNotificationDTO> sentNotification = adminNotificationService.sendNotification(id);
            if (sentNotification.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification sent successfully", sentNotification.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Notification not found", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error sending notification: " + e.getMessage(), null));
        }
    }

    // Get notifications by status
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AdminNotificationDTO>>> getNotificationsByStatus(
            @PathVariable AdminNotification.NotificationStatus status) {
        try {
            List<AdminNotificationDTO> notifications = adminNotificationService.getNotificationsByStatus(status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving notifications: " + e.getMessage(), null));
        }
    }

    // Search notifications
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AdminNotificationDTO>>> searchNotifications(
            @RequestParam String keyword) {
        try {
            List<AdminNotificationDTO> notifications = adminNotificationService.searchNotifications(keyword);
            return ResponseEntity.ok(new ApiResponse<>(true, "Search completed successfully", notifications));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error searching notifications: " + e.getMessage(), null));
        }
    }

    // Clear all notifications
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<String>> clearAllNotifications() {
        try {
            adminNotificationService.clearAllNotifications();
            return ResponseEntity.ok(new ApiResponse<>(true, "All notifications cleared successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error clearing notifications: " + e.getMessage(), null));
        }
    }

    // Get notification statistics
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminNotificationService.NotificationStatistics>> getStatistics() {
        try {
            AdminNotificationService.NotificationStatistics stats = adminNotificationService.getNotificationStatistics();
            return ResponseEntity.ok(new ApiResponse<>(true, "Statistics retrieved successfully", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving statistics: " + e.getMessage(), null));
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
