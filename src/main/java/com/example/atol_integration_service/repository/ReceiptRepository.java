package com.example.atol_integration_service.repository;

import com.example.atol_integration_service.model.ReceiptRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptRecord, String> {
}
