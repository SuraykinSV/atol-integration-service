package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaxSystem {
    OSN("osn"),
    USN_INCOME("usn_income"),
    USN_INCOME_OUTCOME("usn_income_outcome"),
    ENVD("envd"),
    ESN("esn"),
    PATENT("patent");

    private final String value;

    TaxSystem(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}