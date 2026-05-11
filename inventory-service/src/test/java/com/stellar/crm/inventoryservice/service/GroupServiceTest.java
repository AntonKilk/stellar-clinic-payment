package com.stellar.crm.inventoryservice.service;

import com.stellar.crm.inventoryservice.dto.GroupCreateRequest;
import com.stellar.crm.inventoryservice.dto.GroupResponse;
import com.stellar.crm.inventoryservice.model.Group;
import com.stellar.crm.inventoryservice.model.ProcessedEvent;
import com.stellar.crm.inventoryservice.repository.GroupRepository;
import com.stellar.crm.inventoryservice.repository.ProcessedEventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private static final int GROUP_LIMIT = 10;
    private static final int GROUP_LIMIT_LARGE = 20;
    private static final int INITIAL_COUNT = 3;
    private static final int INITIAL_COUNT_AFTER_RESERVE = 4;
    private static final int PARTIAL_COUNT = 5;
    private static final int PARTIAL_COUNT_AFTER_RELEASE = 4;
    private static final int FIND_ALL_COUNT = 2;
    private static final int FIND_ALL_LIMIT = 5;
    private static final int PAGE_SIZE = 10;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void shouldCreateGroupWithCorrectFields() {
        final UUID refId = UUID.randomUUID();
        final GroupCreateRequest request = new GroupCreateRequest(refId, GROUP_LIMIT_LARGE);
        when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final GroupResponse response = groupService.createGroup(request);

        assertThat(response.groupRefId()).isEqualTo(refId);
        assertThat(response.groupLimit()).isEqualTo(GROUP_LIMIT_LARGE);
        assertThat(response.currentCount()).isZero();
        assertThat(response.guid()).isNotNull();
    }

    @Test
    void shouldPersistGroupOnCreate() {
        final GroupCreateRequest request = new GroupCreateRequest(UUID.randomUUID(), GROUP_LIMIT);
        when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        groupService.createGroup(request);

        final ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentCount()).isZero();
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldIncrementCurrentCountOnReserve() {
        final Group group = buildGroup(UUID.randomUUID(), INITIAL_COUNT, GROUP_LIMIT);
        when(groupRepository.findByGroupRefId(group.getGroupRefId())).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final GroupResponse response = groupService.reserveSlot(group.getGroupRefId());

        assertThat(response.currentCount()).isEqualTo(INITIAL_COUNT_AFTER_RESERVE);
    }

    @Test
    void shouldThrowWhenGroupNotFoundOnReserve() {
        final UUID refId = UUID.randomUUID();
        when(groupRepository.findByGroupRefId(refId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.reserveSlot(refId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(refId.toString());
    }

    @Test
    void shouldThrowWhenGroupFullOnReserve() {
        final Group group = buildGroup(UUID.randomUUID(), GROUP_LIMIT, GROUP_LIMIT);
        when(groupRepository.findByGroupRefId(group.getGroupRefId())).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.reserveSlot(group.getGroupRefId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Group is full");
    }

    @Test
    void shouldDecrementCurrentCountOnRelease() {
        final Group group = buildGroup(UUID.randomUUID(), PARTIAL_COUNT, GROUP_LIMIT);
        when(groupRepository.findByGroupRefId(group.getGroupRefId())).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final GroupResponse response = groupService.releaseSlot(group.getGroupRefId(), UUID.randomUUID());

        assertThat(response.currentCount()).isEqualTo(PARTIAL_COUNT_AFTER_RELEASE);
    }

    @Test
    void shouldThrowWhenGroupNotFoundOnRelease() {
        final UUID refId = UUID.randomUUID();
        when(groupRepository.findByGroupRefId(refId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.releaseSlot(refId, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(refId.toString());
    }

    @Test
    void shouldThrowWhenCountAlreadyZeroOnRelease() {
        final Group group = buildGroup(UUID.randomUUID(), 0, GROUP_LIMIT);
        when(groupRepository.findByGroupRefId(group.getGroupRefId())).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.releaseSlot(group.getGroupRefId(), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Count already zero");
    }

    @Test
    void shouldNotDecrementWhenAlreadyProcessed() {
        final UUID correlationId = UUID.randomUUID();
        final Group group = buildGroup(UUID.randomUUID(), PARTIAL_COUNT, GROUP_LIMIT);
        when(processedEventRepository.existsById(correlationId)).thenReturn(true);
        when(groupRepository.findByGroupRefId(group.getGroupRefId())).thenReturn(Optional.of(group));

        final GroupResponse response = groupService.releaseSlot(group.getGroupRefId(), correlationId);

        assertThat(response.currentCount()).isEqualTo(PARTIAL_COUNT);
        verify(groupRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void shouldSaveProcessedEventOnSuccessfulRelease() {
        final UUID correlationId = UUID.randomUUID();
        final Group group = buildGroup(UUID.randomUUID(), PARTIAL_COUNT, GROUP_LIMIT);
        when(groupRepository.findByGroupRefId(group.getGroupRefId())).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        groupService.releaseSlot(group.getGroupRefId(), correlationId);

        final ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());
        assertThat(captor.getValue().getCorrelationId()).isEqualTo(correlationId);
        assertThat(captor.getValue().getProcessedAt()).isNotNull();
    }

    @Test
    void shouldReturnMappedPageOnFindAll() {
        final Group group = buildGroup(UUID.randomUUID(), FIND_ALL_COUNT, FIND_ALL_LIMIT);
        final Page<Group> page = new PageImpl<>(List.of(group));
        when(groupRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        final Page<GroupResponse> result = groupService.findAll(null, null, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).guid()).isEqualTo(group.getGuid());
        assertThat(result.getContent().get(0).currentCount()).isEqualTo(FIND_ALL_COUNT);
        assertThat(result.getContent().get(0).groupLimit()).isEqualTo(FIND_ALL_LIMIT);
    }

    @Test
    void shouldReturnEmptyPageWhenNoGroupsFound() {
        when(groupRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        final Page<GroupResponse> result = groupService.findAll(UUID.randomUUID(), true, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getContent()).isEmpty();
    }

    private Group buildGroup(final UUID groupRefId, final int currentCount, final int limit) {
        final Group group = new Group();
        group.setGuid(UUID.randomUUID());
        group.setGroupRefId(groupRefId);
        group.setCurrentCount(currentCount);
        group.setGroupLimit(limit);
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        return group;
    }
}
