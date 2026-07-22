package com.example.atol_integration_service.repository;

import com.example.atol_integration_service.enums.ReceiptStatus;
import com.example.atol_integration_service.model.ReceiptRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptRecord, String> {
    List<ReceiptRecord> findByStatus(ReceiptStatus status);

}
