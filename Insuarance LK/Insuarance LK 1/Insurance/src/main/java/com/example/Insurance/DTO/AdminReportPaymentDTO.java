package com.example.Insurance.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class AdminReportPaymentDTO {

    private Long id;

    @NotBlank(message = "Payment month is required")
    @Pattern(regexp = "^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)$",
            message = "Payment month must be a valid 3-letter month abbreviation")
    @JsonProperty("month")
    private String month;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be greater than 0")
    @JsonProperty("amount")
    private Double amount;

    // Constructors
    public AdminReportPaymentDTO() {}

    public AdminReportPaymentDTO(String month, Double amount) {
        this.month = month;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
