package com.example.atol_integration_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TransactionDto {
    private String id;
    private String type;
    private Double amount;
    private List<ItemDto> items;
    private MerchantDto merchant;
    private ConsumerDto consumer;
    private List<PaymentDto> payments;

    @Data
    public static class ItemDto {
        private String desc;
        private String type;
        private Double quantity;
        private Double price;
        private Integer measure;
        private String taxRate;
        private String mode;
        private String prodCode;
    }

    @Data
    public static class MerchantDto {
        @JsonProperty("tax_id")
        private String taxId;

        @JsonProperty("tax_system")
        private String taxSystem;

        private String email;

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
        private String paymentType;

        private Double amt;
    }
}