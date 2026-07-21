package com.fatayriTech.avarESG.dto.response.SecurityResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {

    private Long id;
    private String name;
    private String description;
    private String module;
    private String type;
    private boolean active;
}