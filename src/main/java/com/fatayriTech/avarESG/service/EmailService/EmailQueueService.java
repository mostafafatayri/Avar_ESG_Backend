package com.fatayriTech.avarESG.service.EmailService;

import com.fatayriTech.avarESG.enums.EmailQueueStatus;
import com.fatayriTech.avarESG.model.EmailQueue;
import com.fatayriTech.avarESG.repository.EmailQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final EmailQueueRepository
            emailQueueRepository;

    @Transactional
    public EmailQueue queueEmail(
            String recipientEmail,
            String subject,
            String body,
            String referenceType,
            Long referenceId,
            Long notificationEventId
    ) {
        if (recipientEmail == null
                || recipientEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is required"
            );
        }

        EmailQueue email =
                EmailQueue.builder()
                        .recipientEmail(
                                recipientEmail.trim()
                        )
                        .subject(
                                normalizeSubject(subject)
                        )
                        .body(
                                normalizeBody(body)
                        )
                        .status(
                                EmailQueueStatus.PENDING
                        )
                        .retryCount(0)
                        .maxRetries(3)
                        .nextAttemptAt(
                                LocalDateTime.now()
                        )
                        .referenceType(
                                referenceType
                        )
                        .referenceId(
                                referenceId
                        )
                        .notificationEventId(
                                notificationEventId
                        )
                        .build();

        return emailQueueRepository.save(email);
    }

    private String normalizeSubject(
            String subject
    ) {
        return subject == null
                || subject.isBlank()
                ? "AVAR ESG Notification"
                : subject.trim();
    }

    private String normalizeBody(
            String body
    ) {
        return body == null
                || body.isBlank()
                ? "You have a new notification in AVAR ESG."
                : body;
    }
}