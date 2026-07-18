package com.example.atol_integration_service.controllers;
import com.example.atol_integration_service.dto.TransactionDto;
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
    public ResponseEntity<String> recieveTransaction(@Valid @RequestBody TransactionDto transaction) {
        log.info("Получена транзакция: {}", transaction);

        receiptService.processTransaction(transaction);
        return ResponseEntity.ok("Транзакция принята в обработку");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReceiptInfo(@PathVariable String id) {
        log.info("Запрошена информация о чеке: {}", id);
        var receiptRecord = receiptService.getReceiptInfo(id);
        if (receiptRecord != null) {
            return ResponseEntity.ok(receiptRecord);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/check/{uuid}")
    public ResponseEntity<String> forceCheck(@PathVariable String uuid) {
        log.info("Запрошена ручная проверка статуса для чека: {}", uuid);

        try {
            receiptService.checkAndSaveFiscalData(uuid);
            return ResponseEntity.ok("Запрос статуса успешно выполнен для: " + uuid);
        } catch (Exception e) {
            log.error("Ошибка при ручной проверке статуса: {}", e.getMessage());
            return ResponseEntity.status(500).body("Ошибка при проверке: " + e.getMessage());
        }
    }

}