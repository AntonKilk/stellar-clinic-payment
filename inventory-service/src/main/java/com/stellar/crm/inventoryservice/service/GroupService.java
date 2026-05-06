package com.stellar.crm.inventoryservice.service;

import com.stellar.crm.inventoryservice.dto.GroupCreateRequest;
import com.stellar.crm.inventoryservice.dto.GroupResponse;
import com.stellar.crm.inventoryservice.model.Group;
import com.stellar.crm.inventoryservice.model.ProcessedEvent;
import com.stellar.crm.inventoryservice.repository.GroupRepository;
import com.stellar.crm.inventoryservice.repository.GroupSpecification;
import com.stellar.crm.inventoryservice.repository.ProcessedEventRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final ProcessedEventRepository processedEventRepository;

    public GroupResponse createGroup(GroupCreateRequest request) {
        Group group = new Group();
        group.setGuid(UUID.randomUUID());
        group.setGroupRefId(request.groupRefId());
        group.setGroupLimit(request.groupLimit());
        group.setCurrentCount(0);
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        groupRepository.save(group);
        return toResponse(group);
    }

    public GroupResponse reserveSlot(UUID groupRefId) {
        Group group = groupRepository.findByGroupRefId(groupRefId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupRefId));
        if (group.getCurrentCount() >= group.getGroupLimit()) {
            throw new IllegalStateException("Group is full");
        }
        group.setCurrentCount(group.getCurrentCount() + 1);
        group.setUpdatedAt(Instant.now());
        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse releaseSlot(UUID groupRefId, UUID correlationId) {
        if (processedEventRepository.existsById(correlationId)) {
            return toResponse(groupRepository.findByGroupRefId(groupRefId)
                    .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupRefId)));
        }
        Group group = groupRepository.findByGroupRefId(groupRefId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupRefId));
        if (group.getCurrentCount() <= 0) {
            throw new IllegalStateException("Count already zero");
        }
        group.setCurrentCount(group.getCurrentCount() - 1);
        group.setUpdatedAt(Instant.now());
        groupRepository.save(group);
        processedEventRepository.save(new ProcessedEvent(correlationId, Instant.now()));
        return toResponse(group);
    }

    public Page<GroupResponse> findAll(UUID groupRefId, Boolean hasAvailablePlaces, final Pageable pageable) {
        return groupRepository.findAll(
                        GroupSpecification.byFilter(groupRefId, hasAvailablePlaces),
                        pageable
                )
                .map(this::toResponse);
    }

    private GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getGuid(),
                group.getGroupRefId(),
                group.getCurrentCount(),
                group.getGroupLimit()
        );
    }

}
