package com.inno.authenticationservice.service.impl;

import com.inno.authenticationservice.configuration.JwtProperties;
import com.inno.authenticationservice.entity.UserRole;
import com.inno.authenticationservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

  private final JwtProperties jwtProperties;

  @Override
  public String createAccessToken(Long userId, UserRole role) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProperties.getAccessTokenExpiration(), ChronoUnit.MILLIS);

    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role.name())
        .claim("type", "ACCESS")
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(getSigningKey())
        .compact();
  }

  @Override
  public String createRefreshToken(Long userId, UserRole role) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.MILLIS);

    return Jwts.builder()
        .subject(userId.toString())
        .claim("type", "REFRESH")
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(getSigningKey())
        .compact();
  }

  @Override
  public Claims isValid(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException | IllegalArgumentException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return null;
    }
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
