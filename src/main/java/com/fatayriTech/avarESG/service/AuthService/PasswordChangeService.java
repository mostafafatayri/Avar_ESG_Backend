package com.fatayriTech.avarESG.service.AuthService;

import com.fatayriTech.avarESG.dto.request.UserRequests.RequestPasswordChangeRequest;
import com.fatayriTech.avarESG.dto.request.UserRequests.VerifyPasswordChangeOtpRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.PasswordChangeResponse;

public interface PasswordChangeService {

    PasswordChangeResponse requestPasswordChange(
            Long userId,
            RequestPasswordChangeRequest request
    );

    PasswordChangeResponse resendOtp(
            Long userId
    );

    PasswordChangeResponse verifyOtpAndChangePassword(
            Long userId,
            VerifyPasswordChangeOtpRequest request
    );
}