package com.example.atol_integration_service.controllers;
import com.example.atol_integration_service.dto.GeneralResponse;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.ReceiptStatus;
import com.example.atol_integration_service.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> receiveTransaction(@Valid @RequestBody TransactionDto transaction) {
        log.info("Получена транзакция: {}", transaction.getId());

        GeneralResponse<?> response = receiptService.processTransaction(transaction);

        if (response.getStatus().equals(ReceiptStatus.FAIL.toString())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


    @GetMapping("/check/{transactionId}")
    public ResponseEntity<GeneralResponse<?>> getReceiptStatus(@PathVariable String transactionId) {
        log.info("Запрошена ручная проверка статуса для транзакции: {}", transactionId);
        return ResponseEntity.ok(receiptService.checkFiscalData(transactionId));
    }

}