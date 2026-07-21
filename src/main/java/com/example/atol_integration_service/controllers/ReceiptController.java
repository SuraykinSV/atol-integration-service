package com.example.atol_integration_service.controllers;
import com.example.atol_integration_service.dto.GeneralResponse;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<GeneralResponse<String>> recieveTransaction(@Valid @RequestBody TransactionDto transaction) {
        log.info("Получена транзакция: {}", transaction.getId());

        receiptService.processTransaction(transaction);

        GeneralResponse<String> response = GeneralResponse.<String>builder()
                .status("DONE")
                .message("Транзакция принята в обработку")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .data(transaction.getId())
                .build();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/check/{transactionId}")
    public ResponseEntity<GeneralResponse<?>> getReceiptStatus(@PathVariable String transactionId) {
        log.info("Запрошена ручная проверка статуса для транзакции: {}", transactionId);

        GeneralResponse<?> response = receiptService.checkFiscalData(transactionId);

        return ResponseEntity.ok(response);
    }

}