package com.example.atol_integration_service.model;

import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.enums.ReceiptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptRecord {
    private String id;
    private AtolReceiptDto receipt;
    private ReceiptStatus status;
    //private LocalDateTime createdAt;

    public ReceiptRecord(AtolReceiptDto receipt, ReceiptStatus status) {
            this.receipt = receipt;
            this.status = status;
        }
}
