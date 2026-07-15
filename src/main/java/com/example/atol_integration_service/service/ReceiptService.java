package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.*;
import com.example.atol_integration_service.mapper.ReceiptMapper;
import com.example.atol_integration_service.model.ReceiptRecord;
import com.example.atol_integration_service.storage.ReceiptStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReceiptService {

    private final AuthService authService;
    private final AtolClient atolClient;
    private final ReceiptStorage receiptStorage;
    private final ReceiptMapper receiptMapper;

    public void processTransaction(TransactionDto transaction) {
        log.info("Начало обработки транзакции: {}", transaction.getId());

        AtolReceiptDto receiptDto = receiptMapper.mapToAtolDto(transaction);
        receiptStorage.save(transaction.getId(), receiptDto, ReceiptStatus.DRAFT);

        String token = authService.getValidToken();
        if (token == null) {
            log.error("Невозможно отправить чек, токен не получен.");
            receiptStorage.updateStatus(transaction.getId(), ReceiptStatus.ERROR_NO_TOKEN);
            return;
        }

        ResponseEntity<String> response = atolClient.sendReceipt(token, receiptDto);

        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            receiptStorage.updateStatus(transaction.getId(), ReceiptStatus.REGISTERED);
            log.info("Чек {} успешно зарегистрирован в АТОЛ!", transaction.getId());
        } else {
            receiptStorage.updateStatus(transaction.getId(), ReceiptStatus.ERROR_REGISTRATION);
            log.error("Ошибка регистрации чека {}", transaction.getId());
        }
    }


    public ReceiptRecord getReceiptInfo(String transactionId) {
        return receiptStorage.getReceipt(transactionId);
    }
}