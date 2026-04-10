package com.stellar.crm.inventoryservice.repository;

import com.stellar.crm.inventoryservice.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID>, JpaSpecificationExecutor<Group> {
    Optional<Group> findByGroupRefId(UUID groupRefId);
}

