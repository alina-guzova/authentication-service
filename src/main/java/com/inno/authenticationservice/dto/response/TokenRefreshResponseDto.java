package com.inno.authenticationservice.dto.response;

import lombok.Builder;

@Builder
public record TokenRefreshResponseDto(String token) {}
