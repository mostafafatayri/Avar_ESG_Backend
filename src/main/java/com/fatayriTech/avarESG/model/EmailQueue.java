package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.EmailQueueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "email_queue",
        indexes = {
                @Index(
                        name = "idx_email_queue_status_next_attempt",
                        columnList = "status,next_attempt_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailQueue extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailQueueStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "notification_event_id")
    private Long notificationEventId;

    @Column(name = "reference_id")
    private Long referenceId;

    @PrePersist
    protected void applyDefaults() {
        if (status == null) {
            status = EmailQueueStatus.PENDING;
        }

        if (retryCount == null) {
            retryCount = 0;
        }

        if (maxRetries == null) {
            maxRetries = 3;
        }

        if (nextAttemptAt == null) {
            nextAttemptAt = LocalDateTime.now();
        }
    }
}