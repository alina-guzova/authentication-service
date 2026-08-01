package com.inno.authenticationservice.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record UserServiceClientResponseDto(
    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
