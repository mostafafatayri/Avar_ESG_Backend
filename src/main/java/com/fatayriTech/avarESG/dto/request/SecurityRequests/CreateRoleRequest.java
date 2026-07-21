package com.fatayriTech.avarESG.dto.request.SecurityRequests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    @NotBlank(message = "Role code is required")
    private String code;

    private String description;

    private Set<Long> permissionIds = new HashSet<>();
}