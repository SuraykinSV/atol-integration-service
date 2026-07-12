package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {

    FULL_PREPAYMENT("full_prepayment"),
    PREPAYMENT("prepayment"),
    ADVANCE("advance"),
    FULL_PAYMENT("full_payment"),
    PARTIAL_PAYMENT("partial_payment"),
    CREDIT("credit"),
    CREDIT_PAYMENT("credit_payment");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
