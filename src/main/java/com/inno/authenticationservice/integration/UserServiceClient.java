package com.inno.authenticationservice.integration;

import com.inno.authenticationservice.dto.request.UserClientCreateRequestDto;
import com.inno.authenticationservice.dto.response.UserServiceClientResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${application.integration.user-service.url}")
public interface UserServiceClient {

  @PostMapping("/api/v1/users/{userId}")
  UserServiceClientResponseDto createUser(
      @PathVariable("userId") Long userId, @RequestBody UserClientCreateRequestDto dto);
}
