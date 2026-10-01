package com.rizvi.spring.io;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank(message = "OTP is required")
        String otp
) {}