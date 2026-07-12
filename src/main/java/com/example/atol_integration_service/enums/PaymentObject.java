package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentObject {
    COMMODITY(1),
    EXCISE(2),
    JOB(3),
    SERVICE(4),
    GAMBLING_BET(5),
    GAMBLING_PRIZE(6),
    LOTTERY_TICKET(7),
    LOTTERY_PRIZE(8),
    INTELLECTUAL_ACTIVITY(9),
    PAYMENT(10),
    AGENT_COMMISSION(11),
    CONTRIBUTION_PENALTY_FINE(12),
    ANOTHER(13),
    PROPERTY_RIGHT(14),
    NON_OPERATING_GAIN(15),
    OTHER_CONTRIBUTIONS(16),
    SALES_TAX(17),
    RESORT_FEE(18),
    DEPOSIT(19),
    CONSUMPTION(20),
    PENSION_INSURANCE_IP(21),
    PENSION_INSURANCE(22),
    MEDICAL_INSURANCE_IP(23),
    MEDICAL_INSURANCE(24),
    SOCIAL_INSURANCE(25),
    CASINO_PAYMENT(26),
    BANK_AGENT_PAYMENT(27),
    EXCISE_WITH_MARKING(30),
    EXCISE_WITHOUT_MARKING(31),
    COMMODITY_WITH_MARKING(32),
    COMMODITY_WITHOUT_MARKING(33);

    private final int value;

    PaymentObject(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
