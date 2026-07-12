package com.example.atol_integration_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransactionDto {
    private String id;
    private String customerEmail;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private String name;
        private double price;
        private double quantity;
    }
}
