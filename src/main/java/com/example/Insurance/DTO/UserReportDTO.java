package com.example.Insurance.DTO;



import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class UserReportDTO {

    private Long id;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("adminReportId")
    private Long adminReportId;

    @JsonProperty("userNotes")
    private String userNotes;

    @JsonProperty("isFavorite")
    private Boolean isFavorite;

    @JsonProperty("viewedDate")
    private LocalDateTime viewedDate;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("updatedDate")
    private LocalDateTime updatedDate;

    // Admin report details for easy access
    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("customerNIC")
    private String customerNIC;

    @JsonProperty("customerPhone")
    private String customerPhone;

    @JsonProperty("reportYear")
    private Integer reportYear;

    @JsonProperty("appliedPolicies")
    private String appliedPolicies;

    @JsonProperty("claimsInfo")
    private String claimsInfo;

    @JsonProperty("claimDate")
    private LocalDateTime claimDate;

    @JsonProperty("paidMonths")
    private Integer paidMonths;

    @JsonProperty("totalAmount")
    private Double totalAmount;

    @JsonProperty("averageAmount")
    private Double averageAmount;

    @JsonProperty("paymentData")
    private List<AdminReportPaymentDTO> paymentData;

    // Constructors
    public UserReportDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAdminReportId() { return adminReportId; }
    public void setAdminReportId(Long adminReportId) { this.adminReportId = adminReportId; }

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

    // Admin report getters and setters
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

    public LocalDateTime getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDateTime claimDate) { this.claimDate = claimDate; }

    public Integer getPaidMonths() { return paidMonths; }
    public void setPaidMonths(Integer paidMonths) { this.paidMonths = paidMonths; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getAverageAmount() { return averageAmount; }
    public void setAverageAmount(Double averageAmount) { this.averageAmount = averageAmount; }

    public List<AdminReportPaymentDTO> getPaymentData() { return paymentData; }
    public void setPaymentData(List<AdminReportPaymentDTO> paymentData) { this.paymentData = paymentData; }
}
