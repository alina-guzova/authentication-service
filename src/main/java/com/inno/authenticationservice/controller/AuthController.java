package com.inno.authenticationservice.controller;

import com.inno.authenticationservice.dto.request.LoginRequestDto;
import com.inno.authenticationservice.dto.request.TokenRefreshRequestDto;
import com.inno.authenticationservice.dto.request.TokenValidateRequestDto;
import com.inno.authenticationservice.dto.request.UserRegisterRequestDto;
import com.inno.authenticationservice.dto.response.LoginResponseDto;
import com.inno.authenticationservice.dto.response.TokenRefreshResponseDto;
import com.inno.authenticationservice.dto.response.TokenValidateResponseDto;
import com.inno.authenticationservice.dto.response.UserRegisterResponseDto;
import com.inno.authenticationservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponseDto> register(
      @Valid @RequestBody UserRegisterRequestDto userRegisterRequest) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.create(userRegisterRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/validate")
  public ResponseEntity<TokenValidateResponseDto> validate(
      @Valid @RequestBody TokenValidateRequestDto request) {
    return ResponseEntity.ok(authService.validateToken(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponseDto> refresh(
      @Valid @RequestBody TokenRefreshRequestDto request) {
    return ResponseEntity.ok(authService.refreshToken(request));
  }
}
