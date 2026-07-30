package com.inno.authenticationservice.service;

import com.inno.authenticationservice.dto.request.LoginRequestDto;
import com.inno.authenticationservice.dto.request.TokenRefreshRequestDto;
import com.inno.authenticationservice.dto.request.TokenValidateRequestDto;
import com.inno.authenticationservice.dto.request.UserRegisterRequestDto;
import com.inno.authenticationservice.dto.response.LoginResponseDto;
import com.inno.authenticationservice.dto.response.TokenRefreshResponseDto;
import com.inno.authenticationservice.dto.response.TokenValidateResponseDto;
import com.inno.authenticationservice.dto.response.UserRegisterResponseDto;

public interface AuthService {

  UserRegisterResponseDto create(UserRegisterRequestDto dto);

  TokenValidateResponseDto validateToken(TokenValidateRequestDto dto);

  TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto dto);

  LoginResponseDto login(LoginRequestDto dto);
}
