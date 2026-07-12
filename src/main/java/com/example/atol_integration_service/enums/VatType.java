package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VatType {
    NONE("none"),
    VAT0("vat0"),
    VAT10("vat10"),
    VAT20("vat20"),
    VAT110("vat110"),
    VAT120("vat120"),
    VAT5("vat5"),
    VAT7("vat7"),
    VAT105("vat105"),
    VAT107("vat107");

    private final String value;

    VatType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}