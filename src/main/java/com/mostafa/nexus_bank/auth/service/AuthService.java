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

public interface AuthService {

    MessageResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    MessageResponse logout(LogoutRequest request);

    MessageResponse verifyEmail(VerifyEmailRequest request);

    MessageResponse verifyOtp(VerifyOtpRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}
