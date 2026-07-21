package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "business_unit")
    private String businessUnit;

    @Column(name = "site_facility")
    private String siteFacility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "invitation_sent_at")
    private LocalDateTime invitationSentAt;

    @Column(name = "invitation_accepted_at")
    private LocalDateTime invitationAcceptedAt;

    @Builder.Default
    @Column(
            name = "must_change_password",
            nullable = false
    )
    private boolean mustChangePassword = true;
}