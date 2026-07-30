package com.inno.authenticationservice.dto.request;

import com.inno.authenticationservice.dto.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record LoginRequestDto(
    @NotBlank(message = "Field 'email' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'email' = 255")
        @ValidEmail
        String email,
    @NotBlank(message = "Field 'password' is required and must not be empty")
        @Size(
            max = 255,
            message = "Length of field 'password' must be between 6 and 255 characters")
        String password) {}
