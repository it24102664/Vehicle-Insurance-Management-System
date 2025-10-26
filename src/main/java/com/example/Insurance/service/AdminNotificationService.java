package com.example.Insurance.service;

import com.example.Insurance.DTO.AdminNotificationDTO;
import com.example.Insurance.entity.AdminNotification;
import com.example.Insurance.entity.UserNotification;
import com.example.Insurance.repository.AdminNotificationRepository;
import com.example.Insurance.repository.UserNotificationRepository;
import com.example.Insurance.observer.NotificationSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminNotificationService {

    @Autowired
    private AdminNotificationRepository adminNotificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private NotificationSubject notificationSubject;

    private final Random random = new Random();

    // Create new notification
    public AdminNotificationDTO createNotification(AdminNotificationDTO notificationDTO) {
        AdminNotification notification = convertToEntity(notificationDTO);
        notification.setStatus(AdminNotification.NotificationStatus.DRAFT);
        notification.setCreatedBy("admin");

        AdminNotification savedNotification = adminNotificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    // Get all active notifications
    public List<AdminNotificationDTO> getAllNotifications() {
        List<AdminNotification> notifications = adminNotificationRepository.findByIsActiveTrueOrderByCreatedDesc();
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get notification by ID
    public Optional<AdminNotificationDTO> getNotificationById(Long id) {
        Optional<AdminNotification> notification = adminNotificationRepository.findByIdAndIsActiveTrue(id);
        return notification.map(this::convertToDTO);
    }

    // Update notification
    public Optional<AdminNotificationDTO> updateNotification(Long id, AdminNotificationDTO notificationDTO) {
        Optional<AdminNotification> existingNotification = adminNotificationRepository.findByIdAndIsActiveTrue(id);

        if (existingNotification.isPresent()) {
            AdminNotification notification = existingNotification.get();

            notification.setTitle(notificationDTO.getTitle());
            notification.setMessage(notificationDTO.getMessage());
            notification.setType(notificationDTO.getType());
            notification.setPriority(notificationDTO.getPriority());
            notification.setTarget(notificationDTO.getTarget());
            notification.setScheduleDate(notificationDTO.getScheduleDate());
            notification.setExpiryDate(notificationDTO.getExpiryDate());
            notification.setUpdatedDate(LocalDateTime.now());

            AdminNotification updatedNotification = adminNotificationRepository.save(notification);
            return Optional.of(convertToDTO(updatedNotification));
        }

        return Optional.empty();
    }

    // Hard delete notification
    public boolean deleteNotification(Long id) {
        if (adminNotificationRepository.existsById(id)) {
            adminNotificationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Send notification immediately with Observer Pattern
    public Optional<AdminNotificationDTO> sendNotification(Long id) {
        Optional<AdminNotification> notificationOpt = adminNotificationRepository.findByIdAndIsActiveTrue(id);

        if (notificationOpt.isPresent()) {
            AdminNotification notification = notificationOpt.get();

            int sentCount = simulateNotificationSending(notification.getTarget());

            notification.setStatus(AdminNotification.NotificationStatus.SENT);
            notification.setSentCount(sentCount);
            notification.setUpdatedDate(LocalDateTime.now());

            AdminNotification updatedNotification = adminNotificationRepository.save(notification);

            // ✅ OBSERVER PATTERN: Notify all observers
            notificationSubject.setNotification(updatedNotification);

            return Optional.of(convertToDTO(updatedNotification));
        }

        return Optional.empty();
    }

    // Send new notification with Observer Pattern
    public AdminNotificationDTO sendNewNotification(AdminNotificationDTO notificationDTO) {
        AdminNotification notification = convertToEntity(notificationDTO);

        int sentCount = simulateNotificationSending(notification.getTarget());

        notification.setStatus(AdminNotification.NotificationStatus.SENT);
        notification.setSentCount(sentCount);
        notification.setCreatedBy("admin");

        AdminNotification savedNotification = adminNotificationRepository.save(notification);

        // ✅ OBSERVER PATTERN: Notify all observers
        notificationSubject.setNotification(savedNotification);

        return convertToDTO(savedNotification);
    }

    // Schedule notification
    public AdminNotificationDTO scheduleNotification(AdminNotificationDTO notificationDTO) {
        AdminNotification notification = convertToEntity(notificationDTO);
        notification.setStatus(AdminNotification.NotificationStatus.SCHEDULED);
        notification.setCreatedBy("admin");

        AdminNotification savedNotification = adminNotificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    // Get notifications by status
    public List<AdminNotificationDTO> getNotificationsByStatus(AdminNotification.NotificationStatus status) {
        List<AdminNotification> notifications = adminNotificationRepository.findByStatusAndIsActiveTrueOrderByCreatedDesc(status);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Search notifications
    public List<AdminNotificationDTO> searchNotifications(String keyword) {
        List<AdminNotification> notifications = adminNotificationRepository.searchByKeyword(keyword);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Hard delete all notifications
    public void clearAllNotifications() {
        adminNotificationRepository.deleteAll();
    }

    // Get notification statistics
    public NotificationStatistics getNotificationStatistics() {
        Long totalNotifications = (long) adminNotificationRepository.findByIsActiveTrueOrderByCreatedDesc().size();
        Long sentCount = (long) adminNotificationRepository.findByStatusAndIsActiveTrueOrderByCreatedDesc(AdminNotification.NotificationStatus.SENT).size();
        Long draftCount = (long) adminNotificationRepository.findByStatusAndIsActiveTrueOrderByCreatedDesc(AdminNotification.NotificationStatus.DRAFT).size();
        Long scheduledCount = (long) adminNotificationRepository.findByStatusAndIsActiveTrueOrderByCreatedDesc(AdminNotification.NotificationStatus.SCHEDULED).size();

        return new NotificationStatistics(totalNotifications, sentCount, draftCount, scheduledCount);
    }

    // Convert Entity to DTO
    private AdminNotificationDTO convertToDTO(AdminNotification notification) {
        AdminNotificationDTO dto = new AdminNotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setPriority(notification.getPriority());
        dto.setTarget(notification.getTarget());
        dto.setStatus(notification.getStatus());
        dto.setCreated(notification.getCreated());
        dto.setScheduleDate(notification.getScheduleDate());
        dto.setExpiryDate(notification.getExpiryDate());
        dto.setSentCount(notification.getSentCount());
        dto.setCreatedBy(notification.getCreatedBy());
        dto.setUpdatedDate(notification.getUpdatedDate());
        dto.setIsActive(notification.getIsActive());
        return dto;
    }

    // Convert DTO to Entity
    private AdminNotification convertToEntity(AdminNotificationDTO dto) {
        AdminNotification notification = new AdminNotification();
        if (dto.getId() != null) {
            notification.setId(dto.getId());
        }
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setPriority(dto.getPriority());
        notification.setTarget(dto.getTarget());
        notification.setScheduleDate(dto.getScheduleDate());
        notification.setExpiryDate(dto.getExpiryDate());
        return notification;
    }

    // Simulate notification sending
    private int simulateNotificationSending(AdminNotification.TargetAudience target) {
        int baseCount = switch (target) {
            case ALL -> 1000 + random.nextInt(500);
            case ACTIVE -> 700 + random.nextInt(300);
            case INACTIVE -> 200 + random.nextInt(100);
            case PREMIUM -> 150 + random.nextInt(100);
            case NEW -> 50 + random.nextInt(50);
        };
        return baseCount;
    }

    // Inner class for statistics
    public static class NotificationStatistics {
        private Long total;
        private Long sent;
        private Long draft;
        private Long scheduled;

        public NotificationStatistics(Long total, Long sent, Long draft, Long scheduled) {
            this.total = total;
            this.sent = sent;
            this.draft = draft;
            this.scheduled = scheduled;
        }

        // Getters
        public Long getTotal() { return total; }
        public Long getSent() { return sent; }
        public Long getDraft() { return draft; }
        public Long getScheduled() { return scheduled; }
    }
}