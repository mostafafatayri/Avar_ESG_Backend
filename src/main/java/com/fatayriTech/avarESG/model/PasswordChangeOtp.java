package com.fatayriTech.avarESG.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "password_change_otps",
        indexes = {
                @Index(
                        name = "idx_password_change_otp_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_password_change_otp_expiry",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeOtp
        extends BaseAuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "otp_hash",
            nullable = false,
            length = 255
    )
    private String otpHash;

    @Column(
            name = "pending_password_hash",
            nullable = false,
            length = 255
    )
    private String pendingPasswordHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(
            name = "attempt_count",
            nullable = false
    )
    private Integer attemptCount = 0;

    @Builder.Default
    @Column(
            name = "max_attempts",
            nullable = false
    )
    private Integer maxAttempts = 5;

    @Builder.Default
    @Column(
            name = "used",
            nullable = false
    )
    private boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}