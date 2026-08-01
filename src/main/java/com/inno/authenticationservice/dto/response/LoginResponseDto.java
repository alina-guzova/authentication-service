package com.inno.authenticationservice.dto.response;

import lombok.Builder;

@Builder
public record LoginResponseDto(String accessToken, String refreshToken) {}
