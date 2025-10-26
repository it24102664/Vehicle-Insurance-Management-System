package com.example.Insurance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_report_payments")
public class AdminReportPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_month", nullable = false, length = 10)
    private String paymentMonth;

    @Column(name = "payment_amount", nullable = false)
    private Double paymentAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_report_id", nullable = false)
    private AdminReport adminReport;

    // Constructors
    public AdminReportPayment() {}

    public AdminReportPayment(String paymentMonth, Double paymentAmount, AdminReport adminReport) {
        this.paymentMonth = paymentMonth;
        this.paymentAmount = paymentAmount;
        this.adminReport = adminReport;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentMonth() {
        return paymentMonth;
    }

    public void setPaymentMonth(String paymentMonth) {
        this.paymentMonth = paymentMonth;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public AdminReport getAdminReport() {
        return adminReport;
    }

    public void setAdminReport(AdminReport adminReport) {
        this.adminReport = adminReport;
    }
}
