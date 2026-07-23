package com.mostafa.nexus_bank.auth.dto.request;

import com.mostafa.nexus_bank.common.enums.OtpPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for OTP verification")
public record VerifyOtpRequest(
        @Schema(description = "The OTP code received by the user", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "OTP code is required")
        String code,

        @Schema(description = "The purpose of the OTP (e.g., registration, password reset)", example = "REGISTRATION", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "OTP purpose is required")
        OtpPurpose purpose
) {
}
