package com.example.atol_integration_service.service;
import com.example.atol_integration_service.enums.ReceiptStatus;
import com.example.atol_integration_service.model.ReceiptRecord;
import com.example.atol_integration_service.repository.ReceiptRepository;
import com.example.atol_integration_service.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptSchedulerService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptService receiptService;


    @Scheduled(fixedDelayString = "${scheduler.interval.receipt-check}")
    public void processUnfinishedReceipts() {
        log.info("[ШЕДУЛЕР] Запуск проверки чеков в статусе WAIT...");

        List<ReceiptRecord> waitReceipts = receiptRepository.findByStatus(ReceiptStatus.WAIT);

        if (waitReceipts.isEmpty()) return;

        log.info("Найдено чеков для обработки: {}", waitReceipts.size());

        for (ReceiptRecord record : waitReceipts) {
            receiptService.handleWaitReceipt(record);
        }
    }
}