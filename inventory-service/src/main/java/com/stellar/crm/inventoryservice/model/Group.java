package com.stellar.crm.inventoryservice.model;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class Group implements Persistable<UUID> {


    @Id
    @Column(nullable = false, updatable = false)
    private UUID guid;

    @Column(name = "group_ref_id", nullable = false)
    private UUID groupRefId;

    @Column(name = "current_count", nullable = false)
    private Integer currentCount;

    @Column(name = "group_limit", nullable = false)
    private Integer groupLimit;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Transient
    @Getter(AccessLevel.NONE)
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return guid;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
