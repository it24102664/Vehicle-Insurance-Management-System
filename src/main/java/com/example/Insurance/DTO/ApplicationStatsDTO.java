package com.example.Insurance.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatsDTO {
    private long pending;
    private long approved;
    private long rejected;
    private long duplicates;
}
