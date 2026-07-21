package com.fatayriTech.avarESG.dto.request.UserRequests;

import com.fatayriTech.avarESG.enums.UserStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {

    private String fullName;
    private String username;

    @Email(message = "A valid email is required")
    private String email;

    private String jobTitle;
    private Long departmentId;
    private String businessUnit;
    private String siteFacility;
    private UserStatus status;

    private Set<String> roleCodes;
}