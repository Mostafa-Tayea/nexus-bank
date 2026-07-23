package com.mostafa.nexus_bank.exception;

public class ResourceConflictException extends BusinessException {

    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String resourceName, String detail) {
        super(String.format("Conflict with %s: %s", resourceName, detail));
    }
}
