package com.fatayriTech.avarESG.dto.response.UserResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    private boolean mustChangePassword;

    private UserResponse user;
}