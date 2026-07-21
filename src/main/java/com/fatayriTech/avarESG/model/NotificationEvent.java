package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.NotificationChannel;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_event_key_channel",
                        columnNames = {
                                "event_key",
                                "channel"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_notification_event_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_notification_event_recipient",
                        columnList = "recipient_user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent
        extends BaseAuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "rule_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_notification_event_rule"
            )
    )
    private NotificationRule rule;

    @Column(
            name = "target_type",
            nullable = false,
            length = 100
    )
    private String targetType;

    @Column(
            name = "target_id",
            nullable = false
    )
    private Long targetId;

    /*
     * Reporting period related to the event.
     *
     * Examples:
     * 2026-07
     * 2026-Q3
     * 2026-H2
     * 2026
     */
    @Column(
            name = "reporting_period",
            length = 30
    )
    private String reportingPeriod;

    @Column(
            name = "recipient_user_id"
    )
    private Long recipientUserId;

    @Column(
            name = "recipient_email",
            length = 320
    )
    private String recipientEmail;

    @Column(
            name = "event_key",
            nullable = false,
            length = 500
    )
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    @Builder.Default
    private NotificationStatus status =
            NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;
}