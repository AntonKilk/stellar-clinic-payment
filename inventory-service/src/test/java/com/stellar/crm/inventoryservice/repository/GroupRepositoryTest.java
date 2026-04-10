package com.stellar.crm.inventoryservice.repository;

import com.stellar.crm.inventoryservice.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GroupRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("inventory_db")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private GroupRepository groupRepository;

    private Group fullGroup;
    private Group availableGroup;

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
        // full group: currentCount == groupLimit, no available places
        fullGroup = groupRepository.save(buildGroup(UUID.randomUUID(), 10, 10));
        // available group: currentCount < groupLimit
        availableGroup = groupRepository.save(buildGroup(UUID.randomUUID(), 5, 10));
    }

    @Test
    void shouldReturnGroupMatchingGroupRefId() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(fullGroup.getGroupRefId(), null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getGuid()).isEqualTo(fullGroup.getGuid());
    }

    @Test
    void shouldNotReturnGroupsWithDifferentGroupRefId() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(fullGroup.getGroupRefId(), null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).noneMatch(g -> g.getGuid().equals(availableGroup.getGuid()));
    }

    @Test
    void shouldReturnAllGroupsWhenGroupRefIdIsNull() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(null, null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyWhenGroupRefIdNotFound() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(UUID.randomUUID(), null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnOnlyGroupsWithAvailablePlacesWhenTrue() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(null, true),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getGuid()).isEqualTo(availableGroup.getGuid());
    }

    @Test
    void shouldReturnAllGroupsWhenAvailableIsFalse() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(null, false),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnAllGroupsWhenAvailableIsNull() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(null, null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldFilterByGroupRefIdAndAvailablePlaces() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(availableGroup.getGroupRefId(), true),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getGuid()).isEqualTo(availableGroup.getGuid());
    }

    @Test
    void shouldReturnEmptyWhenGroupRefIdMatchesButNoAvailablePlaces() {
        final Page<Group> result = groupRepository.findAll(
                GroupSpecification.byFilter(fullGroup.getGroupRefId(), true),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    private Group buildGroup(final UUID refId, final int currentCount, final int limit) {
        final Group group = new Group();
        group.setGuid(UUID.randomUUID());
        group.setGroupRefId(refId);
        group.setCurrentCount(currentCount);
        group.setGroupLimit(limit);
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        return group;
    }
}
