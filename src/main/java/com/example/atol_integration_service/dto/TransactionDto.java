package com.example.atol_integration_service.dto;

import com.example.atol_integration_service.enums.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransactionDto {
    @NotBlank(message = "ID транзакции обязателен")
    private String id;

    private String type;

    @NotNull(message = "Итоговая сумма обязательна")
    @DecimalMin(value = "0.0", message = "Сумма не может быть отрицательной")
    private BigDecimal amount;

    @NotEmpty(message = "Чек должен содержать хотя бы один товар")
    private List<ItemDto> items;

    @Valid
    private MerchantDto merchant;

    @Valid
    private ConsumerDto consumer;

    @NotEmpty(message = "Должна быть передана хотя бы одна оплата")
    private List<PaymentDto> payments;

    @Data
    public static class ItemDto {
        @NotBlank(message = "Название товара обязательно")
        private String desc;

        @NotNull(message = "Тип предмета расчета обязателен")
        private PaymentObject type;

        @NotNull(message = "Количество обязательно")
        @DecimalMin(value = "0.0", message = "Количество должно быть больше 0")
        private BigDecimal quantity;

        @NotNull(message = "Цена обязательна")
        @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
        private BigDecimal price;

        @NotNull(message = "Единица измерения обязательна")
        private Measure measure;

        @NotNull(message = "Ставка НДС обязательна")
        private VatType taxRate;

        @NotNull(message = "Признак способа расчета обязателен")
        private PaymentMethod mode;

        private String prodCode;
    }

    @Data
    public static class MerchantDto {
        @JsonProperty("tax_id")
        @NotBlank(message = "ИНН обязателен")
        private String taxId;

        @JsonProperty("tax_system")
        @NotNull(message = "Система налогообложения обязательна")
        private TaxSystem taxSystem;

        @NotBlank(message = "email компании обязателен")
        private String email;

        @NotBlank(message = "Место расчетов обязательно")
        @JsonProperty("web_site")
        private String webSite;
    }

    @Data
    public static class ConsumerDto {
        private String email;
        private String phone;
    }

    @Data
    public static class PaymentDto {
        @JsonProperty("payment_type")
        @NotNull(message = "Тип оплаты обязателен")
        private PaymentType paymentType;
        @NotNull(message = "Сумма оплаты обязательна")
        @Min(value = 0, message = "Сумма оплаты не может быть отрицательной")
        private BigDecimal amt;
    }
}