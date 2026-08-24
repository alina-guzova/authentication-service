package com.inno.authenticationservice.util;

import com.inno.authenticationservice.dto.request.LoginRequestDto;
import com.inno.authenticationservice.dto.request.TokenRefreshRequestDto;
import com.inno.authenticationservice.dto.request.TokenValidateRequestDto;
import com.inno.authenticationservice.dto.request.UserClientCreateRequestDto;
import com.inno.authenticationservice.dto.request.UserRegisterRequestDto;
import com.inno.authenticationservice.dto.response.LoginResponseDto;
import com.inno.authenticationservice.dto.response.TokenRefreshResponseDto;
import com.inno.authenticationservice.dto.response.TokenValidateResponseDto;
import com.inno.authenticationservice.dto.response.UserRegisterResponseDto;
import com.inno.authenticationservice.dto.response.UserServiceClientResponseDto;
import com.inno.authenticationservice.entity.User;
import com.inno.authenticationservice.entity.UserRole;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class UserTestData {
  // --- API Paths ---
  public static final String AUTH_API_PATH = "/api/v1/auth";
  public static final String LOGIN_API_PATH = "/api/v1/auth/login";
  public static final String REGISTER_API_PATH = "/api/v1/auth/register";
  public static final String REFRESH_API_PATH = "/api/v1/auth/refresh";
  public static final String VALIDATE_API_PATH = "/api/v1/auth/validate";

  // --- Identifiers & Credentials ---
  public static final Long DEFAULT_ID = 1L;
  public static final Long NON_EXISTENT_USER_ID = 99999L;
  public static final String DEFAULT_EMAIL = "alice@gmail.com";
  public static final String EXISTING_EMAIL = "existing.alice@gmail.com";
  public static final String DEFAULT_PASSWORD = "password123";
  public static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHashValue123456789";
  public static final UserRole DEFAULT_ROLE = UserRole.USER;
  public static final String USER_SERVICE_USERS_API_PATH = "/api/v1/users";
  public static final String USER_SERVICE_USERS_REGEX_PATH = "/api/v1/users/\\d+";

  // --- Tokens ---
  public static final String VALID_ACCESS_TOKEN = "valid.access.jwt.token";
  public static final String VALID_REFRESH_TOKEN = "valid.refresh.jwt.token";
  public static final String INVALID_TOKEN = "invalid.jwt.token";

  // --- Personal Data ---
  public static final String DEFAULT_NAME = "Alice";
  public static final String DEFAULT_SURNAME = "Britt";
  public static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2000, 1, 23);

  private UserTestData() {}

  public static User buildUserEntity() {
    return buildUserEntity(DEFAULT_ID, DEFAULT_EMAIL, true);
  }

  public static User buildUserEntity(Long id, String email, Boolean active) {
    return User.builder()
        .id(id)
        .email(email)
        .password(ENCODED_PASSWORD)
        .role(DEFAULT_ROLE)
        .active(active)
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  public static User buildUnsavedUserEntity() {
    return buildUserEntity(null, DEFAULT_EMAIL, true);
  }

  public static User buildInactiveUserEntity() {
    return buildUserEntity(DEFAULT_ID, DEFAULT_EMAIL, false);
  }

  public static UserRegisterRequestDto buildRegisterRequestDto() {
    return UserRegisterRequestDto.builder()
        .email(DEFAULT_EMAIL)
        .password(DEFAULT_PASSWORD)
        .role(DEFAULT_ROLE)
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .build();
  }

  public static UserClientCreateRequestDto buildUserClientCreateRequestDto() {
    return UserClientCreateRequestDto.builder()
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .email(DEFAULT_EMAIL)
        .build();
  }

  public static UserRegisterResponseDto buildUserRegisterResponseDto() {
    return UserRegisterResponseDto.builder()
        .user(buildUserServiceClientResponseDto())
        .accessToken(VALID_ACCESS_TOKEN)
        .refreshToken(VALID_REFRESH_TOKEN)
        .build();
  }

  public static LoginResponseDto buildLoginResponseDto() {
    return LoginResponseDto.builder()
        .accessToken(VALID_ACCESS_TOKEN)
        .refreshToken(VALID_REFRESH_TOKEN)
        .build();
  }

  public static TokenRefreshRequestDto buildTokenRefreshRequestDto() {
    return TokenRefreshRequestDto.builder().refreshToken(VALID_REFRESH_TOKEN).build();
  }

  public static TokenRefreshResponseDto buildTokenRefreshResponseDto() {
    return TokenRefreshResponseDto.builder().token(VALID_ACCESS_TOKEN).build();
  }

  public static TokenValidateRequestDto buildTokenValidateRequestDto() {
    return TokenValidateRequestDto.builder().token(VALID_ACCESS_TOKEN).build();
  }

  public static TokenValidateResponseDto buildTokenValidateResponseDto(boolean isValid) {
    if (!isValid) {
      return TokenValidateResponseDto.builder().isValid(false).build();
    }
    return TokenValidateResponseDto.builder()
        .isValid(true)
        .userId(DEFAULT_ID)
        .role(DEFAULT_ROLE.name())
        .tokenType("ACCESS")
        .build();
  }

  public static UserServiceClientResponseDto buildUserServiceClientResponseDto() {
    return UserServiceClientResponseDto.builder()
        .id(DEFAULT_ID)
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .email(DEFAULT_EMAIL)
        .active(true)
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  public static String buildUserServiceUserPath(Long userId) {
    return USER_SERVICE_USERS_API_PATH + "/" + userId;
  }

  public static LoginRequestDto buildLoginRequestDto() {
    return new LoginRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
  }

  public static User buildDefaultUser() {
    return User.builder()
        .email(DEFAULT_EMAIL)
        .password(DEFAULT_PASSWORD)
        .role(UserRole.USER)
        .active(true)
        .build();
  }
}
