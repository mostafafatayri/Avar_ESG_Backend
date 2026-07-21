package com.fatayriTech.avarESG.dto.request.UserRequests;

import com.fatayriTech.avarESG.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "A valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            message = "Password must contain at least 8 characters"
    )
    private String password;

    private String jobTitle;
    private Long departmentId;
    private String businessUnit;
    private String siteFacility;

    private UserStatus status;

    private Set<String> roleCodes = new HashSet<>();
}