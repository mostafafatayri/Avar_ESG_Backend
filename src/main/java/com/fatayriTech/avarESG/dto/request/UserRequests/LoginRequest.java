package com.fatayriTech.avarESG.dto.request.UserRequests;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String identifier; // email or username

    @NotBlank
    private String password;
}