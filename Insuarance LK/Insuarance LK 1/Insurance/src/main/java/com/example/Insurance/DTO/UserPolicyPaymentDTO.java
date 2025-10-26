package com.example.Insurance.DTO;

import java.math.BigDecimal;
import java.util.List;

public class UserPolicyPaymentDTO {
    private Long policyId;
    private String policyNumber;
    private String policyType;
    private String vehicle;
    private BigDecimal monthlyPremium;
    private String status;
    private List<String> paidMonths;
    private List<String> pendingMonths;
    private List<PaymentHistoryDTO> paymentHistory;

    // Default constructor
    public UserPolicyPaymentDTO() {}

    // Getters and Setters
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }

    public BigDecimal getMonthlyPremium() { return monthlyPremium; }
    public void setMonthlyPremium(BigDecimal monthlyPremium) { this.monthlyPremium = monthlyPremium; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getPaidMonths() { return paidMonths; }
    public void setPaidMonths(List<String> paidMonths) { this.paidMonths = paidMonths; }

    public List<String> getPendingMonths() { return pendingMonths; }
    public void setPendingMonths(List<String> pendingMonths) { this.pendingMonths = pendingMonths; }

    public List<PaymentHistoryDTO> getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(List<PaymentHistoryDTO> paymentHistory) { this.paymentHistory = paymentHistory; }
}
