package com.mostafa.nexus_bank.exception;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        super("The provided token is invalid or expired");
    }
}
