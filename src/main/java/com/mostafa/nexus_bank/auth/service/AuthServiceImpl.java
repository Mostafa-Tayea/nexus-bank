package com.mostafa.nexus_bank.auth.service;

import com.mostafa.nexus_bank.auth.dto.request.ForgotPasswordRequest;
import com.mostafa.nexus_bank.auth.dto.request.LoginRequest;
import com.mostafa.nexus_bank.auth.dto.request.LogoutRequest;
import com.mostafa.nexus_bank.auth.dto.request.RefreshTokenRequest;
import com.mostafa.nexus_bank.auth.dto.request.RegisterRequest;
import com.mostafa.nexus_bank.auth.dto.request.ResetPasswordRequest;
import com.mostafa.nexus_bank.auth.dto.request.VerifyEmailRequest;
import com.mostafa.nexus_bank.auth.dto.request.VerifyOtpRequest;
import com.mostafa.nexus_bank.auth.dto.response.AuthenticationResponse;
import com.mostafa.nexus_bank.auth.dto.response.MessageResponse;
import com.mostafa.nexus_bank.auth.dto.response.RefreshTokenResponse;
import com.mostafa.nexus_bank.auth.entity.OtpCode;
import com.mostafa.nexus_bank.auth.entity.RefreshToken;
import com.mostafa.nexus_bank.auth.entity.VerificationToken;
import com.mostafa.nexus_bank.auth.repository.OtpCodeRepository;
import com.mostafa.nexus_bank.auth.repository.RefreshTokenRepository;
import com.mostafa.nexus_bank.auth.repository.VerificationTokenRepository;
import com.mostafa.nexus_bank.cache.service.JwtBlacklistService;
import com.mostafa.nexus_bank.cache.service.LoginAttemptService;
import com.mostafa.nexus_bank.common.enums.OtpPurpose;
import com.mostafa.nexus_bank.common.enums.RoleType;
import com.mostafa.nexus_bank.common.event.EmailVerifiedEvent;
import com.mostafa.nexus_bank.common.event.OtpGeneratedEvent;
import com.mostafa.nexus_bank.common.event.PasswordResetEvent;
import com.mostafa.nexus_bank.common.event.UserLoggedInEvent;
import com.mostafa.nexus_bank.common.event.UserRegisteredEvent;
import com.mostafa.nexus_bank.exception.DuplicateResourceException;
import com.mostafa.nexus_bank.exception.InvalidOtpException;
import com.mostafa.nexus_bank.exception.InvalidTokenException;
import com.mostafa.nexus_bank.exception.UnauthorizedException;
import com.mostafa.nexus_bank.role.entity.Role;
import com.mostafa.nexus_bank.role.repository.RoleRepository;
import com.mostafa.nexus_bank.security.config.AuthProperties;
import com.mostafa.nexus_bank.security.jwt.JwtService;
import com.mostafa.nexus_bank.security.service.CustomUserDetails;
import com.mostafa.nexus_bank.security.service.RefreshTokenService;
import com.mostafa.nexus_bank.user.dto.response.UserResponse;
import com.mostafa.nexus_bank.user.entity.User;
import com.mostafa.nexus_bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtBlacklistService jwtBlacklistService;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        log.debug("Processing registration for email: {}", request.email());

        validatePasswordMatch(request.password(), request.confirmPassword());

        checkDuplicateEmail(request.email());
        checkDuplicatePhone(request.phone());
        checkDuplicateNationalId(request.nationalId());

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseThrow(() -> new com.mostafa.nexus_bank.exception.EntityNotFoundException("Role", "name", RoleType.ROLE_CUSTOMER));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .nationalId(request.nationalId())
                .password(passwordEncoder.encode(request.password()))
                .enabled(false)
                .accountNonLocked(true)
                .failedAttempts(0)
                .roles(new java.util.HashSet<>(java.util.List.of(customerRole)))
                .build();

        User savedUser = userRepository.save(user);

        String verificationToken = generateVerificationToken(savedUser);
        String otpCode = generateOtpCode(savedUser, OtpPurpose.EMAIL_VERIFICATION);

        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName() + " " + savedUser.getLastName()));
        eventPublisher.publishEvent(new OtpGeneratedEvent(savedUser.getId(), savedUser.getEmail(), otpCode, com.mostafa.nexus_bank.common.enums.NotificationType.EMAIL));

        log.info("User registered successfully: {}", savedUser.getEmail());

        return MessageResponse.of("Registration successful. Please verify your email.", verificationToken);
    }

    @Override
    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        log.debug("Processing login for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isEnabled()) {
            log.warn("Login attempt for disabled account: {}", request.email());
            throw new DisabledException("Account is not verified. Please verify your email.");
        }

        if (!user.isAccountNonLocked()) {
            log.warn("Login attempt for locked account: {}", request.email());
            throw new LockedException("Account is locked due to too many failed attempts");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            resetFailedAttempts(user);
            updateLastLogin(user);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshTokenJwt = jwtService.generateRefreshToken(userDetails);

            refreshTokenService.createRefreshToken(user, refreshTokenJwt, jwtService.getRefreshTokenExpiration());

            eventPublisher.publishEvent(new UserLoggedInEvent(
                    user.getId(), user.getEmail(), null, null, LocalDateTime.now()));

            log.info("User logged in successfully: {}", request.email());

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenJwt)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                    .user(UserResponse.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .enabled(user.isEnabled())
                            .createdAt(user.getCreatedAt())
                            .build())
                    .build();

        } catch (AuthenticationException e) {
            handleFailedLogin(user);
            throw e;
        }
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Processing refresh token request");

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = refreshToken.getUser();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.revokeRefreshToken(request.refreshToken());
        refreshTokenService.createRefreshToken(user, newRefreshToken, jwtService.getRefreshTokenExpiration());

        log.info("Token refreshed for user: {}", user.getEmail());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse logout(LogoutRequest request) {
        log.debug("Processing logout request");

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());
        String email = refreshToken.getUser().getEmail();

        refreshTokenService.revokeRefreshToken(request.refreshToken());

        loginAttemptService.clearFailedAttempts(email);

        log.info("User logged out successfully: {}", email);
        return MessageResponse.of("Logged out successfully");
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        log.debug("Processing email verification");

        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndVerifiedFalse(request.token())
                .orElseThrow(() -> new InvalidTokenException("Verification token is invalid or already used"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setVerified(true);
        verificationToken.setVerifiedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);

        eventPublisher.publishEvent(new EmailVerifiedEvent(user.getId(), user.getEmail()));

        log.info("Email verified successfully for user: {}", user.getEmail());
        return MessageResponse.of("Email verified successfully");
    }

    @Override
    @Transactional
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        log.debug("Processing OTP verification for purpose: {}", request.purpose());

        OtpCode otpCode = otpCodeRepository.findByCodeAndPurposeAndVerifiedFalse(request.code(), request.purpose())
                .orElseThrow(() -> new InvalidOtpException("OTP code is invalid or already used"));

        if (otpCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP code has expired");
        }

        otpCode.setVerified(true);
        otpCode.setVerifiedAt(LocalDateTime.now());
        otpCodeRepository.save(otpCode);

        log.info("OTP verified successfully for purpose: {}", request.purpose());
        return MessageResponse.of("OTP verified successfully");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        log.debug("Processing forgot password request");

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            otpCodeRepository.deleteByUserIdAndPurposeAndVerifiedFalse(user.getId(), OtpPurpose.RESET_PASSWORD);

            String otpCode = generateOtpCode(user, OtpPurpose.RESET_PASSWORD);
            eventPublisher.publishEvent(new OtpGeneratedEvent(user.getId(), user.getEmail(), otpCode, com.mostafa.nexus_bank.common.enums.NotificationType.EMAIL));

            log.info("Password reset OTP sent to user: {}", user.getEmail());
        });

        return MessageResponse.of("If the email exists, a reset code has been sent");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        log.debug("Processing password reset");

        validatePasswordMatch(request.newPassword(), request.confirmPassword());

        OtpCode otpCode = otpCodeRepository.findByCodeAndPurposeAndVerifiedFalse(request.token(), OtpPurpose.RESET_PASSWORD)
                .orElseThrow(() -> new InvalidTokenException("Reset token is invalid or already used"));

        if (otpCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = otpCode.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        otpCode.setVerified(true);
        otpCode.setVerifiedAt(LocalDateTime.now());
        otpCodeRepository.save(otpCode);

        refreshTokenService.revokeAllUserRefreshTokens(user);

        eventPublisher.publishEvent(new PasswordResetEvent(user.getId(), user.getEmail()));

        log.info("Password reset successfully for user: {}", user.getEmail());
        return MessageResponse.of("Password reset successfully");
    }

    private void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new com.mostafa.nexus_bank.exception.ValidationException(
                    java.util.Map.of("confirmPassword", "Passwords do not match"));
        }
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
    }

    private void checkDuplicatePhone(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("User", "phone", phone);
        }
    }

    private void checkDuplicateNationalId(String nationalId) {
        if (userRepository.existsByNationalId(nationalId)) {
            throw new DuplicateResourceException("User", "national ID", nationalId);
        }
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .verified(false)
                .user(user)
                .build();

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    private String generateOtpCode(User user, OtpPurpose purpose) {
        otpCodeRepository.deleteByUserIdAndPurposeAndVerifiedFalse(user.getId(), purpose);

        String code = String.format("%06d", SECURE_RANDOM.nextInt(1000000));

        OtpCode otpCode = OtpCode.builder()
                .code(code)
                .purpose(purpose)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .user(user)
                .build();

        otpCodeRepository.save(otpCode);
        return code;
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= authProperties.getMaxFailedAttempts()) {
            user.setAccountNonLocked(false);
            log.warn("Account locked for user: {} after {} failed attempts", user.getEmail(), attempts);
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            userRepository.save(user);
        }
    }

    private void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}
