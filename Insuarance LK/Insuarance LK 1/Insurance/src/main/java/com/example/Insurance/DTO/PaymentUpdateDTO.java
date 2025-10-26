package com.example.Insurance.DTO;

import com.example.Insurance.entity.BankSlipDetails;
import com.example.Insurance.entity.OnlinePaymentDetails;
import com.example.Insurance.Enums.PaymentMethod;
import com.example.Insurance.Enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentUpdateDTO {
    private Long paymentId; // Optional, for reference
    private String paymentMonth;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status; // Optional, might be read-only
    private BankSlipDetails bankSlipDetails;
    private OnlinePaymentDetails onlinePaymentDetails;

    // Getters and Setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getPaymentMonth() { return paymentMonth; }
    public void setPaymentMonth(String paymentMonth) { this.paymentMonth = paymentMonth; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public BankSlipDetails getBankSlipDetails() { return bankSlipDetails; }
    public void setBankSlipDetails(BankSlipDetails bankSlipDetails) { this.bankSlipDetails = bankSlipDetails; }
    public OnlinePaymentDetails getOnlinePaymentDetails() { return onlinePaymentDetails; }
    public void setOnlinePaymentDetails(OnlinePaymentDetails onlinePaymentDetails) { this.onlinePaymentDetails = onlinePaymentDetails; }
}