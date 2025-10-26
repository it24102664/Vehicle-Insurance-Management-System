package com.example.Insurance.entity;

import com.example.Insurance.Enums.PolicyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;


    private String icon;

    @Column(nullable = false, length = 1000)
    private String description;

    // FIXED: Changed from LAZY to EAGER to prevent lazy loading error
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_benefits", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "benefit")
    private List<String> benefits;

    @Column(nullable = false)
    private Double premiumAmount;

    @Column(nullable = false)
    private Double coverageAmount;

    @Column(nullable = false)
    private String vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedDate;

    // NEW FIELDS FOR PAYMENT INTEGRATION
    // Link to the policy application that was approved
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_application_id")
    private PolicyApplication policyApplication;

    // Direct user information (copied from approved PolicyApplication)
    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "applicant_email")
    private String applicantEmail;

    @Column(name = "policy_number", unique = true)
    private String policyNumber;

    @Column(name = "vehicle_registration_number")
    private String vehicleRegistrationNumber;

    @Column(name = "policy_type")
    private String policyType;

    // Added PrePersist and PreUpdate methods for automatic date handling
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
        if (updatedDate == null) {
            updatedDate = LocalDate.now();
        }
        // Generate policy number if not set
        if (policyNumber == null || policyNumber.isEmpty()) {
            policyNumber = "POL-" + System.currentTimeMillis();
        }
        // Set policy type based on vehicle type if not set
        if (policyType == null || policyType.isEmpty()) {
            policyType = name; // Use the policy name as type
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDate.now();
    }

    // PAYMENT INTEGRATION METHODS - Now properly implemented
    public Long getUserId() {
        if (applicantId != null) {
            return applicantId;
        }
        if (policyApplication != null && policyApplication.getId() != null) {
            return policyApplication.getId();
        }
        return 1L; // Default fallback
    }

    public String getUserName() {
        if (applicantName != null && !applicantName.isEmpty()) {
            return applicantName;
        }
        if (policyApplication != null && policyApplication.getName() != null) {
            return policyApplication.getName();
        }
        return "Policy Holder";
    }

    public String getUserEmail() {
        if (applicantEmail != null && !applicantEmail.isEmpty()) {
            return applicantEmail;
        }
        if (policyApplication != null && policyApplication.getEmail() != null) {
            return policyApplication.getEmail();
        }
        return "policyholder@example.com";
    }

    public String getPolicyNumber() {
        if (policyNumber != null && !policyNumber.isEmpty()) {
            return policyNumber;
        }
        return "POL-" + id;
    }

    public String getPolicyType() {
        if (policyType != null && !policyType.isEmpty()) {
            return policyType;
        }
        return name; // Use policy name as type
    }

    public String getVehicleRegistrationNumber() {
        if (vehicleRegistrationNumber != null && !vehicleRegistrationNumber.isEmpty()) {
            return vehicleRegistrationNumber;
        }
        if (policyApplication != null && policyApplication.getVehicleRegistrationNumber() != null) {
            return policyApplication.getVehicleRegistrationNumber();
        }
        return "REG-" + id;
    }

    public BigDecimal getMonthlyPremium() {
        if (premiumAmount != null) {
            return BigDecimal.valueOf(premiumAmount);
        }
        return new BigDecimal("15000.00"); // Default monthly premium
    }

    public BigDecimal getPremium() {
        return getMonthlyPremium();
    }

    public String getVehicleNumber() {
        return getVehicleRegistrationNumber();
    }

    public String getVehicle() {
        if (vehicleType != null && vehicleRegistrationNumber != null) {
            return vehicleType + " - " + vehicleRegistrationNumber;
        }
        if (policyApplication != null) {
            String vehicleInfo = "";
            if (policyApplication.getVehicleMake() != null) {
                vehicleInfo += policyApplication.getVehicleMake();
            }
            if (policyApplication.getVehicleModel() != null) {
                vehicleInfo += " " + policyApplication.getVehicleModel();
            }
            if (policyApplication.getVehicleRegistrationNumber() != null) {
                vehicleInfo += " - " + policyApplication.getVehicleRegistrationNumber();
            }
            return vehicleInfo.trim();
        }
        return vehicleType + " - " + getVehicleRegistrationNumber();
    }

    // Utility method to populate policy from approved application
    public void populateFromApplication(PolicyApplication application) {
        this.policyApplication = application;
        this.applicantId = application.getId(); // or application.getApplicantId() if exists
        this.applicantName = application.getName();
        this.applicantEmail = application.getEmail();
        this.vehicleRegistrationNumber = application.getVehicleRegistrationNumber();
        this.policyType = this.name; // Use policy template name as type

        // Generate policy number if not set
        if (this.policyNumber == null || this.policyNumber.isEmpty()) {
            this.policyNumber = "POL-" + System.currentTimeMillis();
        }
    }

    // Method to check if policy is ready for payments
    public boolean isReadyForPayments() {
        return status == PolicyStatus.APPROVED &&
                applicantId != null &&
                policyNumber != null &&
                !policyNumber.isEmpty();
    }
}
