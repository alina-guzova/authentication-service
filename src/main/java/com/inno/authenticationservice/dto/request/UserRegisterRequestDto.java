package com.inno.authenticationservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.inno.authenticationservice.dto.validation.ValidEmail;
import com.inno.authenticationservice.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record UserRegisterRequestDto(
    @ValidEmail
        @NotBlank(message = "Field 'email' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'email' = 255")
        String email,
    @NotBlank(message = "Field 'password' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'password' = 255")
        String password,
    @NotNull(message = "Field 'role' is required") UserRole role,
    @NotBlank(message = "Field 'name' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'name' = 255")
        String name,
    @NotBlank(message = "Field 'surname' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'surname' = 255")
        String surname,
    @NotNull(message = "Field 'birthDate' is required") @Past @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate birthDate) {}
