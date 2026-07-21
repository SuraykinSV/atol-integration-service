package com.example.atol_integration_service.dto;

import com.example.atol_integration_service.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtolReceiptDto {
    private String external_id;
    private String timestamp;
    private Receipt receipt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data @Builder
    public static class Receipt {
        private Client client;
        private Company company;
        private List<Item> items;
        private List<Payment> payments;
        private BigDecimal total;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data @Builder
    public static class Client {
        private String email;
        private String phone;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data @Builder
    public static class Company {
        private String email;
        private TaxSystem sno;
        private String inn;
        private String payment_address;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data @Builder
    public static class Item {
        private String name;
        private BigDecimal price;
        private BigDecimal quantity;
        private Measure measure;
        private BigDecimal sum;
        private PaymentMethod payment_method;
        private PaymentObject payment_object;
        private Vat vat;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data @Builder
    public static class Vat {
        private VatType type;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data @Builder
    public static class Payment {
        private PaymentType type;
        private BigDecimal sum;
    }
}