package com.example.atol_integration_service.dto;

import com.example.atol_integration_service.enums.*;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AtolReceiptDto {
    private String external_id;
    private String timestamp;
    private Receipt receipt;

    @Data @Builder
    public static class Receipt {
        private Client client;
        private Company company;
        private List<Item> items;
        private List<Payment> payments;
        private List<Vat> vats;
        private Double total;
    }

    @Data @Builder
    public static class Client {
        private String email;
    }

    @Data @Builder
    public static class Company {
        private String email;
        private TaxSystem sno;
        private String inn;
        private String payment_address;
       // private String location;
    }

    @Data @Builder
    public static class Item {
        private String name;
        private double price;
        private double quantity;
        private Measure measure;
        private double sum;
        private PaymentMethod payment_method;
        private PaymentObject payment_object;
        private Vat vat;
    }

    @Data @Builder
    public static class Vat {
        private VatType type;
        private Double sum;
    }

    @Data @Builder
    public static class Payment {
        private PaymentType type;
        private double sum;
    }
}