package com.fatayriTech.avarESG.service.AuthService;

import com.fatayriTech.avarESG.dto.request.UserRequests.LoginRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.LoginResponse;

public interface AuthService {
    LoginResponse refresh(String refreshToken);

    void logout(String refreshToken);
    LoginResponse login(LoginRequest request);
}