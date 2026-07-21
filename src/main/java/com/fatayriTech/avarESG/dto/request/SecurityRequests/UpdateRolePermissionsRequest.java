package com.fatayriTech.avarESG.dto.request.SecurityRequests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UpdateRolePermissionsRequest {

    @NotNull(message = "Permission IDs are required")
    private Set<Long> permissionIds = new HashSet<>();
}