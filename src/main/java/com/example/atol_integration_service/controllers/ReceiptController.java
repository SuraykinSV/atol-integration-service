package com.example.atol_integration_service.controllers;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.service.AuthService;
import com.example.atol_integration_service.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<String> recieveTransaction(@RequestBody TransactionDto transaction) {
        log.info("Получена транзакция: {}", transaction);

        if(transaction != null && transaction.getId() != null) {
            log.info("Id транзакции: {}", transaction.getId());
            receiptService.processTransaction(transaction);
            return ResponseEntity.ok("Транзакция принята");
        }else{
            log.warn("В транзакции отсутсвует id");
            return ResponseEntity.badRequest().body("Ошибка: транзакция не содержит id");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReceiptInfo(@PathVariable String id) {
        log.info("Информации о чеке: {}", id);

        var receiptRecord = receiptService.getReceiptInfo(id);

        if (receiptRecord != null) {
            return ResponseEntity.ok(receiptRecord);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}