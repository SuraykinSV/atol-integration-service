package com.example.atol_integration_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Measure {
    PIECE(0),
    GRAM(10),
    KILOGRAM(11),
    TON(12),
    CENTIMETER(20),
    DECIMETER(21),
    METER(22),
    SQUARE_CENTIMETER(30),
    SQUARE_DECIMETER(31),
    SQUARE_METER(32),
    MILLILITER(40),
    LITER(41),
    CUBIC_METER(42),
    KILOWATT_HOUR(50),
    GIGACALORIE(51),
    DAY(70),
    HOUR(71),
    MINUTE(72),
    SECOND(73),
    KILOBYTE(80),
    MEGABYTE(81),
    GIGABYTE(82),
    TERABYTE(83),
    OTHER(255);

    private final int value;

    Measure(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}