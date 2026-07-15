package com.example.atol_integration_service.model;

import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.enums.ReceiptStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "receipts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptRecord {
    @Id
    private String id;

    @Column(name = "receipt_data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private AtolReceiptDto receiptData;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReceiptStatus status;
}
