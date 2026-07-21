package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.UserRequests.RequestPasswordChangeRequest;
import com.fatayriTech.avarESG.dto.request.UserRequests.VerifyPasswordChangeOtpRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.PasswordChangeResponse;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.service.AuthService.PasswordChangeService;
import com.fatayriTech.avarESG.service.SecurityService.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "${api.prefix}/auth/password-change"
)
@RequiredArgsConstructor
public class PasswordChangeController {

    private final PasswordChangeService
            passwordChangeService;

    @PostMapping("/request-otp")
    public PasswordChangeResponse requestOtp(
            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            RequestPasswordChangeRequest request
    ) {
        Long userId =
                requireCurrentUser(currentUser);

        return passwordChangeService
                .requestPasswordChange(
                        userId,
                        request
                );
    }

    @PostMapping("/resend-otp")
    public PasswordChangeResponse resendOtp(
            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        Long userId =
                requireCurrentUser(currentUser);

        return passwordChangeService
                .resendOtp(userId);
    }

    @PostMapping("/confirm")
    public PasswordChangeResponse confirm(
            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            VerifyPasswordChangeOtpRequest request
    ) {
        Long userId =
                requireCurrentUser(currentUser);

        return passwordChangeService
                .verifyOtpAndChangePassword(
                        userId,
                        request
                );
    }

    private Long requireCurrentUser(
            CurrentUser currentUser
    ) {
        if (currentUser == null
                || currentUser.userId() == null) {
            throw new BadRequestException(
                    "Authenticated user could not be resolved"
            );
        }

        return currentUser.userId();
    }
}