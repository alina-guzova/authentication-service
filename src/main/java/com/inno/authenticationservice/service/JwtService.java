package com.inno.authenticationservice.service;

import com.inno.authenticationservice.entity.UserRole;
import io.jsonwebtoken.Claims;

public interface JwtService {

  String createAccessToken(Long userId, UserRole role);

  String createRefreshToken(Long userId, UserRole role);

  Claims isValid(String token);
}
