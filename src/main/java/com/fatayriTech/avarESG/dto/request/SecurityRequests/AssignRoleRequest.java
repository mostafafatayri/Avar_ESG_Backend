package com.fatayriTech.avarESG.dto.request.SecurityRequests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotNull(message = "Role ID is required")
    private Long roleId;
}