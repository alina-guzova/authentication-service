package com.inno.authenticationservice.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import com.inno.authenticationservice.exception.BaseAppException;
import com.inno.authenticationservice.integration.UserServiceClient;
import com.inno.authenticationservice.mapper.UserMapper;
import com.inno.authenticationservice.repository.AuthUserRepository;
import com.inno.authenticationservice.service.JwtService;
import com.inno.authenticationservice.service.impl.AuthServiceImpl;
import com.inno.authenticationservice.util.UserTestData;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private AuthUserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private UserServiceClient userServiceClient;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;

  @InjectMocks private AuthServiceImpl authService;

  @Nested
  class CreateTests {

    @Test
    void whenCreateUser_thenUserIsRegisteredAndResponseReturned() {
      // given
      UserRegisterRequestDto registerDto = UserTestData.buildRegisterRequestDto();
      User unsavedUser = UserTestData.buildUnsavedUserEntity();
      User savedUser = UserTestData.buildUserEntity();
      UserClientCreateRequestDto clientCreateDto = UserTestData.buildUserClientCreateRequestDto();
      UserServiceClientResponseDto clientResponseDto =
          UserTestData.buildUserServiceClientResponseDto();
      UserRegisterResponseDto expectedResponse = UserTestData.buildUserRegisterResponseDto();

      when(userRepository.existsByEmail(registerDto.email())).thenReturn(false);
      when(userMapper.toEntity(registerDto)).thenReturn(unsavedUser);
      when(passwordEncoder.encode(registerDto.password()))
          .thenReturn(UserTestData.ENCODED_PASSWORD);
      when(userRepository.save(unsavedUser)).thenReturn(savedUser);
      when(userMapper.toUserClientCreateDto(registerDto)).thenReturn(clientCreateDto);
      when(userServiceClient.createUser(savedUser.getId(), clientCreateDto))
          .thenReturn(clientResponseDto);
      when(jwtService.createAccessToken(savedUser.getId(), savedUser.getRole()))
          .thenReturn(UserTestData.VALID_ACCESS_TOKEN);
      when(jwtService.createRefreshToken(savedUser.getId(), savedUser.getRole()))
          .thenReturn(UserTestData.VALID_REFRESH_TOKEN);
      when(userMapper.toUserRegisterResponseDto(
              clientResponseDto, UserTestData.VALID_ACCESS_TOKEN, UserTestData.VALID_REFRESH_TOKEN))
          .thenReturn(expectedResponse);

      // when
      UserRegisterResponseDto actualResponse = authService.create(registerDto);

      // then
      assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

      verify(userRepository).existsByEmail(registerDto.email());
      verify(userMapper).toEntity(registerDto);
      verify(passwordEncoder).encode(registerDto.password());
      verify(userRepository).save(unsavedUser);
      verify(userMapper).toUserClientCreateDto(registerDto);
      verify(userServiceClient).createUser(savedUser.getId(), clientCreateDto);
      verify(jwtService).createAccessToken(savedUser.getId(), savedUser.getRole());
      verify(jwtService).createRefreshToken(savedUser.getId(), savedUser.getRole());
      verify(userMapper)
          .toUserRegisterResponseDto(
              clientResponseDto, UserTestData.VALID_ACCESS_TOKEN, UserTestData.VALID_REFRESH_TOKEN);
    }

    @Test
    void whenCreateUserWithExistingEmail_thenThrowConflictException() {
      // given
      UserRegisterRequestDto registerDto = UserTestData.buildRegisterRequestDto();

      when(userRepository.existsByEmail(registerDto.email())).thenReturn(true);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> authService.create(registerDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

      verify(userRepository).existsByEmail(registerDto.email());
      verifyNoMoreInteractions(userRepository);
      verifyNoInteractions(userMapper, passwordEncoder, userServiceClient, jwtService);
    }

    @Test
    void whenUserServiceClientFails_thenPropagateException() {
      // given
      UserRegisterRequestDto registerDto = UserTestData.buildRegisterRequestDto();
      User unsavedUser = UserTestData.buildUnsavedUserEntity();
      User savedUser = UserTestData.buildUserEntity();
      UserClientCreateRequestDto clientCreateDto = UserTestData.buildUserClientCreateRequestDto();

      when(userRepository.existsByEmail(registerDto.email())).thenReturn(false);
      when(userMapper.toEntity(registerDto)).thenReturn(unsavedUser);
      when(passwordEncoder.encode(registerDto.password()))
          .thenReturn(UserTestData.ENCODED_PASSWORD);
      when(userRepository.save(unsavedUser)).thenReturn(savedUser);
      when(userMapper.toUserClientCreateDto(registerDto)).thenReturn(clientCreateDto);

      when(userServiceClient.createUser(savedUser.getId(), clientCreateDto))
          .thenThrow(new RuntimeException("User service unavailable"));

      // when & then
      assertThrows(RuntimeException.class, () -> authService.create(registerDto));

      verify(userRepository).existsByEmail(registerDto.email());
      verify(userRepository).save(unsavedUser);
      verify(userServiceClient).createUser(savedUser.getId(), clientCreateDto);
      verifyNoInteractions(jwtService);
    }
  }

  @Nested
  class LoginTests {

    @Test
    void whenLoginWithValidCredentials_thenReturnLoginResponseDto() {
      // given
      LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();
      User user = UserTestData.buildUserEntity();

      when(userRepository.findByEmail(loginDto.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(loginDto.password(), user.getPassword())).thenReturn(true);
      when(jwtService.createAccessToken(user.getId(), user.getRole()))
          .thenReturn(UserTestData.VALID_ACCESS_TOKEN);
      when(jwtService.createRefreshToken(user.getId(), user.getRole()))
          .thenReturn(UserTestData.VALID_REFRESH_TOKEN);

      // when
      LoginResponseDto actualResponse = authService.login(loginDto);

      // then
      assertThat(actualResponse).isNotNull();
      assertThat(actualResponse.accessToken()).isEqualTo(UserTestData.VALID_ACCESS_TOKEN);
      assertThat(actualResponse.refreshToken()).isEqualTo(UserTestData.VALID_REFRESH_TOKEN);

      verify(userRepository).findByEmail(loginDto.email());
      verify(passwordEncoder).matches(loginDto.password(), user.getPassword());
      verify(jwtService).createAccessToken(user.getId(), user.getRole());
      verify(jwtService).createRefreshToken(user.getId(), user.getRole());
    }

    @Test
    void whenLoginWithNonExistentEmail_thenThrowNotFoundException() {
      // given
      LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();

      when(userRepository.findByEmail(loginDto.email())).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> authService.login(loginDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).findByEmail(loginDto.email());
      verifyNoMoreInteractions(userRepository);
      verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void whenLoginWithInactiveUser_thenThrowForbiddenException() {
      // given
      LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();
      User inactiveUser = UserTestData.buildInactiveUserEntity();

      when(userRepository.findByEmail(loginDto.email())).thenReturn(Optional.of(inactiveUser));

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> authService.login(loginDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

      verify(userRepository).findByEmail(loginDto.email());
      verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void whenLoginWithWrongPassword_thenThrowNotFoundException() {
      // given
      LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();
      User user = UserTestData.buildUserEntity();

      when(userRepository.findByEmail(loginDto.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(loginDto.password(), user.getPassword())).thenReturn(false);

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> authService.login(loginDto));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(userRepository).findByEmail(loginDto.email());
      verify(passwordEncoder).matches(loginDto.password(), user.getPassword());
      verifyNoInteractions(jwtService);
    }
  }

  @Nested
  class ValidateTokenTests {

    @Test
    void whenValidateToken_withValidToken_thenReturnSuccessResponse() {
      // given
      TokenValidateRequestDto request = UserTestData.buildTokenValidateRequestDto();
      Claims claims = mock(Claims.class);

      when(claims.getSubject()).thenReturn(UserTestData.DEFAULT_ID.toString());
      when(claims.get("role", String.class)).thenReturn(UserTestData.DEFAULT_ROLE.name());
      when(claims.get("type", String.class)).thenReturn("ACCESS");
      when(jwtService.isValid(request.token())).thenReturn(claims);

      // when
      TokenValidateResponseDto actualResponse = authService.validateToken(request);

      // then
      assertThat(actualResponse).isNotNull();
      assertThat(actualResponse.isValid()).isTrue();
      assertThat(actualResponse.userId()).isEqualTo(UserTestData.DEFAULT_ID);
      assertThat(actualResponse.role()).isEqualTo(UserTestData.DEFAULT_ROLE.name());
      assertThat(actualResponse.tokenType()).isEqualTo("ACCESS");

      verify(jwtService).isValid(request.token());
    }

    @Test
    void whenValidateToken_withInvalidToken_thenReturnInvalidResponse() {
      // given
      TokenValidateRequestDto request = UserTestData.buildTokenValidateRequestDto();

      when(jwtService.isValid(request.token())).thenReturn(null);

      // when
      TokenValidateResponseDto actualResponse = authService.validateToken(request);

      // then
      assertThat(actualResponse).isNotNull();
      assertThat(actualResponse.isValid()).isFalse();
      assertThat(actualResponse.userId()).isNull();
      assertThat(actualResponse.role()).isNull();

      verify(jwtService).isValid(request.token());
    }
  }

  @Nested
  class RefreshTokenTests {

    @Test
    void whenRefreshTokenWithValidToken_thenReturnTokenRefreshResponseDto() {
      // given
      TokenRefreshRequestDto request = UserTestData.buildTokenRefreshRequestDto();
      Claims claims = mock(Claims.class);
      User user = UserTestData.buildUserEntity();

      when(jwtService.isValid(request.refreshToken())).thenReturn(claims);
      when(claims.get("type", String.class)).thenReturn("REFRESH");
      when(claims.getSubject()).thenReturn(UserTestData.DEFAULT_ID.toString());
      when(userRepository.findById(UserTestData.DEFAULT_ID)).thenReturn(Optional.of(user));
      when(jwtService.createAccessToken(user.getId(), user.getRole()))
          .thenReturn(UserTestData.VALID_ACCESS_TOKEN);

      // when
      TokenRefreshResponseDto actualResponse = authService.refreshToken(request);

      // then
      assertThat(actualResponse).isNotNull();
      assertThat(actualResponse.token()).isEqualTo(UserTestData.VALID_ACCESS_TOKEN);

      verify(jwtService).isValid(request.refreshToken());
      verify(userRepository).findById(UserTestData.DEFAULT_ID);
      verify(jwtService).createAccessToken(user.getId(), user.getRole());
    }

    @Test
    void whenRefreshTokenIsInvalidOrExpired_thenThrowInvalidTokenException() {
      // given
      TokenRefreshRequestDto request = UserTestData.buildTokenRefreshRequestDto();

      when(jwtService.isValid(request.refreshToken())).thenReturn(null);

      // when & then
      assertThrows(BaseAppException.class, () -> authService.refreshToken(request));

      verify(jwtService).isValid(request.refreshToken());
      verifyNoInteractions(userRepository);
    }

    @Test
    void whenRefreshTokenHasWrongType_thenThrowInvalidTokenException() {
      // given
      TokenRefreshRequestDto request = UserTestData.buildTokenRefreshRequestDto();
      Claims claims = mock(Claims.class);

      when(jwtService.isValid(request.refreshToken())).thenReturn(claims);
      when(claims.get("type", String.class)).thenReturn("ACCESS");

      // when & then
      assertThrows(BaseAppException.class, () -> authService.refreshToken(request));

      verify(jwtService).isValid(request.refreshToken());
      verifyNoInteractions(userRepository);
    }

    @Test
    void whenRefreshTokenUserNotFound_thenThrowNotFoundException() {
      // given
      TokenRefreshRequestDto request = UserTestData.buildTokenRefreshRequestDto();
      Claims claims = mock(Claims.class);

      when(jwtService.isValid(request.refreshToken())).thenReturn(claims);
      when(claims.get("type", String.class)).thenReturn("REFRESH");
      when(claims.getSubject()).thenReturn(UserTestData.DEFAULT_ID.toString());
      when(userRepository.findById(UserTestData.DEFAULT_ID)).thenReturn(Optional.empty());

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> authService.refreshToken(request));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

      verify(jwtService).isValid(request.refreshToken());
      verify(userRepository).findById(UserTestData.DEFAULT_ID);
      verify(jwtService, never()).createAccessToken(any(), any());
    }

    @Test
    void whenRefreshTokenUserIsInactive_thenThrowForbiddenException() {
      // given
      TokenRefreshRequestDto request = UserTestData.buildTokenRefreshRequestDto();
      Claims claims = mock(Claims.class);
      User inactiveUser = UserTestData.buildInactiveUserEntity();

      when(jwtService.isValid(request.refreshToken())).thenReturn(claims);
      when(claims.get("type", String.class)).thenReturn("REFRESH");
      when(claims.getSubject()).thenReturn(UserTestData.DEFAULT_ID.toString());
      when(userRepository.findById(UserTestData.DEFAULT_ID)).thenReturn(Optional.of(inactiveUser));

      // when & then
      BaseAppException exception =
          assertThrows(BaseAppException.class, () -> authService.refreshToken(request));

      assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

      verify(jwtService).isValid(request.refreshToken());
      verify(userRepository).findById(UserTestData.DEFAULT_ID);
      verify(jwtService, never()).createAccessToken(any(), any());
    }
  }
}
