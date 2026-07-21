package com.fatayriTech.avarESG.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_invitation_tokens",
        indexes = {
                @Index(
                        name = "idx_invitation_token_hash",
                        columnList = "token_hash"
                ),
                @Index(
                        name = "idx_invitation_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvitationToken
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
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 128
    )
    private String tokenHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean revoked = false;
}