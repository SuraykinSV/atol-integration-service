package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.AtolResponseDto;
import com.example.atol_integration_service.dto.GeneralResponse;
import com.example.atol_integration_service.dto.TransactionDto;
import com.example.atol_integration_service.enums.PaymentType;
import com.example.atol_integration_service.enums.ReceiptStatus;
import com.example.atol_integration_service.mapper.ReceiptMapper;
import com.example.atol_integration_service.model.ReceiptRecord;
import com.example.atol_integration_service.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {
    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private AtolClient atolClient;

    @Mock
    private AuthService authService;

    @Mock private ReceiptMapper receiptMapper;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void tokenIsNullTest(){
        TransactionDto transaction = createTransaction("test-no-token");
        when(authService.getValidToken()).thenReturn(null);
        when(receiptMapper.mapToAtolDto(any())).thenReturn(new AtolReceiptDto());

        GeneralResponse<?> response = receiptService.processTransaction(transaction);

        assertEquals(ReceiptStatus.WAIT.toString(), response.getStatus());
        assertEquals("Нет токена, отправка отложена", response.getMessage());

        verify(receiptRepository, times(1)).save(any(ReceiptRecord.class));
    }

    @Test
    void uuidIsNullTest() {
        ReceiptRecord record = new ReceiptRecord();
        record.setId("test-no-uuid");
        record.setReceiptData(new AtolReceiptDto());
        record.setAtolUuid(null);

        when(authService.getValidToken()).thenReturn("valid-token");

        AtolResponseDto mockResponse = new AtolResponseDto();
        mockResponse.setUuid("new-uuid");
        when(atolClient.sendReceipt(eq("valid-token"), any())).thenReturn(mockResponse);

        receiptService.handleWaitReceipt(record);

        assertEquals("new-uuid", record.getAtolUuid());
        verify(receiptRepository, times(1)).save(record);
    }

    @Test
    void AtolReturnsUuidTest() {
        TransactionDto transaction = new TransactionDto();
        transaction.setId("test-123");
        transaction.setAmount(BigDecimal.valueOf(150.0));
        TransactionDto.ItemDto item = new TransactionDto.ItemDto();
        item.setPrice(BigDecimal.valueOf(150.0));
        item.setQuantity(BigDecimal.valueOf(1.0));
        transaction.setItems(java.util.List.of(item));

        TransactionDto.PaymentDto payment = new TransactionDto.PaymentDto();
        payment.setPaymentType(PaymentType.EXTENDED);
        payment.setAmt(BigDecimal.valueOf(150.0));
        transaction.setPayments(java.util.List.of(payment));


        when(authService.getValidToken()).thenReturn("fake-token");
        AtolResponseDto fakeResponse = new AtolResponseDto();
        fakeResponse.setUuid("new-uuid-777");
        when(atolClient.sendReceipt(eq("fake-token"), any())).thenReturn(fakeResponse);

        GeneralResponse<?> response = receiptService.processTransaction(transaction);

        assertEquals(ReceiptStatus.WAIT.toString(), response.getStatus());
        assertEquals("Чек передан в АТОЛ, обрабатывается", response.getMessage());

        verify(receiptRepository, times(1)).save(any(ReceiptRecord.class));
    }
    @Test
    void ReceiptStatusIsDoneTest() {
        ReceiptRecord record = new ReceiptRecord();
        record.setId("test-status-done");
        record.setStatus(ReceiptStatus.DONE);
        AtolResponseDto.PayloadDto payload = new AtolResponseDto.PayloadDto();
        record.setFiscalData(payload);

        when(receiptRepository.findById("test-status-done")).thenReturn(Optional.of(record));

        GeneralResponse<?> response = receiptService.checkFiscalData("test-status-done");

        assertEquals(ReceiptStatus.DONE.toString(), response.getStatus());
        assertEquals(record.getFiscalData(), response.getData());
    }
    @Test
    void  ReceiptStatusIsFailTest() {
        ReceiptRecord record = new ReceiptRecord();
        record.setId("test-status-fail");
        record.setStatus(ReceiptStatus.FAIL);
        record.setErrorDetails("Некорректная сумма чека");
        when(receiptRepository.findById("test-status-fail")).thenReturn(Optional.of(record));

        GeneralResponse<?> response = receiptService.checkFiscalData("test-status-fail");

        assertEquals(ReceiptStatus.FAIL.toString(), response.getStatus());
        assertEquals("Некорректная сумма чека", response.getMessage());
    }

    @Test
    void AtolResponse4xxTest() {
        TransactionDto transaction = createTransaction("test-returns40xx");
        when(authService.getValidToken()).thenReturn("fake-token");

        HttpClientErrorException badRequestException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "Неверный ИНН".getBytes(),
                null
        );
        when(atolClient.sendReceipt(anyString(), any())).thenThrow(badRequestException);

        GeneralResponse<?> response = receiptService.processTransaction(transaction);

        assertEquals(ReceiptStatus.FAIL.toString(), response.getStatus());
        verify(receiptRepository, times(1)).save(any(ReceiptRecord.class));
    }
    @Test
    void AtolResponse5xxTest() {
        TransactionDto transaction = createTransaction("test-returns5xx");
        when(authService.getValidToken()).thenReturn("fake-token");

        HttpServerErrorException serverErrorException = HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                null,
                null
        );
        when(atolClient.sendReceipt(anyString(), any())).thenThrow(serverErrorException);

        GeneralResponse<?> response = receiptService.processTransaction(transaction);

        assertEquals(ReceiptStatus.WAIT.toString(), response.getStatus());
        verify(receiptRepository, times(1)).save(any(ReceiptRecord.class));
    }


    private TransactionDto createTransaction(String id) {
        TransactionDto transaction = new TransactionDto();
        transaction.setId(id);
        transaction.setAmount(BigDecimal.valueOf(150.0));

        TransactionDto.ItemDto item = new TransactionDto.ItemDto();
        item.setPrice(BigDecimal.valueOf(150.0));
        item.setQuantity(BigDecimal.valueOf(1.0));
        transaction.setItems(java.util.List.of(item));

        TransactionDto.PaymentDto payment = new TransactionDto.PaymentDto();
        payment.setPaymentType(PaymentType.EXTENDED);
        payment.setAmt(BigDecimal.valueOf(150.0));
        transaction.setPayments(java.util.List.of(payment));

        return transaction;
    }
}