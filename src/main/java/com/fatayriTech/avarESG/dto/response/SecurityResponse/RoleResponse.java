package com.fatayriTech.avarESG.dto.response.SecurityResponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoleResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean systemRole;
    private boolean active;

    private long assignedUsersCount;
    private int permissionsCount;

    private List<PermissionResponse> permissions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}