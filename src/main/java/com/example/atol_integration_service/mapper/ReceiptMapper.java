package com.example.atol_integration_service.mapper;

import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.TransactionDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReceiptMapper {

    public AtolReceiptDto mapToAtolDto(TransactionDto td) {
        String timestamp = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());

        AtolReceiptDto.Company company = AtolReceiptDto.Company.builder()   //
                .inn(td.getMerchant().getTaxId())
                .sno(td.getMerchant().getTaxSystem())
                .email(td.getMerchant().getEmail())
                .payment_address(td.getMerchant().getWebSite())
                .build();

        AtolReceiptDto.Client client = AtolReceiptDto.Client.builder()  //
                .email(td.getConsumer().getEmail())
                .phone(td.getConsumer().getPhone())
                .build();

        List<AtolReceiptDto.Item> atolItems = new ArrayList<>();    //

        for (TransactionDto.ItemDto tdItem : td.getItems()) {

            BigDecimal price = tdItem.getPrice();
            BigDecimal quantity = tdItem.getQuantity();
            BigDecimal sum = price.multiply(quantity);

            atolItems.add(AtolReceiptDto.Item.builder()
                    .name(tdItem.getDesc())
                    .price(price)
                    .quantity(quantity)
                    .measure(tdItem.getMeasure())
                    .sum(sum)
                    .payment_object(tdItem.getType())
                    .payment_method(tdItem.getMode())
                    .vat(AtolReceiptDto.Vat.builder().type(tdItem.getTaxRate()).build())
                    .build());
        }

        List<AtolReceiptDto.Payment> atolPayments = new ArrayList<>();  //
        for (TransactionDto.PaymentDto tdPayment : td.getPayments()) {
            atolPayments.add(AtolReceiptDto.Payment.builder()
                    .type(tdPayment.getPaymentType())
                    .sum(tdPayment.getAmt())
                    .build());
        }

        return AtolReceiptDto.builder()
                .external_id(td.getId())
                .timestamp(timestamp)
                .receipt(AtolReceiptDto.Receipt.builder()
                        .client(client)
                        .company(company)
                        .items(atolItems)
                        .payments(atolPayments)
                        .total(td.getAmount())
                        .build())
                .build();
    }
}
