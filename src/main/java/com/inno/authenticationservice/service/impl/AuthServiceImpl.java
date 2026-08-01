package com.inno.authenticationservice.service.impl;

import static com.inno.authenticationservice.exception.BaseAppExceptionBuilder.buildConflictException;
import static com.inno.authenticationservice.exception.BaseAppExceptionBuilder.buildForbiddenException;
import static com.inno.authenticationservice.exception.BaseAppExceptionBuilder.buildInvalidTokenException;
import static com.inno.authenticationservice.exception.BaseAppExceptionBuilder.buildNotFoundException;
import static com.inno.authenticationservice.exception.ExceptionMessages.INVALID_CREDENTIALS;
import static com.inno.authenticationservice.exception.ExceptionMessages.TOKEN_NOT_REFRESH;
import static com.inno.authenticationservice.exception.ExceptionMessages.TOKEN_NOT_VALID;
import static com.inno.authenticationservice.exception.ExceptionMessages.USER_ALREADY_EXISTS_WITH_EMAIL;
import static com.inno.authenticationservice.exception.ExceptionMessages.USER_IS_DISABLED;
import static com.inno.authenticationservice.exception.ExceptionMessages.USER_NOT_FOUND;

import com.inno.authenticationservice.dto.request.LoginRequestDto;
import com.inno.authenticationservice.dto.request.TokenRefreshRequestDto;
import com.inno.authenticationservice.dto.request.TokenValidateRequestDto;
import com.inno.authenticationservice.dto.request.UserRegisterRequestDto;
import com.inno.authenticationservice.dto.response.LoginResponseDto;
import com.inno.authenticationservice.dto.response.TokenRefreshResponseDto;
import com.inno.authenticationservice.dto.response.TokenValidateResponseDto;
import com.inno.authenticationservice.dto.response.UserRegisterResponseDto;
import com.inno.authenticationservice.dto.response.UserServiceClientResponseDto;
import com.inno.authenticationservice.entity.User;
import com.inno.authenticationservice.integration.UserServiceClient;
import com.inno.authenticationservice.mapper.UserMapper;
import com.inno.authenticationservice.repository.AuthUserRepository;
import com.inno.authenticationservice.service.AuthService;
import com.inno.authenticationservice.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

  private final AuthUserRepository userRepository;
  private final UserMapper userMapper;
  private final UserServiceClient userServiceClient;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  @Transactional
  public UserRegisterResponseDto create(UserRegisterRequestDto dto) {

    if (userRepository.existsByEmail(dto.email())) {
      log.warn("Registration failed: email already exists | email={}", dto.email());
      throw buildConflictException(USER_ALREADY_EXISTS_WITH_EMAIL, dto.email());
    }

    User user = userMapper.toEntity(dto);
    user.setPassword(passwordEncoder.encode(dto.password()));

    User savedUser = userRepository.save(user);

    UserServiceClientResponseDto responseDto =
        userServiceClient.createUser(savedUser.getId(), userMapper.toUserClientCreateDto(dto));
    String accessToken = jwtService.createAccessToken(savedUser.getId(), savedUser.getRole());
    String refreshToken = jwtService.createRefreshToken(savedUser.getId(), savedUser.getRole());

    log.info("User registered successfully | userId={}, email={}", savedUser.getId(), dto.email());

    return userMapper.toUserRegisterResponseDto(responseDto, accessToken, refreshToken);
  }

  @Override
  @Transactional
  public LoginResponseDto login(LoginRequestDto dto) {

    User user =
        userRepository
            .findByEmail(dto.email())
            .orElseThrow(
                () -> {
                  log.warn("Login failed: user not found | email={}", dto.email());
                  return buildNotFoundException(INVALID_CREDENTIALS);
                });

    if (!user.getActive()) {
      throw buildForbiddenException(USER_IS_DISABLED);
    }

    if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
      log.warn(
          "Login failed: user account is disabled | userId={}, email={}",
          user.getId(),
          dto.email());
      throw buildNotFoundException(INVALID_CREDENTIALS);
    }

    String accessToken = jwtService.createAccessToken(user.getId(), user.getRole());
    String refreshToken = jwtService.createRefreshToken(user.getId(), user.getRole());

    log.info("User logged in successfully | userId={}", user.getId());

    return LoginResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @Override
  public TokenValidateResponseDto validateToken(TokenValidateRequestDto request) {
    Claims claims = jwtService.isValid(request.token());

    if (claims != null) {
      log.info("Token validation succeeded | userId={}", claims.getSubject());
      return TokenValidateResponseDto.builder()
          .isValid(true)
          .userId(Long.valueOf(claims.getSubject()))
          .role(claims.get("role", String.class))
          .tokenType(claims.get("type", String.class))
          .build();
    }

    return TokenValidateResponseDto.builder().isValid(false).build();
  }

  @Override
  public TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto request) {
    Claims claims = jwtService.isValid(request.refreshToken());

    if (claims == null) {
      log.warn("Token refresh failed: invalid or expired refresh token");
      throw buildInvalidTokenException(TOKEN_NOT_VALID);
    }

    String tokenType = claims.get("type", String.class);
    if (!"REFRESH".equals(tokenType)) {
      log.warn("Token refresh failed: token is not a refresh token | type={}", tokenType);
      throw buildInvalidTokenException(TOKEN_NOT_REFRESH);
    }

    Long userId = Long.valueOf(claims.getSubject());

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("Token refresh failed: user not found | userId={}", userId);
                  return buildNotFoundException(USER_NOT_FOUND);
                });

    if (!user.getActive()) {
      throw buildForbiddenException(USER_IS_DISABLED);
    }

    String newAccessToken = jwtService.createAccessToken(user.getId(), user.getRole());
    log.info("Token refreshed successfully | userId={}", userId);

    return TokenRefreshResponseDto.builder().token(newAccessToken).build();
  }
}
