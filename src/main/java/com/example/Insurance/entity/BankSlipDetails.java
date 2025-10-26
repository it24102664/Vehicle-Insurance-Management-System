package com.example.Insurance.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "bank_slip_details")
public class BankSlipDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private LocalDate depositDate;

    @Column(nullable = false)
    private String referenceNumber;

    @Column(nullable = false)
    private String depositorName;

    private String bankSlipImagePath;

    // Constructors
    public BankSlipDetails() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public LocalDate getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(LocalDate depositDate) {
        this.depositDate = depositDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getDepositorName() {
        return depositorName;
    }

    public void setDepositorName(String depositorName) {
        this.depositorName = depositorName;
    }

    public String getBankSlipImagePath() {
        return bankSlipImagePath;
    }

    public void setBankSlipImagePath(String bankSlipImagePath) {
        this.bankSlipImagePath = bankSlipImagePath;
    }
}
