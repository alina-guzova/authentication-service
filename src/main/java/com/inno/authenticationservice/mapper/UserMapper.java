package com.inno.authenticationservice.mapper;

import com.inno.authenticationservice.dto.request.UserClientCreateRequestDto;
import com.inno.authenticationservice.dto.request.UserRegisterRequestDto;
import com.inno.authenticationservice.dto.response.UserRegisterResponseDto;
import com.inno.authenticationservice.dto.response.UserServiceClientResponseDto;
import com.inno.authenticationservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  @Mapping(target = "email", source = "email", qualifiedByName = "cleanEmail")
  User toEntity(UserRegisterRequestDto dto);

  UserClientCreateRequestDto toUserClientCreateDto(UserRegisterRequestDto dto);

  @Mapping(target = "user", source = "userDto")
  @Mapping(target = "accessToken", source = "accessToken")
  @Mapping(target = "refreshToken", source = "refreshToken")
  UserRegisterResponseDto toUserRegisterResponseDto(
      UserServiceClientResponseDto userDto, String accessToken, String refreshToken);

  @Named("cleanEmail")
  default String cleanEmail(String email) {
    return email == null ? null : email.trim().toLowerCase();
  }
}
