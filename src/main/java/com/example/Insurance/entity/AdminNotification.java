

package com.example.Insurance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notifications")
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience")
    private TargetAudience target;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    @Column(name = "created_date")
    private LocalDateTime created;


    private LocalDateTime scheduleDate;


    private LocalDateTime expiryDate;

    @Column(name = "sent_count", columnDefinition = "INT DEFAULT 0")
    private Integer sentCount;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive;

    // Enums
    public enum NotificationType {
        GENERAL, UPDATE, PROMOTION, MAINTENANCE, SECURITY
    }

    public enum PriorityLevel {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum TargetAudience {
        ALL, ACTIVE, INACTIVE, PREMIUM, NEW
    }

    public enum NotificationStatus {
        DRAFT, SENT, SCHEDULED, EXPIRED
    }

    // Constructors
    public AdminNotification() {
        this.created = LocalDateTime.now();
        this.isActive = true;
        this.sentCount = 0;
    }

    public AdminNotification(String title, String message, NotificationType type,
                             PriorityLevel priority, TargetAudience target) {
        this();
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.target = target;
        this.status = NotificationStatus.DRAFT;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TargetAudience getTarget() {
        return target;
    }

    public void setTarget(TargetAudience target) {
        this.target = target;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDateTime scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getSentCount() {
        return sentCount;
    }

    public void setSentCount(Integer sentCount) {
        this.sentCount = sentCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AdminNotification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", status=" + status +
                ", created=" + created +
                '}';
    }
}

