package com.mostafa.nexus_bank.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        super("You do not have permission to access this resource");
    }
}
