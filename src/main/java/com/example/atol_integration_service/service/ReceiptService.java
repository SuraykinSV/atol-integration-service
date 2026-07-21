package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.AtolResponseDto;
import com.example.atol_integration_service.dto.GeneralResponse;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.*;
import com.example.atol_integration_service.exceptions.ValidationException;
import com.example.atol_integration_service.mapper.ReceiptMapper;
import com.example.atol_integration_service.model.ReceiptRecord;
import com.example.atol_integration_service.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class ReceiptService {

    private final AuthService authService;
    private final AtolClient atolClient;
    private final ReceiptMapper receiptMapper;
    private final ReceiptRepository receiptRepository;
//
    public void processTransaction(TransactionDto transaction) {
        validateTransactionBusinessLogic(transaction);
        log.info("Начало обработки транзакции: {}", transaction.getId());

        AtolReceiptDto receiptDto = receiptMapper.mapToAtolDto(transaction);

        String token = authService.getValidToken();
        if (token == null || token.isBlank()) {
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


    /*public ReceiptRecord getReceiptInfo(String transactionId) {return receiptRepository.findById(transactionId).orElse(null);}*/

//
    private void validateTransactionBusinessLogic(TransactionDto td) {

        BigDecimal calculatedItemsSum = BigDecimal.ZERO;
        for (TransactionDto.ItemDto item : td.getItems()) {
            BigDecimal price = item.getPrice();
            BigDecimal quantity = item.getQuantity();
            calculatedItemsSum = calculatedItemsSum.add(price.multiply(quantity));
        }

        BigDecimal calculatedPaymentsSum = BigDecimal.ZERO;
        for (TransactionDto.PaymentDto payment : td.getPayments()) {
            BigDecimal amt = payment.getAmt();
            calculatedPaymentsSum = calculatedPaymentsSum.add(amt);
        }

        if (calculatedItemsSum.compareTo(td.getAmount()) != 0) {
            throw new ValidationException(String.format("Сумма товаров (%s) не совпадает с итоговой суммой чека (%s)", calculatedItemsSum, td.getAmount()));
        }

        if (calculatedPaymentsSum.compareTo(td.getAmount()) != 0) {
            throw new ValidationException(String.format("Внесенная оплата (%s) не покрывает итоговую сумму чека (%s)", calculatedPaymentsSum, td.getAmount()));
        }
    }
//
    private ReceiptStatus mapAtolStatus(String atolStatus) {
        if (atolStatus == null) return ReceiptStatus.FAIL;

        return switch (atolStatus.toLowerCase()) {
            case "wait" -> ReceiptStatus.WAIT;
            case "done" -> ReceiptStatus.DONE;
            default -> ReceiptStatus.FAIL;
        };
    }

//
    private void saveFailedReceipt(String id, AtolReceiptDto receiptDto, ReceiptStatus status) {
        ReceiptRecord record = new ReceiptRecord();
        record.setId(id);
        record.setReceiptData(receiptDto);
        record.setStatus(status);
        receiptRepository.save(record);
    }

//
public GeneralResponse<?> checkFiscalData(String transactionId) {
    log.info("Проверяем статус чека для транзакции: {}", transactionId);

    ReceiptRecord record = receiptRepository.findById(transactionId)
            .orElseThrow(() -> new ValidationException("Транзакция с ID " + transactionId + " не найдена в БД"));

    if (record.getStatus() == ReceiptStatus.DONE && record.getFiscalData() != null) {
        return new GeneralResponse<>(ReceiptStatus.DONE.toString(), "Фискальные данные получены",
                LocalDateTime.now().toString(), record.getFiscalData());
    }

    if (record.getStatus() == ReceiptStatus.ERROR_NO_TOKEN || record.getStatus() == ReceiptStatus.ERROR_REGISTRATION) {
        return new GeneralResponse<>(ReceiptStatus.ERROR_REGISTRATION.toString(), "Ошибка на этапе отправки чека в АТОЛ",
                LocalDateTime.now().toString(), record.getReceiptData());
    }

    if (record.getAtolUuid() != null) {
        String token = authService.getValidToken();
        if (token == null || token.isBlank()) {
            return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Внутренняя ошибка: нет токена для опроса АТОЛ",
                    LocalDateTime.now().toString(), null);
        }

        try {
            AtolResponseDto response = atolClient.getReceiptStatus(record.getAtolUuid(), token);

            if (response == null) {
                return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Нет ответа от АТОЛ (null)",
                        LocalDateTime.now().toString(), null);
            }

            ReceiptStatus newStatus = mapAtolStatus(response.getStatus());

            if (newStatus == ReceiptStatus.DONE && response.getPayload() != null) {
                record.setStatus(newStatus);
                record.setFiscalData(response.getPayload());
                receiptRepository.save(record);
                return new GeneralResponse<>(ReceiptStatus.DONE.toString(), "Фискальные данные успешно получены",
                        LocalDateTime.now().toString(), response.getPayload());
            }
            else if (newStatus == ReceiptStatus.FAIL) {
                record.setStatus(newStatus);
                receiptRepository.save(record);
                return new GeneralResponse<>(ReceiptStatus.FAIL.toString(), "АТОЛ отклонил чек при фискализации",
                        LocalDateTime.now().toString(), response.getError());
            }
            else {
                return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Чек находится в очереди на пробитие (wait)",
                        LocalDateTime.now().toString(), null);
            }

        } catch (Exception e) {
            log.error("Сетевая или системная ошибка при обращении к АТОЛ: {}", e.getMessage());
            return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Сервис АТОЛ временно недоступен",
                    LocalDateTime.now().toString(), null);
        }
    }

    return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Чек обрабатывается", LocalDateTime.now().toString(), null);
}
}