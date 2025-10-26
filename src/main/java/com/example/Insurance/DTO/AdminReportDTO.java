package com.example.Insurance.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminReportDTO {

    private Long id;

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
    private LocalDate claimDate;

    @JsonProperty("paidMonths")
    private Integer paidMonths;

    @JsonProperty("totalAmount")
    private Double totalAmount;

    @JsonProperty("averageAmount")
    private Double averageAmount;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("updatedDate")
    private LocalDateTime updatedDate;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("paymentData")
    private List<AdminReportPaymentDTO> paymentData;

    // Constructors
    public AdminReportDTO() {}

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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<AdminReportPaymentDTO> getPaymentData() { return paymentData; }
    public void setPaymentData(List<AdminReportPaymentDTO> paymentData) { this.paymentData = paymentData; }
}
