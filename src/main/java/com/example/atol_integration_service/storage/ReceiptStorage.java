package com.example.atol_integration_service.storage;

import com.example.atol_integration_service.dto.AtolReceiptDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ReceiptStorage {

    @Data
    public static class ReceiptRecord {
        private AtolReceiptDto receipt;
        private String status;

        public ReceiptRecord(AtolReceiptDto receipt, String status) {
            this.receipt = receipt;
            this.status = status;
        }
    }

    private final Map<String, ReceiptRecord> storage = new ConcurrentHashMap<>();

    public void save(String id, AtolReceiptDto receiptDto, String status) {
        storage.put(id, new ReceiptRecord(receiptDto, status));
        log.info("Чек [{}] сохранен в Storage со статусом: {}", id, status);
    }

    public void updateStatus(String id, String newStatus) {
        if (storage.containsKey(id)) {
            storage.get(id).setStatus(newStatus);
            log.info("Статус чека [{}] изменен на: {}", id, newStatus);
        }
    }

    public ReceiptRecord getReceipt(String id) {
        return storage.get(id);
    }
}