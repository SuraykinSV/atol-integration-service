package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.AtolResponseDto;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.*;
import com.example.atol_integration_service.exceptions.ValidationException;
import com.example.atol_integration_service.mapper.ReceiptMapper;
import com.example.atol_integration_service.model.ReceiptRecord;
import com.example.atol_integration_service.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        validateTransactionBusinessLogic(transaction);
        log.info("Начало обработки транзакции: {}", transaction.getId());

        AtolReceiptDto receiptDto = receiptMapper.mapToAtolDto(transaction);

        String token = authService.getValidToken();
        if (token == null) {
            log.error("Невозможно отправить чек, токен не получен.");
            saveFailedReceipt(transaction.getId(), receiptDto, ReceiptStatus.ERROR_NO_TOKEN);
            return;
        }

         try {
            AtolResponseDto response = atolClient.sendReceipt(token, receiptDto);
            ReceiptRecord record = new ReceiptRecord();
            record.setId(transaction.getId());
            record.setReceiptData(receiptDto);

            if (response != null && response.getUuid() != null) {
                record.setAtolUuid(response.getUuid());
                record.setStatus(ReceiptStatus.WAIT);
                log.info("Чек {} отправлен в АТОЛ. UUID: {}", transaction.getId(), response.getUuid());
            } else {
                record.setStatus(ReceiptStatus.ERROR_REGISTRATION);
                log.error("АТОЛ вернул пустой UUID для чека {}", transaction.getId());
            }
            receiptRepository.save(record);

        } catch (Exception e) {
            log.error("Ошибка при отправке чека {} в АТОЛ", transaction.getId(), e);
            saveFailedReceipt(transaction.getId(), receiptDto, ReceiptStatus.ERROR_REGISTRATION);
        }
    }


    public ReceiptRecord getReceiptInfo(String transactionId) {return receiptRepository.findById(transactionId).orElse(null);}


    private void validateTransactionBusinessLogic(TransactionDto tx) {
        double calculatedItemsSum = tx.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double calculatedPaymentsSum = tx.getPayments().stream()
                .mapToDouble(TransactionDto.PaymentDto::getAmt)
                .sum();

        if (Math.abs(calculatedItemsSum - tx.getAmount()) > 0.001) {
            throw new ValidationException(String.format("Сумма товаров (%.2f) не совпадает с итоговой суммой чека (%.2f)", calculatedItemsSum, tx.getAmount()));
        }

        if (Math.abs(calculatedPaymentsSum - tx.getAmount()) > 0.001) {
            throw new ValidationException(String.format("Внесенная оплата (%.2f) не покрывает итоговую сумму чека (%.2f)", calculatedPaymentsSum, tx.getAmount()));
        }
    }

    private ReceiptStatus mapAtolStatus(String atolStatus) {
        if (atolStatus == null) return ReceiptStatus.FAIL;
        switch (atolStatus) {
            case "wait":
                return ReceiptStatus.WAIT;
            case "done":
                return ReceiptStatus.DONE;
            case "fail":
                return ReceiptStatus.FAIL;
            default:
                return ReceiptStatus.FAIL;
        }
    }

    public void checkAndSaveFiscalData(String uuid) {
        log.info("Запрашиваем статус чека по UUID: {}", uuid);

        String token = authService.getValidToken();
        if (token == null || token.isBlank()) {
            log.error("Не удалось получить токен для проверки статуса чека {}", uuid);
            return;
        }

        AtolResponseDto response = atolClient.getReceiptStatus(uuid, token);

        ReceiptRecord record = receiptRepository.findByAtolUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Чек с UUID " + uuid + " не найден в БД"));

        ReceiptStatus newStatus = mapAtolStatus(response.getStatus().getValue());
        record.setStatus(newStatus);

        if (newStatus == ReceiptStatus.DONE && response.getPayload() != null) {
            record.setFiscalData(response.getPayload());
            log.info("Фискальные данные успешно получены и сохранены для чека {}", record.getId());
        } else if (newStatus == ReceiptStatus.FAIL) {
            log.error("Ошибка пробития чека в АТОЛ (fail). UUID: {}", uuid);
        }
        receiptRepository.save(record);
    }

    private void saveFailedReceipt(String id, AtolReceiptDto receiptDto, ReceiptStatus status) {
        ReceiptRecord record = new ReceiptRecord();
        record.setId(id);
        record.setReceiptData(receiptDto);
        record.setStatus(status);
        receiptRepository.save(record);
    }

}