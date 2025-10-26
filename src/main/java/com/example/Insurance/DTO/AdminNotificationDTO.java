package com.example.Insurance.DTO;


import com.example.Insurance.entity.AdminNotification;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AdminNotificationDTO {

    private Long id;
    private String title;
    private String message;
    private AdminNotification.NotificationType type;
    private AdminNotification.PriorityLevel priority;
    private AdminNotification.TargetAudience target;
    private AdminNotification.NotificationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiryDate;

    private Integer sentCount;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedDate;

    private Boolean isActive;

    // Constructors
    public AdminNotificationDTO() {}

    public AdminNotificationDTO(String title, String message,
                                AdminNotification.NotificationType type,
                                AdminNotification.PriorityLevel priority,
                                AdminNotification.TargetAudience target) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.target = target;
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

    public AdminNotification.NotificationType getType() {
        return type;
    }

    public void setType(AdminNotification.NotificationType type) {
        this.type = type;
    }

    public AdminNotification.PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(AdminNotification.PriorityLevel priority) {
        this.priority = priority;
    }

    public AdminNotification.TargetAudience getTarget() {
        return target;
    }

    public void setTarget(AdminNotification.TargetAudience target) {
        this.target = target;
    }

    public AdminNotification.NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(AdminNotification.NotificationStatus status) {
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

    @Override
    public String toString() {
        return "AdminNotificationDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", target=" + target +
                ", status=" + status +
                ", created=" + created +
                ", scheduleDate=" + scheduleDate +
                ", expiryDate=" + expiryDate +
                ", sentCount=" + sentCount +
                ", createdBy='" + createdBy + '\'' +
                ", updatedDate=" + updatedDate +
                ", isActive=" + isActive +
                '}';
    }

    public boolean getTarget(AdminNotification.TargetAudience targetAudience) {

        return false;
    }
}
