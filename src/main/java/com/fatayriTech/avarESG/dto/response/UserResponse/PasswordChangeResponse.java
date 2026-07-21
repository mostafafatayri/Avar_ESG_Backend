package com.fatayriTech.avarESG.dto.response.UserResponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PasswordChangeResponse {

    private String message;
    private String maskedEmail;
    private LocalDateTime expiresAt;
    private boolean completed;
}