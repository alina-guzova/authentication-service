package com.inno.authenticationservice.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessages {

  // User exceptions
  public static final String USER_NOT_FOUND = "Failed to retrieve user";
  public static final String USER_ALREADY_EXISTS_WITH_EMAIL = "User with email %s already exists";
  public static final String USER_IS_DISABLED = "User account is disabled";

  // Credentials exceptions
  public static final String INVALID_CREDENTIALS = "Invalid email or password";

  // Token exceptions
  public static final String TOKEN_NOT_VALID = "Refresh token is invalid or expired";
  public static final String TOKEN_NOT_REFRESH = "Provided token is not a refresh token";
}
