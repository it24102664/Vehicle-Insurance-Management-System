package com.example.Insurance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_user_reports")
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is CORRECT - it will map to 'user_id' column
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserR userR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_report_id", nullable = false)
    private AdminReport adminReport;

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "viewed_date")
    private LocalDateTime viewedDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Constructors
    public UserReport() {}

    public UserReport(UserR userR, AdminReport adminReport) {
        this.userR = userR;
        this.adminReport = adminReport;
        this.createdDate = LocalDateTime.now();
        this.isFavorite = false;
    }

    // All getters and setters remain the same...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserR getUserR() { return userR; }
    public void setUserR(UserR userR) { this.userR = userR; }

    public AdminReport getAdminReport() { return adminReport; }
    public void setAdminReport(AdminReport adminReport) { this.adminReport = adminReport; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }

    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }

    public LocalDateTime getViewedDate() { return viewedDate; }
    public void setViewedDate(LocalDateTime viewedDate) { this.viewedDate = viewedDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
