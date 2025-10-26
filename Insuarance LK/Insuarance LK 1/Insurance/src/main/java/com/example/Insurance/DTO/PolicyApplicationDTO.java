package com.example.Insurance.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyApplicationDTO {
    private Long policyId;
    private String applicantName;
    private Integer age;
    private String nic;
    private String address;
    private String phone;
    private String email;
    private String vehicleDetails;
    private String additionalNotes;
}
