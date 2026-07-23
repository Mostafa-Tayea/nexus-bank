package com.mostafa.nexus_bank.role.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mostafa.nexus_bank.common.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "Response object representing a role")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RoleResponse {

    @Schema(description = "Unique identifier of the role", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Role type name", example = "ROLE_USER")
    @JsonProperty("name")
    private RoleType name;

    @Schema(description = "Description of the role", example = "Standard user role with basic access")
    @JsonProperty("description")
    private String description;
}
