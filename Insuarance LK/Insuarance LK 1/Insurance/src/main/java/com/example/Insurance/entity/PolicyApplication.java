package com.example.Insurance.entity;

import com.example.Insurance.Enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "policy_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String nic;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String vehicleDetails;

    @Column(length = 1000)
    private String additionalNotes;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private LocalDate reviewedDate;
    private String reviewNotes;

    public String getVehicleMake() {

        return "";
    }

    public String getVehicleModel() {

        return "";
    }

    public String getVehicleRegistrationNumber() {

        return "";
    }

    public String getName() {

        return "";
    }
}

