package com.stellar.crm.inventoryservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_events")
public class ProcessedEvent implements Persistable<UUID> {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID correlationId;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    @Override
    public @Nullable UUID getId() {
        return correlationId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
