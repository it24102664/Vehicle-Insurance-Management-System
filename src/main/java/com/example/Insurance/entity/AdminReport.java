package com.example.Insurance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "admin_reports")
public class AdminReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_nic", nullable = false, length = 20)
    private String customerNIC;

    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;

    @Column(name = "report_year", nullable = false)
    private Integer reportYear;

    @Column(name = "applied_policies", columnDefinition = "TEXT")
    private String appliedPolicies;

    @Column(name = "claims_info", columnDefinition = "TEXT")
    private String claimsInfo;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "paid_months")
    private Integer paidMonths;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "average_amount")
    private Double averageAmount;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Relationship with payments
    @OneToMany(mappedBy = "adminReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdminReportPayment> payments;

    // Relationship with user reports
    @OneToMany(mappedBy = "adminReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserReport> userReports;

    // Constructors
    public AdminReport() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerNIC() { return customerNIC; }
    public void setCustomerNIC(String customerNIC) { this.customerNIC = customerNIC; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Integer getReportYear() { return reportYear; }
    public void setReportYear(Integer reportYear) { this.reportYear = reportYear; }

    public String getAppliedPolicies() { return appliedPolicies; }
    public void setAppliedPolicies(String appliedPolicies) { this.appliedPolicies = appliedPolicies; }

    public String getClaimsInfo() { return claimsInfo; }
    public void setClaimsInfo(String claimsInfo) { this.claimsInfo = claimsInfo; }

    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }

    public Integer getPaidMonths() { return paidMonths; }
    public void setPaidMonths(Integer paidMonths) { this.paidMonths = paidMonths; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getAverageAmount() { return averageAmount; }
    public void setAverageAmount(Double averageAmount) { this.averageAmount = averageAmount; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public List<AdminReportPayment> getPayments() { return payments; }
    public void setPayments(List<AdminReportPayment> payments) { this.payments = payments; }

    public List<UserReport> getUserReports() { return userReports; }
    public void setUserReports(List<UserReport> userReports) { this.userReports = userReports; }

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
