package com.stellar.crm.inventoryservice.repository;

import com.stellar.crm.inventoryservice.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
