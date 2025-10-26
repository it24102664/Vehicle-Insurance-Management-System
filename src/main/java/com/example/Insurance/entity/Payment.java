package com.example.Insurance.entity;

import com.example.Insurance.Enums.PaymentMethod;
import com.example.Insurance.Enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "id")
    private Policy policy;

    @Column(nullable = false)
    private String paymentMonth;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime submittedDate;

    private LocalDateTime approvedDate;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    private String adminComments;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_slip_id")
    private BankSlipDetails bankSlipDetails;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "online_payment_id")
    private OnlinePaymentDetails onlinePaymentDetails;

    // Default constructor
    public Payment() {
        this.submittedDate = LocalDateTime.now();
        this.expiryTime = LocalDateTime.now().plusHours(12);
    }

    // Constructor with policy
    public Payment(@NotNull Policy policy, String paymentMonth, BigDecimal amount) {
        this();
        this.policy = policy;
        this.paymentMonth = paymentMonth;
        this.amount = amount;
        this.userId = policy.getUserId();
        this.userName = policy.getUserName();
        this.userEmail = policy.getUserEmail();
    }

    // Getters and Setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }

    public String getPaymentMonth() { return paymentMonth; }
    public void setPaymentMonth(String paymentMonth) { this.paymentMonth = paymentMonth; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public BankSlipDetails getBankSlipDetails() { return bankSlipDetails; }
    public void setBankSlipDetails(BankSlipDetails bankSlipDetails) { this.bankSlipDetails = bankSlipDetails; }

    public OnlinePaymentDetails getOnlinePaymentDetails() { return onlinePaymentDetails; }
    public void setOnlinePaymentDetails(OnlinePaymentDetails onlinePaymentDetails) { this.onlinePaymentDetails = onlinePaymentDetails; }

    public void setUpdatedDate(LocalDateTime now) {

    }
}
