package com.mostafa.nexus_bank.role.mapper;

import com.mostafa.nexus_bank.role.dto.response.RoleResponse;
import com.mostafa.nexus_bank.role.entity.Role;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);

    Set<RoleResponse> toResponseSet(Set<Role> roles);
}
