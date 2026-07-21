package com.fatayriTech.avarESG.dto.request.SecurityRequests;

import lombok.Data;

@Data
public class UpdateRoleRequest {

    private String name;
    private String description;
    private Boolean active;
}