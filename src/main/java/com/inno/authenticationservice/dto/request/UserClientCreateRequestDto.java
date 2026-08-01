package com.inno.authenticationservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record UserClientCreateRequestDto(
    String name,
    String surname,
    @JsonFormat(pattern = "dd.MM.yyyy") LocalDate birthDate,
    String email) {}
