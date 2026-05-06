package com.stellar.crm.inventoryservice.kafka;

import com.stellar.crm.inventoryservice.kafka.dto.CancellationRequest;
import com.stellar.crm.inventoryservice.kafka.dto.CancellationResponse;
import com.stellar.crm.inventoryservice.kafka.dto.CancellationResponse.Status;
import com.stellar.crm.inventoryservice.service.GroupService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancellationRequestListener {

    private final GroupService groupService;
    private final CancellationResponseProducer responseProducer;

    @KafkaListener(topics = "${stellar.kafka.topic.cancellation-request}")
    public void onRequest(CancellationRequest request) {
        Status status;
        String message = null;
        try {
            groupService.releaseSlot(request.groupRefId(), request.correlationId());
            status = Status.SUCCESS;
        } catch (EntityNotFoundException ex) {
            status = Status.NOT_FOUND;
            message = ex.getMessage();
        } catch (IllegalStateException ex) {
            status = Status.ALREADY_RELEASED;
            message = ex.getMessage();
        } catch (RuntimeException ex) {
            log.error("Unexpected failure releasing slot for inquiry {} (group {})",
                    request.inquiryId(), request.groupRefId(), ex);
            status = Status.ERROR;
            message = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        responseProducer.send(new CancellationResponse(
                request.correlationId(),
                request.inquiryId(),
                request.groupRefId(),
                status,
                message
        ));
    }
}
