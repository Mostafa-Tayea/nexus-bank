package com.mostafa.nexus_bank.exception;

public class EmailSendingException extends BusinessException {

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
