package com.mostafa.nexus_bank.user.mapper;

import com.mostafa.nexus_bank.common.enums.RoleType;
import com.mostafa.nexus_bank.user.dto.request.CreateUserRequest;
import com.mostafa.nexus_bank.user.dto.request.UpdateUserRequest;
import com.mostafa.nexus_bank.user.dto.response.UserProfileResponse;
import com.mostafa.nexus_bank.user.dto.response.UserResponse;
import com.mostafa.nexus_bank.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    @Mapping(target = "verificationTokens", ignore = true)
    @Mapping(target = "otpCodes", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "nationalId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    @Mapping(target = "verificationTokens", ignore = true)
    @Mapping(target = "otpCodes", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "enabled", target = "enabled")
    @Mapping(source = "createdAt", target = "createdAt")
    UserResponse toResponse(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "nationalId", target = "nationalId")
    @Mapping(source = "enabled", target = "enabled")
    @Mapping(source = "accountNonLocked", target = "accountNonLocked")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastLogin", target = "lastLogin")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserProfileResponse toProfileResponse(User user);

    default Set<RoleType> mapRoles(Set<com.mostafa.nexus_bank.role.entity.Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(com.mostafa.nexus_bank.role.entity.Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
}
