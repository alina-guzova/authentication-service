package com.inno.authenticationservice.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inno.authenticationservice.configuration.JwtProperties;
import com.inno.authenticationservice.entity.UserRole;
import com.inno.authenticationservice.service.impl.JwtServiceImpl;
import com.inno.authenticationservice.util.UserTestData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

  private static final String SECRET_KEY =
      "dGhpc0lzQVNlY3JldEtleUZvckp3dFRlc3RpbmdQdXJwb3Nlc011c3RCZUxvbmc=";
  private static final long ACCESS_EXPIRATION = 3600000L;
  private static final long REFRESH_EXPIRATION = 86400000L;

  @Mock private JwtProperties jwtProperties;

  @InjectMocks private JwtServiceImpl jwtService;

  @BeforeEach
  void setUp() {
    when(jwtProperties.getSecretKey()).thenReturn(SECRET_KEY);
  }

  @Nested
  class CreateAccessTokenTests {

    @Test
    void whenCreateAccessToken_thenValidJwtIsGenerated() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserRole role = UserTestData.DEFAULT_ROLE;

      when(jwtProperties.getAccessTokenExpiration()).thenReturn(ACCESS_EXPIRATION);

      // when
      String token = jwtService.createAccessToken(userId, role);

      // then
      assertThat(token).isNotBlank();

      SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertThat(claims.getSubject()).isEqualTo(userId.toString());
      assertThat(claims.get("role", String.class)).isEqualTo(role.name());
      assertThat(claims.get("type", String.class)).isEqualTo("ACCESS");

      verify(jwtProperties).getAccessTokenExpiration();
      verify(jwtProperties).getSecretKey();
    }
  }

  @Nested
  class CreateRefreshTokenTests {

    @Test
    void whenCreateRefreshToken_thenValidJwtIsGenerated() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserRole role = UserTestData.DEFAULT_ROLE;

      when(jwtProperties.getRefreshTokenExpiration()).thenReturn(REFRESH_EXPIRATION);

      // when
      String token = jwtService.createRefreshToken(userId, role);

      // then
      assertThat(token).isNotBlank();

      SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      assertThat(claims.getSubject()).isEqualTo(userId.toString());
      assertThat(claims.get("type", String.class)).isEqualTo("REFRESH");

      verify(jwtProperties).getRefreshTokenExpiration();
      verify(jwtProperties).getSecretKey();
    }
  }

  @Nested
  class IsValidTests {

    @Test
    void whenValidateValidToken_thenClaimsAreReturned() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserRole role = UserTestData.DEFAULT_ROLE;

      when(jwtProperties.getAccessTokenExpiration()).thenReturn(ACCESS_EXPIRATION);
      String token = jwtService.createAccessToken(userId, role);

      // when
      Claims claims = jwtService.isValid(token);

      // then
      assertThat(claims).isNotNull();
      assertThat(claims.getSubject()).isEqualTo(userId.toString());
      assertThat(claims.get("role", String.class)).isEqualTo(role.name());
    }

    @Test
    void whenValidateInvalidToken_thenReturnNull() {
      // given
      String invalidToken = UserTestData.INVALID_TOKEN;

      // when
      Claims claims = jwtService.isValid(invalidToken);

      // then
      assertThat(claims).isNull();
    }

    @Test
    void whenValidateExpiredToken_thenReturnNull() {
      // given
      Long userId = UserTestData.DEFAULT_ID;
      UserRole role = UserTestData.DEFAULT_ROLE;

      when(jwtProperties.getAccessTokenExpiration()).thenReturn(-1000L);
      String expiredToken = jwtService.createAccessToken(userId, role);

      // when
      Claims claims = jwtService.isValid(expiredToken);

      // then
      assertThat(claims).isNull();
    }
  }
}
