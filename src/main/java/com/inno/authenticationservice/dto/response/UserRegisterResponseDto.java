package com.inno.authenticationservice.dto.response;

import lombok.Builder;

@Builder
public record UserRegisterResponseDto(
    UserServiceClientResponseDto user, String accessToken, String refreshToken) {}
