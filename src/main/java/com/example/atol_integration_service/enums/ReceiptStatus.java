package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReceiptStatus {
    WAIT("wait"),
    DONE("done"),
    ERROR_NO_TOKEN("ERROR_NO_TOKEN"),
    FAIL("fail");
    private final String value;

    ReceiptStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
