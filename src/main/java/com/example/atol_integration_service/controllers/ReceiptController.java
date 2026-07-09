package com.example.atol_integration_service.controllers;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<String> recieveTransaction(@RequestBody TransactionDto transaction) {
        log.info("Получен пост запрос в контроллере: {}", transaction);
        if(transaction.getId() != null) {
            log.info("Id транзакции: {}", transaction.getId());
            authService.runTokenUpdate();
            return ResponseEntity.ok("Транзакция принята");
        }else{
            log.warn("В транзакции отсутсвует id");
            return ResponseEntity.badRequest().body("Ошибка: транзакция не содержит id");
        }
    }

}