package com.mostafa.nexus_bank.exception;

public class InvalidOtpException extends BusinessException {

    public InvalidOtpException(String message) {
        super(message);
    }

    public InvalidOtpException() {
        super("The provided OTP code is invalid or expired");
    }
}
