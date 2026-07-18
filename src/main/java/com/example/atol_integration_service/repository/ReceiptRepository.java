package com.example.atol_integration_service.repository;

import com.example.atol_integration_service.model.ReceiptRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptRecord, String> {
    Optional<ReceiptRecord> findByAtolUuid(String atolUuid);

}
