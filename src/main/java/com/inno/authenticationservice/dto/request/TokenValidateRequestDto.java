package com.inno.authenticationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TokenValidateRequestDto(
    @NotBlank(message = "Field 'token' is required and must not be empty") String token) {}
