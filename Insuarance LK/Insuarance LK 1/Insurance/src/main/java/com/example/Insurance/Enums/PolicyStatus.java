package com.example.Insurance.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyStatus {
    DRAFT,
    INACTIVE,
    ACTIVE, APPROVED;

    @JsonCreator
    public static PolicyStatus fromString(String value) {
        if (value == null) return ACTIVE; // Default value
        return PolicyStatus.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}