package com.inno.authenticationservice.dto.response;

import lombok.Builder;

@Builder
public record TokenValidateResponseDto(
    boolean isValid, Long userId, String role, String tokenType) {}
