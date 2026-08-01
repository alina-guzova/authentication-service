package com.inno.authenticationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TokenRefreshRequestDto(
    @NotBlank(message = "Field 'refreshToken' is required and must not be empty")
        String refreshToken) {}
