package com.inno.authenticationservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inno.authenticationservice.dto.request.LoginRequestDto;
import com.inno.authenticationservice.dto.request.TokenRefreshRequestDto;
import com.inno.authenticationservice.dto.request.UserRegisterRequestDto;
import com.inno.authenticationservice.dto.response.UserServiceClientResponseDto;
import com.inno.authenticationservice.entity.User;
import com.inno.authenticationservice.entity.UserRole;
import com.inno.authenticationservice.repository.AuthUserRepository;
import com.inno.authenticationservice.service.JwtService;
import com.inno.authenticationservice.util.UserTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class AuthIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private AuthUserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtService jwtService;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void whenRegisterUser_thenSaveToPostgresAndCallUserService() throws Exception {
    // given
    UserRegisterRequestDto registerDto = UserTestData.buildRegisterRequestDto();
    UserServiceClientResponseDto userServiceResponse =
        UserTestData.buildUserServiceClientResponseDto();

    String mockResponseBody = objectMapper.writeValueAsString(userServiceResponse);

    wireMockServer.stubFor(
        post(urlMatching(UserTestData.USER_SERVICE_USERS_REGEX_PATH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(mockResponseBody)));

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.REGISTER_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").isString())
        .andExpect(jsonPath("$.refreshToken").isString())
        .andExpect(jsonPath("$.user.id").value(UserTestData.DEFAULT_ID))
        .andExpect(jsonPath("$.user.email").value(registerDto.email()))
        .andExpect(jsonPath("$.user.name").value(registerDto.name()))
        .andExpect(jsonPath("$.user.surname").value(registerDto.surname()));

    // then
    var savedUser = userRepository.findByEmail(registerDto.email());
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get().getActive()).isTrue();

    // then
    Long savedUserId = savedUser.get().getId();
    wireMockServer.verify(
        postRequestedFor(urlMatching(UserTestData.buildUserServiceUserPath(savedUserId))));
  }

  @Test
  void whenLoginWithValidCredentials_thenReturnTokens() throws Exception {
    // given
    User user = UserTestData.buildDefaultUser();
    user.setPassword(passwordEncoder.encode(UserTestData.DEFAULT_PASSWORD));
    userRepository.save(user);

    LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.LOGIN_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isString())
        .andExpect(jsonPath("$.refreshToken").isString());
  }

  @Test
  void whenLoginWithNonExistentEmail_thenReturnNotFound() throws Exception {
    // given
    LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.LOGIN_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenLoginWithDisabledUser_thenReturnForbidden() throws Exception {
    // given
    User disabledUser = UserTestData.buildDefaultUser();
    disabledUser.setPassword(passwordEncoder.encode(UserTestData.DEFAULT_PASSWORD));
    disabledUser.setActive(false);
    userRepository.save(disabledUser);

    LoginRequestDto loginDto = UserTestData.buildLoginRequestDto();

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.LOGIN_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenLoginWithWrongPassword_thenReturnNotFound() throws Exception {
    // given
    User user = UserTestData.buildDefaultUser();
    user.setPassword(passwordEncoder.encode("correctPassword123"));
    userRepository.save(user);

    LoginRequestDto loginDto = new LoginRequestDto(UserTestData.DEFAULT_EMAIL, "wrongPassword999");

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.LOGIN_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenRefreshTokenWithValidToken_thenReturnNewAccessToken() throws Exception {
    // given
    User user = UserTestData.buildUserEntity();
    user.setId(null);
    User savedUser = userRepository.save(user);

    String validRefreshToken =
        jwtService.createRefreshToken(savedUser.getId(), savedUser.getRole());
    TokenRefreshRequestDto refreshDto = new TokenRefreshRequestDto(validRefreshToken);

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.REFRESH_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString());
  }

  @Test
  void whenRefreshTokenWithAccessToken_thenReturnInvalidTokenError() throws Exception {
    // given
    User user = UserTestData.buildUserEntity();
    user.setId(null);
    User savedUser = userRepository.save(user);

    String accessToken = jwtService.createAccessToken(savedUser.getId(), savedUser.getRole());
    TokenRefreshRequestDto refreshDto = new TokenRefreshRequestDto(accessToken);

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.REFRESH_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenRefreshTokenForNonExistentUser_thenReturnNotFound() throws Exception {
    // given
    Long nonExistentUserId = 999L;
    String refreshToken = jwtService.createRefreshToken(nonExistentUserId, UserRole.USER);
    TokenRefreshRequestDto refreshDto = new TokenRefreshRequestDto(refreshToken);

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.REFRESH_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenRefreshTokenForDisabledUser_thenReturnForbidden() throws Exception {
    // given
    User inactiveUser = UserTestData.buildInactiveUserEntity();
    inactiveUser.setId(null);
    User savedUser = userRepository.save(inactiveUser);

    String refreshToken = jwtService.createRefreshToken(savedUser.getId(), savedUser.getRole());
    TokenRefreshRequestDto refreshDto = new TokenRefreshRequestDto(refreshToken);

    // when & then
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(UserTestData.REFRESH_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshDto)))
        .andExpect(status().isForbidden());
  }
}
