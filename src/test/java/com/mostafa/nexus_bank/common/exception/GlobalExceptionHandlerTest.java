package com.mostafa.nexus_bank.common.exception;

import com.mostafa.nexus_bank.common.response.ApiError;
import com.mostafa.nexus_bank.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    @DisplayName("Handle Validation Exception")
    void handleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        FieldError fieldError = new FieldError("request", "email", "Email is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(bindingResult.getGlobalErrors()).thenReturn(java.util.List.of());

        ResponseEntity<ApiError> response = handler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).containsKey("email");
    }

    @Test
    @DisplayName("Handle Entity Not Found Exception")
    void handleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("User", "id", "123");

        ResponseEntity<ApiError> response = handler.handleEntityNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Handle Duplicate Resource Exception")
    void handleDuplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("User", "email", "test@test.com");

        ResponseEntity<ApiError> response = handler.handleDuplicateResourceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Handle Insufficient Balance Exception")
    void handleInsufficientBalance() {
        InsufficientBalanceException ex = new InsufficientBalanceException("123456", new java.math.BigDecimal("1000"), new java.math.BigDecimal("500"));

        ResponseEntity<ApiError> response = handler.handleInsufficientBalanceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Handle Transfer Limit Exceeded Exception")
    void handleTransferLimitExceeded() {
        TransferLimitExceededException ex = new TransferLimitExceededException("123456", new java.math.BigDecimal("10000"), new java.math.BigDecimal("15000"));

        ResponseEntity<ApiError> response = handler.handleTransferLimitExceededException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Handle Invalid Token Exception")
    void handleInvalidToken() {
        InvalidTokenException ex = new InvalidTokenException("Token expired");

        ResponseEntity<ApiError> response = handler.handleInvalidTokenException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Handle Invalid OTP Exception")
    void handleInvalidOtp() {
        InvalidOtpException ex = new InvalidOtpException("OTP expired");

        ResponseEntity<ApiError> response = handler.handleInvalidOtpException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Handle Access Denied Exception")
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ApiError> response = handler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Handle Bad Credentials Exception")
    void handleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ApiError> response = handler.handleAuthenticationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
    }

    @Test
    @DisplayName("Handle Generic Exception")
    void handleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ApiError> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("unexpected error");
    }

    @Test
    @DisplayName("Handle Business Exception")
    void handleBusinessException() {
        BusinessException ex = new BusinessException("Cannot transfer to same account");

        ResponseEntity<ApiError> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Handle No Resource Found Exception")
    void handleNoResourceFound() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<ApiError> response = handler.handleNoResourceFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
