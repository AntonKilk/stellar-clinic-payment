package com.stellar.crm.customerservice.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final UUID resourceId;

    public ResourceNotFoundException(String message, UUID id) {
        super(message);
        this.resourceId = id;
    }
}
