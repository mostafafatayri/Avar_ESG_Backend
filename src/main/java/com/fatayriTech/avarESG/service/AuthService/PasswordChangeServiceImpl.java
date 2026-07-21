package com.fatayriTech.avarESG.service.AuthService;

import com.fatayriTech.avarESG.dto.request.UserRequests.RequestPasswordChangeRequest;
import com.fatayriTech.avarESG.dto.request.UserRequests.VerifyPasswordChangeOtpRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.PasswordChangeResponse;
import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.PasswordChangeOtp;
import com.fatayriTech.avarESG.repository.PasswordChangeOtpRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.service.EmailService.EmailQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordChangeServiceImpl
        implements PasswordChangeService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordChangeOtpRepository passwordChangeOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailQueueService emailQueueService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Override
    @Transactional
    public PasswordChangeResponse requestPasswordChange(
            Long userId,
            RequestPasswordChangeRequest request
    ) {
        AppUser user = getActiveUser(userId);

        validatePasswordRequest(
                user,
                request
        );

        /*
         * Invalidate any previous password-change OTP.
         */
        passwordChangeOtpRepository
                .deleteByUserId(userId);

        String otp = generateOtp();

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusMinutes(
                                OTP_EXPIRY_MINUTES
                        );

        PasswordChangeOtp passwordChangeOtp =
                PasswordChangeOtp.builder()
                        .userId(user.getId())
                        .otpHash(
                                passwordEncoder.encode(
                                        otp
                                )
                        )
                        .pendingPasswordHash(
                                passwordEncoder.encode(
                                        request.getNewPassword()
                                )
                        )
                        .expiresAt(expiresAt)
                        .attemptCount(0)
                        .maxAttempts(
                                MAX_OTP_ATTEMPTS
                        )
                        .used(false)
                        .build();

        passwordChangeOtpRepository.save(
                passwordChangeOtp
        );

        queueOtpEmail(
                user,
                otp,
                passwordChangeOtp.getId()
        );

        return PasswordChangeResponse.builder()
                .message(
                        "A verification code was sent to your email"
                )
                .maskedEmail(
                        maskEmail(user.getEmail())
                )
                .expiresAt(expiresAt)
                .completed(false)
                .build();
    }

    @Override
    @Transactional
    public PasswordChangeResponse resendOtp(
            Long userId
    ) {
        AppUser user = getActiveUser(userId);

        PasswordChangeOtp existingRequest =
                passwordChangeOtpRepository
                        .findTopByUserIdAndUsedFalseOrderByCreationDateDesc(
                                userId
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Start the password-change process first"
                                )
                        );

        String otp = generateOtp();

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusMinutes(
                                OTP_EXPIRY_MINUTES
                        );

        existingRequest.setOtpHash(
                passwordEncoder.encode(otp)
        );

        existingRequest.setExpiresAt(
                expiresAt
        );

        existingRequest.setAttemptCount(0);
        existingRequest.setUsed(false);
        existingRequest.setUsedAt(null);

        PasswordChangeOtp savedRequest =
                passwordChangeOtpRepository.save(
                        existingRequest
                );

        queueOtpEmail(
                user,
                otp,
                savedRequest.getId()
        );

        return PasswordChangeResponse.builder()
                .message(
                        "A new verification code was sent to your email"
                )
                .maskedEmail(
                        maskEmail(user.getEmail())
                )
                .expiresAt(expiresAt)
                .completed(false)
                .build();
    }

    @Override
    @Transactional
    public PasswordChangeResponse verifyOtpAndChangePassword(
            Long userId,
            VerifyPasswordChangeOtpRequest request
    ) {
        AppUser user = getActiveUser(userId);

        PasswordChangeOtp passwordChangeOtp =
                passwordChangeOtpRepository
                        .findTopByUserIdAndUsedFalseOrderByCreationDateDesc(
                                userId
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No active password-change request was found"
                                )
                        );

        if (passwordChangeOtp.isUsed()) {
            throw new BadRequestException(
                    "This verification code has already been used"
            );
        }

        if (passwordChangeOtp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "The verification code has expired"
            );
        }

        if (passwordChangeOtp.getAttemptCount()
                >= passwordChangeOtp.getMaxAttempts()) {
            throw new BadRequestException(
                    "Maximum verification attempts exceeded. Request a new code"
            );
        }

        boolean validOtp =
                passwordEncoder.matches(
                        request.getOtp(),
                        passwordChangeOtp.getOtpHash()
                );

        if (!validOtp) {
            passwordChangeOtp.setAttemptCount(
                    passwordChangeOtp
                            .getAttemptCount() + 1
            );

            passwordChangeOtpRepository.save(
                    passwordChangeOtp
            );

            int remainingAttempts =
                    passwordChangeOtp
                            .getMaxAttempts()
                            - passwordChangeOtp
                            .getAttemptCount();

            throw new BadRequestException(
                    "Invalid verification code. "
                            + remainingAttempts
                            + " attempt(s) remaining"
            );
        }

        /*
         * The pending password is already encoded.
         */
        user.setPassword(
                passwordChangeOtp
                        .getPendingPasswordHash()
        );

        user.setMustChangePassword(false);

        /*
         * Revoke the current refresh token so the user must
         * log in again with the new password.
         */
        user.setRefreshToken(null);

        userRepository.save(user);

        passwordChangeOtp.setUsed(true);
        passwordChangeOtp.setUsedAt(
                LocalDateTime.now()
        );

        passwordChangeOtpRepository.save(
                passwordChangeOtp
        );

        queuePasswordChangedEmail(user);

        return PasswordChangeResponse.builder()
                .message(
                        "Your password was changed successfully. Log in using your new password"
                )
                .maskedEmail(
                        maskEmail(user.getEmail())
                )
                .completed(true)
                .build();
    }

    private void validatePasswordRequest(
            AppUser user,
            RequestPasswordChangeRequest request
    ) {
        if (!request.getNewPassword()
                .equals(
                        request.getConfirmPassword()
                )) {
            throw new BadRequestException(
                    "Passwords do not match"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {
            throw new BadRequestException(
                    "The new password must be different from the current password"
            );
        }
    }

    private AppUser getActiveUser(
            Long userId
    ) {
        AppUser user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (user.getStatus()
                != UserStatus.ACTIVE) {
            throw new BadRequestException(
                    "User account is not active"
            );
        }

        return user;
    }

    private String generateOtp() {
        int value =
                secureRandom.nextInt(900000)
                        + 100000;

        return String.valueOf(value);
    }

    private void queueOtpEmail(
            AppUser user,
            String otp,
            Long passwordChangeRequestId
    ) {
        String subject =
                "Verify your AVAR ESG password change";

        String body =
                """
                Hello %s,

                We received a request to change your AVAR ESG password.

                Your verification code is:

                %s

                This code expires in %d minutes.

                If you did not request this change, contact your administrator immediately.

                AVAR ESG Security
                """.formatted(
                        user.getFullName(),
                        otp,
                        OTP_EXPIRY_MINUTES
                );

        emailQueueService.queueEmail(
                user.getEmail(),
                subject,
                body,
                "PASSWORD_CHANGE_OTP",
                passwordChangeRequestId,
                null
        );
    }

    private void queuePasswordChangedEmail(
            AppUser user
    ) {
        String subject =
                "Your AVAR ESG password was changed";

        String body =
                """
                Hello %s,

                Your AVAR ESG account password was changed successfully.

                You can now log in using your new password.

                If you did not perform this action, contact your administrator immediately.

                AVAR ESG Security
                """.formatted(
                        user.getFullName()
                );

        emailQueueService.queueEmail(
                user.getEmail(),
                subject,
                body,
                "PASSWORD_CHANGED",
                user.getId(),
                null
        );
    }

    private String maskEmail(
            String email
    ) {
        if (email == null
                || !email.contains("@")) {
            return email;
        }

        String[] parts =
                email.split("@", 2);

        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0)
                    + "***@"
                    + domain;
        }

        return name.substring(0, 2)
                + "***@"
                + domain;
    }
}