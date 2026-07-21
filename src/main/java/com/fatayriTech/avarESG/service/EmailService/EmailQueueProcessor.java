package com.fatayriTech.avarESG.service.EmailService;

import com.fatayriTech.avarESG.enums.EmailQueueStatus;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import com.fatayriTech.avarESG.model.EmailQueue;
import com.fatayriTech.avarESG.repository.EmailQueueRepository;
import com.fatayriTech.avarESG.repository.NotificationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueProcessor {

    private final EmailQueueClaimService emailQueueClaimService;
    private final EmailQueueRepository emailQueueRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:no-reply@avaresg.com}")
    private String senderEmail;

    @Value("${app.notifications.email-enabled:false}")
    private boolean emailEnabled;

    @Scheduled(fixedDelay = 10000)
    public void processQueue() {
        if (!emailEnabled) {
            return;
        }

        List<Long> readyEmailIds =
                emailQueueClaimService
                        .claimReadyEmailIds();

        for (Long emailId : readyEmailIds) {
            processSingleEmail(emailId);
        }
    }

    @Transactional
    public void processSingleEmail(Long emailId) {
        EmailQueue email =
                emailQueueRepository
                        .findById(emailId)
                        .orElse(null);

        if (email == null) {
            return;
        }

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(
                    email.getRecipientEmail()
            );
            message.setSubject(
                    email.getSubject()
            );
            message.setText(
                    email.getBody()
            );

            javaMailSender.send(message);

            email.setStatus(
                    EmailQueueStatus.SENT
            );

            email.setSentAt(
                    LocalDateTime.now()
            );

            email.setLastError(null);

            updateNotificationEventAsSent(
                    email
            );

            log.info(
                    "Email sent successfully. emailQueueId={}, recipient={}",
                    email.getId(),
                    email.getRecipientEmail()
            );

        } catch (Exception exception) {
            handleFailure(
                    email,
                    exception
            );
        }

        emailQueueRepository.save(email);
    }

    private void handleFailure(
            EmailQueue email,
            Exception exception
    ) {
        int newRetryCount =
                email.getRetryCount() + 1;

        email.setRetryCount(
                newRetryCount
        );

        email.setLastError(
                extractErrorMessage(exception)
        );

        if (newRetryCount
                >= email.getMaxRetries()) {

            email.setStatus(
                    EmailQueueStatus.FAILED
            );

            updateNotificationEventAsFailed(
                    email
            );

            log.error(
                    "Email permanently failed. emailQueueId={}, recipient={}",
                    email.getId(),
                    email.getRecipientEmail(),
                    exception
            );

            return;
        }

        email.setStatus(
                EmailQueueStatus.RETRY
        );

        long retryDelayMinutes =
                switch (newRetryCount) {
                    case 1 -> 1;
                    case 2 -> 5;
                    default -> 15;
                };

        email.setNextAttemptAt(
                LocalDateTime.now()
                        .plusMinutes(
                                retryDelayMinutes
                        )
        );

        log.warn(
                "Email failed. Retry scheduled. emailQueueId={}, retryCount={}, nextAttemptAt={}",
                email.getId(),
                newRetryCount,
                email.getNextAttemptAt()
        );
    }

    private void updateNotificationEventAsSent(
            EmailQueue email
    ) {
        if (email.getNotificationEventId()
                == null) {
            return;
        }

        notificationEventRepository
                .findById(
                        email.getNotificationEventId()
                )
                .ifPresent(event -> {
                    event.setStatus(
                            NotificationStatus.SENT
                    );

                    event.setSentAt(
                            LocalDateTime.now()
                    );

                    event.setErrorMessage(null);

                    notificationEventRepository
                            .save(event);
                });
    }

    private void updateNotificationEventAsFailed(
            EmailQueue email
    ) {
        if (email.getNotificationEventId()
                == null) {
            return;
        }

        notificationEventRepository
                .findById(
                        email.getNotificationEventId()
                )
                .ifPresent(event -> {
                    event.setStatus(
                            NotificationStatus.FAILED
                    );

                    event.setErrorMessage(
                            email.getLastError()
                    );

                    notificationEventRepository
                            .save(event);
                });
    }

    private String extractErrorMessage(
            Exception exception
    ) {
        if (exception.getMessage() != null
                && !exception.getMessage()
                .isBlank()) {
            return exception.getMessage();
        }

        return exception
                .getClass()
                .getSimpleName();
    }
}