package com.inno.authenticationservice.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseAppExceptionBuilder {

  public static BaseAppException buildNotFoundException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.NOT_FOUND);
  }

  public static BaseAppException buildConflictException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.CONFLICT);
  }

  public static BaseAppException buildInvalidTokenException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.BAD_REQUEST);
  }

  public static BaseAppException buildForbiddenException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.FORBIDDEN);
  }

  public static BaseAppException buildUnauthorizedException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.UNAUTHORIZED);
  }
}
