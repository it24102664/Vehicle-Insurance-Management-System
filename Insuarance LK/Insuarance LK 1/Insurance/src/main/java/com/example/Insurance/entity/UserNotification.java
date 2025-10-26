package com.example.Insurance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "admin_notification_id", nullable = false)
    private Long adminNotificationId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level")
    private PriorityLevel priority;

    @Column(name = "is_read", columnDefinition = "BIT DEFAULT 0")
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "is_archived", columnDefinition = "BIT DEFAULT 0")
    private Boolean isArchived;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "is_deleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "sent_by")
    private String sentBy;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // Enums
    public enum NotificationType {
        GENERAL, UPDATE, PROMOTION, MAINTENANCE, SECURITY
    }

    public enum PriorityLevel {
        LOW, MEDIUM, HIGH, URGENT
    }

    // Constructors
    public UserNotification() {
        this.createdAt = LocalDateTime.now();
        this.receivedAt = LocalDateTime.now();
        this.isRead = false;
        this.isArchived = false;
        this.isDeleted = false;
    }

    public UserNotification(Long userId, Long adminNotificationId, String title, String message,
                            NotificationType type, PriorityLevel priority) {
        this();
        this.userId = userId;
        this.adminNotificationId = adminNotificationId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAdminNotificationId() {
        return adminNotificationId;
    }

    public void setAdminNotificationId(Long adminNotificationId) {
        this.adminNotificationId = adminNotificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        } else if (!isRead) {
            this.readAt = null;
        }
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
        if (isArchived && this.archivedAt == null) {
            this.archivedAt = LocalDateTime.now();
        } else if (!isArchived) {
            this.archivedAt = null;
        }
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
        if (isDeleted && this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        } else if (!isDeleted) {
            this.deletedAt = null;
        }
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    // Helper methods
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isActive() {
        return !isDeleted && !isExpired();
    }

    @Override
    public String toString() {
        return "UserNotification{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
