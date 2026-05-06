package com.stellar.crm.inventoryservice.kafka;

import com.stellar.crm.inventoryservice.kafka.dto.CancellationRequest;
import com.stellar.crm.inventoryservice.kafka.dto.CancellationResponse;
import com.stellar.crm.inventoryservice.service.GroupService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancellationRequestListenerTest {

    @Mock
    private GroupService groupService;

    @Mock
    private CancellationResponseProducer responseProducer;

    @InjectMocks
    private CancellationRequestListener listener;

    @Test
    void shouldRespondSuccessWhenSlotReleased() {
        final CancellationRequest req = newRequest();

        listener.onRequest(req);

        verify(groupService).releaseSlot(req.groupRefId(), req.correlationId());
        final CancellationResponse sent = capture();
        assertThat(sent.status()).isEqualTo(CancellationResponse.Status.SUCCESS);
        assertThat(sent.message()).isNull();
        assertThat(sent.correlationId()).isEqualTo(req.correlationId());
        assertThat(sent.inquiryId()).isEqualTo(req.inquiryId());
        assertThat(sent.groupRefId()).isEqualTo(req.groupRefId());
    }

    @Test
    void shouldRespondNotFoundWhenGroupMissing() {
        final CancellationRequest req = newRequest();
        doThrow(new EntityNotFoundException("Group not found: " + req.groupRefId()))
                .when(groupService).releaseSlot(req.groupRefId(), req.correlationId());

        listener.onRequest(req);

        final CancellationResponse sent = capture();
        assertThat(sent.status()).isEqualTo(CancellationResponse.Status.NOT_FOUND);
        assertThat(sent.message()).contains(req.groupRefId().toString());
    }

    @Test
    void shouldRespondAlreadyReleasedWhenCountZero() {
        final CancellationRequest req = newRequest();
        doThrow(new IllegalStateException("Count already zero"))
                .when(groupService).releaseSlot(req.groupRefId(), req.correlationId());

        listener.onRequest(req);

        final CancellationResponse sent = capture();
        assertThat(sent.status()).isEqualTo(CancellationResponse.Status.ALREADY_RELEASED);
        assertThat(sent.message()).isEqualTo("Count already zero");
    }

    @Test
    void shouldRespondErrorOnUnexpectedFailure() {
        final CancellationRequest req = newRequest();
        doThrow(new RuntimeException("boom"))
                .when(groupService).releaseSlot(req.groupRefId(), req.correlationId());

        listener.onRequest(req);

        final CancellationResponse sent = capture();
        assertThat(sent.status()).isEqualTo(CancellationResponse.Status.ERROR);
        assertThat(sent.message()).contains("boom");
    }

    private CancellationResponse capture() {
        final ArgumentCaptor<CancellationResponse> captor = ArgumentCaptor.forClass(CancellationResponse.class);
        verify(responseProducer).send(captor.capture());
        return captor.getValue();
    }

    private CancellationRequest newRequest() {
        return new CancellationRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now());
    }
}
