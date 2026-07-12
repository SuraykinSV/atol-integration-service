package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentType {
    CASH(0),
    ELECTRONIC(1),
    PREPAID(2),
    CREDIT(3),
    OTHER(4),
    EXTENDED(5);
    private final int value;

    PaymentType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}