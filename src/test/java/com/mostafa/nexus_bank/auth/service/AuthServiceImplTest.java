package com.mostafa.nexus_bank.auth.service;

import com.mostafa.nexus_bank.auth.dto.request.LoginRequest;
import com.mostafa.nexus_bank.auth.dto.request.RegisterRequest;
import com.mostafa.nexus_bank.auth.dto.response.AuthenticationResponse;
import com.mostafa.nexus_bank.auth.dto.response.MessageResponse;
import com.mostafa.nexus_bank.auth.entity.RefreshToken;
import com.mostafa.nexus_bank.auth.entity.VerificationToken;
import com.mostafa.nexus_bank.auth.repository.OtpCodeRepository;
import com.mostafa.nexus_bank.auth.repository.RefreshTokenRepository;
import com.mostafa.nexus_bank.auth.repository.VerificationTokenRepository;
import com.mostafa.nexus_bank.cache.service.JwtBlacklistService;
import com.mostafa.nexus_bank.cache.service.LoginAttemptService;
import com.mostafa.nexus_bank.common.enums.OtpPurpose;
import com.mostafa.nexus_bank.common.enums.RoleType;
import com.mostafa.nexus_bank.exception.DuplicateResourceException;
import com.mostafa.nexus_bank.exception.InvalidOtpException;
import com.mostafa.nexus_bank.exception.InvalidTokenException;
import com.mostafa.nexus_bank.exception.ValidationException;
import com.mostafa.nexus_bank.role.entity.Role;
import com.mostafa.nexus_bank.role.repository.RoleRepository;
import com.mostafa.nexus_bank.security.config.AuthProperties;
import com.mostafa.nexus_bank.security.jwt.JwtService;
import com.mostafa.nexus_bank.security.service.CustomUserDetails;
import com.mostafa.nexus_bank.security.service.RefreshTokenService;
import com.mostafa.nexus_bank.user.entity.User;
import com.mostafa.nexus_bank.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private OtpCodeRepository otpCodeRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthProperties authProperties;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private JwtBlacklistService jwtBlacklistService;
    @Mock private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .name(RoleType.ROLE_CUSTOMER)
                .description("Customer role")
                .build();
        customerRole.setId(UUID.randomUUID());

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("01234567890")
                .nationalId("12345678901234")
                .password("encodedPassword")
                .enabled(true)
                .accountNonLocked(true)
                .failedAttempts(0)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();
        testUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "john@example.com",
                "01234567890", "12345678901234",
                "P@ssw0rd123", "P@ssw0rd123"
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("01234567890")).thenReturn(false);
        when(userRepository.existsByNationalId("12345678901234")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("P@ssw0rd123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = authService.register(request);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(2)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Register - Duplicate Email")
    void register_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "john@example.com",
                "01234567890", "12345678901234",
                "P@ssw0rd123", "P@ssw0rd123"
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Register - Password Mismatch")
    void register_PasswordMismatch() {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "john@example.com",
                "01234567890", "12345678901234",
                "P@ssw0rd123", "DifferentPassword"
        );

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "P@ssw0rd123");

        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                        .authenticated(userDetails, null, userDetails.getAuthorities()));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        AuthenticationResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(refreshTokenService).createRefreshToken(any(), eq("refresh-token"), eq(604800000L));
    }

    @Test
    @DisplayName("Login - Disabled User")
    void login_DisabledUser() {
        testUser.setEnabled(false);
        LoginRequest request = new LoginRequest("john@example.com", "P@ssw0rd123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    @DisplayName("Login - Locked User")
    void login_LockedUser() {
        testUser.setAccountNonLocked(false);
        LoginRequest request = new LoginRequest("john@example.com", "P@ssw0rd123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LockedException.class);
    }

    @Test
    @DisplayName("Login - Invalid Credentials")
    void login_InvalidCredentials() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository).save(argThat(user -> user.getFailedAttempts() == 1));
    }

    @Test
    @DisplayName("Login - Account Locks After Max Failed Attempts")
    void login_AccountLocksAfterMaxAttempts() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");
        testUser.setFailedAttempts(4);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(authProperties.getMaxFailedAttempts()).thenReturn(5);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository).save(argThat(user -> !user.isAccountNonLocked()));
    }

    @Test
    @DisplayName("Verify Email - Success")
    void verifyEmail_Success() {
        VerificationToken token = VerificationToken.builder()
                .token("valid-token")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .verified(false)
                .user(testUser)
                .build();
        token.setId(UUID.randomUUID());

        when(verificationTokenRepository.findByTokenAndVerifiedFalse("valid-token"))
                .thenReturn(Optional.of(token));
        when(userRepository.save(any())).thenReturn(testUser);

        MessageResponse response = authService.verifyEmail(
                new com.mostafa.nexus_bank.auth.dto.request.VerifyEmailRequest("valid-token"));

        assertThat(response).isNotNull();
        assertThat(testUser.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Verify Email - Expired Token")
    void verifyEmail_ExpiredToken() {
        VerificationToken token = VerificationToken.builder()
                .token("expired-token")
                .expiryDate(LocalDateTime.now().minusHours(1))
                .verified(false)
                .user(testUser)
                .build();
        token.setId(UUID.randomUUID());

        when(verificationTokenRepository.findByTokenAndVerifiedFalse("expired-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail(
                new com.mostafa.nexus_bank.auth.dto.request.VerifyEmailRequest("expired-token")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Verify Email - Already Used Token")
    void verifyEmail_AlreadyUsedToken() {
        when(verificationTokenRepository.findByTokenAndVerifiedFalse("used-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail(
                new com.mostafa.nexus_bank.auth.dto.request.VerifyEmailRequest("used-token")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Verify OTP - Success")
    void verifyOtp_Success() {
        com.mostafa.nexus_bank.auth.entity.OtpCode otpCode =
                com.mostafa.nexus_bank.auth.entity.OtpCode.builder()
                        .code("123456")
                        .purpose(OtpPurpose.EMAIL_VERIFICATION)
                        .expiryTime(LocalDateTime.now().plusMinutes(5))
                        .verified(false)
                        .user(testUser)
                        .build();
        otpCode.setId(UUID.randomUUID());

        when(otpCodeRepository.findByCodeAndPurposeAndVerifiedFalse("123456", OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(otpCode));

        MessageResponse response = authService.verifyOtp(
                new com.mostafa.nexus_bank.auth.dto.request.VerifyOtpRequest("123456", OtpPurpose.EMAIL_VERIFICATION));

        assertThat(response).isNotNull();
        assertThat(otpCode.isVerified()).isTrue();
    }

    @Test
    @DisplayName("Verify OTP - Expired")
    void verifyOtp_Expired() {
        com.mostafa.nexus_bank.auth.entity.OtpCode otpCode =
                com.mostafa.nexus_bank.auth.entity.OtpCode.builder()
                        .code("123456")
                        .purpose(OtpPurpose.EMAIL_VERIFICATION)
                        .expiryTime(LocalDateTime.now().minusMinutes(1))
                        .verified(false)
                        .user(testUser)
                        .build();
        otpCode.setId(UUID.randomUUID());

        when(otpCodeRepository.findByCodeAndPurposeAndVerifiedFalse("123456", OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(otpCode));

        assertThatThrownBy(() -> authService.verifyOtp(
                new com.mostafa.nexus_bank.auth.dto.request.VerifyOtpRequest("123456", OtpPurpose.EMAIL_VERIFICATION)))
                .isInstanceOf(InvalidOtpException.class);
    }

    @Test
    @DisplayName("Verify OTP - Already Used")
    void verifyOtp_AlreadyUsed() {
        when(otpCodeRepository.findByCodeAndPurposeAndVerifiedFalse("123456", OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyOtp(
                new com.mostafa.nexus_bank.auth.dto.request.VerifyOtpRequest("123456", OtpPurpose.EMAIL_VERIFICATION)))
                .isInstanceOf(InvalidOtpException.class);
    }

    @Test
    @DisplayName("Forgot Password - Email Exists")
    void forgotPassword_EmailExists() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        MessageResponse response = authService.forgotPassword(
                new com.mostafa.nexus_bank.auth.dto.request.ForgotPasswordRequest("john@example.com"));

        assertThat(response).isNotNull();
        verify(otpCodeRepository, atLeastOnce()).deleteByUserIdAndPurposeAndVerifiedFalse(testUser.getId(), OtpPurpose.RESET_PASSWORD);
        verify(eventPublisher, atLeastOnce()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Forgot Password - Email Does Not Exist")
    void forgotPassword_EmailDoesNotExist() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        MessageResponse response = authService.forgotPassword(
                new com.mostafa.nexus_bank.auth.dto.request.ForgotPasswordRequest("nonexistent@example.com"));

        assertThat(response).isNotNull();
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Reset Password - Success")
    void resetPassword_Success() {
        com.mostafa.nexus_bank.auth.entity.OtpCode otpCode =
                com.mostafa.nexus_bank.auth.entity.OtpCode.builder()
                        .code("reset-code")
                        .purpose(OtpPurpose.RESET_PASSWORD)
                        .expiryTime(LocalDateTime.now().plusMinutes(10))
                        .verified(false)
                        .user(testUser)
                        .build();
        otpCode.setId(UUID.randomUUID());

        when(otpCodeRepository.findByCodeAndPurposeAndVerifiedFalse("reset-code", OtpPurpose.RESET_PASSWORD))
                .thenReturn(Optional.of(otpCode));
        when(passwordEncoder.encode("NewP@ssw0rd1")).thenReturn("newEncodedPassword");

        MessageResponse response = authService.resetPassword(
                new com.mostafa.nexus_bank.auth.dto.request.ResetPasswordRequest(
                        "reset-code", "NewP@ssw0rd1", "NewP@ssw0rd1"));

        assertThat(response).isNotNull();
        assertThat(testUser.getPassword()).isEqualTo("newEncodedPassword");
        verify(refreshTokenService).revokeAllUserRefreshTokens(testUser);
    }

    @Test
    @DisplayName("Reset Password - Password Mismatch")
    void resetPassword_PasswordMismatch() {
        assertThatThrownBy(() -> authService.resetPassword(
                new com.mostafa.nexus_bank.auth.dto.request.ResetPasswordRequest(
                        "token", "NewP@ssw0rd1", "DifferentPassword")))
                .isInstanceOf(ValidationException.class);
    }
}
