package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "in_app_notifications",
        indexes = {
                @Index(
                        name = "idx_in_app_notification_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_in_app_notification_user_read",
                        columnList = "user_id,is_read"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InAppNotification
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
            nullable = false,
            length = 220
    )
    private String title;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 60
    )
    private NotificationEventType type;

    @Column(
            name = "action_url",
            length = 1000
    )
    private String actionUrl;

    @Column(
            name = "target_type",
            length = 100
    )
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(
            name = "reporting_period",
            length = 30
    )
    private String reportingPeriod;

    @Column(
            name = "is_read",
            nullable = false
    )
    @Builder.Default
    private Boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}