package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.*;
import com.example.atol_integration_service.mapper.ReceiptMapper;
import com.example.atol_integration_service.model.ReceiptRecord;
import com.example.atol_integration_service.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReceiptService {

    private final AuthService authService;
    private final AtolClient atolClient;
    private final ReceiptMapper receiptMapper;
    private final ReceiptRepository receiptRepository;

    public void processTransaction(TransactionDto transaction) {
        log.info("Начало обработки транзакции: {}", transaction.getId());

        AtolReceiptDto receiptDto = receiptMapper.mapToAtolDto(transaction);

        saveReceipt(transaction.getId(), receiptDto, ReceiptStatus.DRAFT);

        String token = authService.getValidToken();
        if (token == null) {
            log.error("Невозможно отправить чек, токен не получен.");
            updateStatus(transaction.getId(), ReceiptStatus.ERROR_NO_TOKEN);
            return;
        }

        ResponseEntity<String> response = atolClient.sendReceipt(token, receiptDto);

        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            updateStatus(transaction.getId(), ReceiptStatus.REGISTERED);
            log.info("Чек {} успешно зарегистрирован в АТОЛ!", transaction.getId());
        } else {
            updateStatus(transaction.getId(), ReceiptStatus.ERROR_REGISTRATION);
            log.error("Ошибка регистрации чека {}", transaction.getId());
        }
    }

    private void saveReceipt(String id, AtolReceiptDto receipt, ReceiptStatus status) {
        ReceiptRecord record = new ReceiptRecord();
        record.setId(id);
        record.setReceiptData(receipt);
        record.setStatus(status);
        receiptRepository.save(record);
        log.info("Чек [{}] сохранен в БД со статусом : {}", id, status);
    }

    private void updateStatus(String id, ReceiptStatus newStatus) {
        receiptRepository.findById(id).ifPresent(record -> {
            record.setStatus(newStatus);
            receiptRepository.save(record);
            log.info("Статус чека [{}] в БД изменен на: {}", id, newStatus);
        });
    }

    public ReceiptRecord getReceiptInfo(String transactionId) {return receiptRepository.findById(transactionId).orElse(null);}
}