package com.fatayriTech.avarESG.dto.response.UserResponse;

import com.fatayriTech.avarESG.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String jobTitle;
    private Long departmentId;
    private String businessUnit;
    private String siteFacility;
    private UserStatus status;

    private boolean mustChangePassword;

    private List<String> roles;
    private List<String> permissions;

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}