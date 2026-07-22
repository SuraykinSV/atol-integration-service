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
import org.springframework.web.client.HttpStatusCodeException;

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
    public GeneralResponse<?> processTransaction(TransactionDto transaction) {
        validateTransactionBusinessLogic(transaction);
        log.info("Начало обработки транзакции: {}", transaction.getId());

        AtolReceiptDto receiptDto = receiptMapper.mapToAtolDto(transaction);
        String token = authService.getValidToken();

        ReceiptRecord record = new ReceiptRecord();
        record.setId(transaction.getId());
        record.setReceiptData(receiptDto);

        if (token == null || token.isBlank()) {
            log.error("Невозможно отправить чек, токен не получен.");
            saveFailedReceipt(transaction.getId(), receiptDto, ReceiptStatus.ERROR_NO_TOKEN);
            return new GeneralResponse<>(ReceiptStatus.WAIT.toString(),
                    "Нет токена, отправка отложена",LocalDateTime.now().toString(),null);
        }

         try {
             AtolResponseDto response = atolClient.sendReceipt(token, receiptDto);

             record.setAtolUuid(response.getUuid());
             record.setStatus(ReceiptStatus.WAIT);
             receiptRepository.save(record);

             return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Чек передан в АТОЛ, обрабатывается",
                     LocalDateTime.now().toString(), transaction.getId());
         }catch (HttpStatusCodeException e) {

             if (e.getStatusCode().is4xxClientError()) {
                 record.setStatus(ReceiptStatus.FAIL);
                 record.setErrorDetails(e.getResponseBodyAsString());
                 receiptRepository.save(record);
                 return new GeneralResponse<>(ReceiptStatus.FAIL.toString(), e.getResponseBodyAsString(), LocalDateTime.now().toString(), null);

             } else if (e.getStatusCode().is5xxServerError()) {
                 record.setStatus(ReceiptStatus.WAIT);
                 receiptRepository.save(record);

                 return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Проблемы на стороне кассы, повторим позже", LocalDateTime.now().toString(), null);
             }

         } catch (Exception e) {
             record.setStatus(ReceiptStatus.WAIT);
             receiptRepository.save(record);
             return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Сетевая ошибка, повторим позже", LocalDateTime.now().toString(), null);
         }

        return new GeneralResponse<>(ReceiptStatus.FAIL.toString(), "Неизвестная ошибка", LocalDateTime.now().toString(), null);
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
    if (record.getStatus() == ReceiptStatus.DONE) {
        return new GeneralResponse<>(ReceiptStatus.DONE.toString(), "Чек успешно зарегистрирован", LocalDateTime.now().toString(), record.getFiscalData());
    } else if (record.getStatus() == ReceiptStatus.FAIL) {
        return new GeneralResponse<>(ReceiptStatus.FAIL.toString(), record.getErrorDetails(), LocalDateTime.now().toString(), null);
    } else {
        return new GeneralResponse<>(ReceiptStatus.WAIT.toString(), "Чек обрабатывается", LocalDateTime.now().toString(), null);
    }
    }
//
    public void handleWaitReceipt(ReceiptRecord record) {
        if (record.getAtolUuid() == null) {
            retrySendToAtol(record);
        } else {
            fetchFiscalDataFromAtol(record);
        }
    }

    private void retrySendToAtol(ReceiptRecord record) {
        log.info("[ШЕДУЛЕР] Попытка повторной отправки чека {}", record.getId());
        String token = authService.getValidToken();

        if (token == null || token.isBlank()) return;

        try {
            AtolResponseDto response = atolClient.sendReceipt(token, record.getReceiptData());

            if (response != null && response.getUuid() != null) {
                record.setAtolUuid(response.getUuid());
                log.info("[ШЕДУЛЕР] Чек {} успешно доставлен. UUID: {}", record.getId(), response.getUuid());
                receiptRepository.save(record);
            }
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            if (e.getStatusCode().is4xxClientError()) {
                record.setStatus(ReceiptStatus.FAIL);
                record.setErrorDetails(e.getResponseBodyAsString());
                receiptRepository.save(record);
            }
        } catch (Exception e) {
            log.warn("[ШЕДУЛЕР] Не удалось связаться с АТОЛ . Причина: {}", e.getMessage());
        }
    }

    private void fetchFiscalDataFromAtol(ReceiptRecord record) {
        log.info("[ШЕДУЛЕР] Запрос статуса регистрации для чека {} (UUID: {})", record.getId(), record.getAtolUuid());
        String token = authService.getValidToken();

        if (token == null || token.isBlank()) return;

        try {
            AtolResponseDto response = atolClient.getReceiptStatus(record.getAtolUuid(), token);

            if (response == null || response.getStatus() == null) return;

            ReceiptStatus newStatus = mapAtolStatus(response.getStatus());

            if (newStatus == ReceiptStatus.DONE) {
                record.setStatus(ReceiptStatus.DONE);
                record.setFiscalData(response.getPayload());
                log.info("[ШЕДУЛЕР] Фискальные данные для чека {} успешно получены", record.getId());
                receiptRepository.save(record);
            }
            else if (newStatus == ReceiptStatus.FAIL) {
                record.setStatus(ReceiptStatus.FAIL);
                String errorMsg = response.getError() != null ? response.getError().toString() : "Ошибка фискализации";
                record.setErrorDetails(errorMsg);
                log.error("[ШЕДУЛЕР] АТОЛ отклонил чек {}", record.getId());
                receiptRepository.save(record);
            }
        } catch (Exception e) {
            log.warn("[ШЕДУЛЕР] Не удалось связаться с АТОЛ. Причина: {}", e.getMessage());
        }
    }
}

