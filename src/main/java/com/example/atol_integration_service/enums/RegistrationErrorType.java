package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegistrationErrorType {
    SYSTEM("system"),
    DRIVER("driver"),
    TIMEOUT("timeout"),
    UNKNOWN("unknown");

    private final String value;

    RegistrationErrorType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
