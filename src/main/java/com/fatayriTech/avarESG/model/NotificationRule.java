package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import com.fatayriTech.avarESG.enums.NotificationModule;
import com.fatayriTech.avarESG.enums.NotificationRecipientType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "notification_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_rule_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_notification_rule_module_event",
                        columnList = "module,event_type"
                ),
                @Index(
                        name = "idx_notification_rule_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRule
        extends BaseAuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String code;

    @Column(
            nullable = false,
            length = 180
    )
    private String name;

    @Column(
            length = 1000
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private NotificationModule module;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 60
    )
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "recipient_type",
            nullable = false,
            length = 40
    )
    private NotificationRecipientType recipientType;

    /*
     * Used when recipient type is USER.
     */
    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    /*
     * Used when recipient type is ROLE.
     *
     * Examples:
     * ESG_OWNER
     * APPROVER
     * ADMIN
     */
    @Column(
            name = "recipient_role_code",
            length = 100
    )
    private String recipientRoleCode;

    @Column(
            name = "channel_email",
            nullable = false
    )
    @Builder.Default
    private Boolean channelEmail = false;

    @Column(
            name = "channel_in_app",
            nullable = false
    )
    @Builder.Default
    private Boolean channelInApp = true;

    /*
     * For due reminders:
     * notify N days before the period ends.
     */
    @Column(name = "days_before")
    private Integer daysBefore;

    /*
     * For overdue reminders:
     * notify N days after the period ends.
     */
    @Column(name = "days_after")
    private Integer daysAfter;

    /*
     * Spring cron format:
     *
     * second minute hour day month weekday
     *
     * Example:
     * 0 0 8 * * *
     */
    @Column(
            name = "cron_expression",
            length = 100
    )
    private String cronExpression;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(
            name = "subject_template",
            columnDefinition = "TEXT"
    )
    private String subjectTemplate;

    @Column(
            name = "body_template",
            columnDefinition = "TEXT"
    )
    private String bodyTemplate;

    @PrePersist
    protected void initializeDefaults() {
        if (active == null) {
            active = true;
        }

        if (channelEmail == null) {
            channelEmail = false;
        }

        if (channelInApp == null) {
            channelInApp = true;
        }

        if (recipientType == null) {
            recipientType =
                    NotificationRecipientType.KPI_OWNER;
        }
    }
}