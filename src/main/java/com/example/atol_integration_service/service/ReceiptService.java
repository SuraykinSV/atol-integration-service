package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.*;
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

    public void processTransaction(TransactionDto transaction) {
        log.info("Начало обработки транзакции: {}", transaction.getId());

        AtolReceiptDto receiptDto = mapToAtolDto(transaction);
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

    private AtolReceiptDto mapToAtolDto(TransactionDto td) {
        String timestamp = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());

        double totalSum = 0;
        List<AtolReceiptDto.Item> atolItems = new ArrayList<>();

        for (TransactionDto.ItemDto tdItem : td.getItems()) {
            double sum = tdItem.getPrice() * tdItem.getQuantity();
            totalSum += sum;
            atolItems.add(AtolReceiptDto.Item.builder()
                    .name(tdItem.getName())
                    .price(tdItem.getPrice())
                    .quantity(tdItem.getQuantity())
                    .measure(Measure.PIECE)
                    .sum(sum)
                    .payment_method(PaymentMethod.FULL_PAYMENT)
                    .payment_object(PaymentObject.COMMODITY)
                    .vat(AtolReceiptDto.Vat.builder().type(VatType.NONE).sum(0.0).build())
                    .build());
        }

        return AtolReceiptDto.builder()
                .external_id(td.getId())
                .timestamp(timestamp)
                .receipt(AtolReceiptDto.Receipt.builder()
                        .client(AtolReceiptDto.Client.builder().email(td.getCustomerEmail()).build())
                        .company(AtolReceiptDto.Company.builder()
                                .email("shop@mail.ru")
                                .sno(TaxSystem.USN_INCOME)
                                .inn("5817153219")
                                .payment_address("https://shop.ru")
                                .build())
                        .items(atolItems)
                        .payments(List.of(AtolReceiptDto.Payment.builder().type(PaymentType.ELECTRONIC).sum(totalSum).build()))
                        .vats(List.of(AtolReceiptDto.Vat.builder().type(VatType.NONE).sum(0.0).build()))
                        .total(totalSum)
                        .build())
                .build();
    }

    public ReceiptRecord getReceiptInfo(String transactionId) {
        return receiptStorage.getReceipt(transactionId);
    }
}