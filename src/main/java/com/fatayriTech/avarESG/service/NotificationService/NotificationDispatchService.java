package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.enums.NotificationChannel;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.InAppNotification;
import com.fatayriTech.avarESG.model.NotificationEvent;
import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.InAppNotificationRepository;
import com.fatayriTech.avarESG.repository.NotificationEventRepository;
import com.fatayriTech.avarESG.service.EmailService.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationEventRepository
            notificationEventRepository;

    private final InAppNotificationRepository
            inAppNotificationRepository;

    private final EmailQueueService
            emailQueueService;

    /**
     * Dispatches the notification through all channels enabled
     * on the notification rule.
     */
    @Transactional
    public void dispatch(
            NotificationRule rule,
            AppUser recipient,
            String eventKey,
            String targetType,
            Long targetId,
            String reportingPeriod,
            String subject,
            String body,
            String actionUrl
    ) {
        if (rule == null || recipient == null) {
            return;
        }

        if (recipient.getId() == null) {
            log.warn(
                    "Notification recipient has no user ID. eventKey={}",
                    eventKey
            );

            return;
        }

        String recipientEventKey =
                buildRecipientEventKey(
                        eventKey,
                        recipient.getId()
                );

        if (Boolean.TRUE.equals(
                rule.getChannelInApp()
        )) {
            createInAppEventIfNotExists(
                    rule,
                    recipient,
                    recipientEventKey,
                    targetType,
                    targetId,
                    reportingPeriod,
                    subject,
                    body,
                    actionUrl
            );
        }

        if (Boolean.TRUE.equals(
                rule.getChannelEmail()
        )) {
            createEmailEventIfNotExists(
                    rule,
                    recipient,
                    recipientEventKey,
                    targetType,
                    targetId,
                    reportingPeriod,
                    subject,
                    body
            );
        }
    }

    /**
     * Creates an EMAIL notification event and places the email
     * inside the persistent email queue.
     *
     * The NotificationEvent remains PENDING until the queue
     * processor successfully sends the email.
     */
    @Transactional
    public void createEmailEventIfNotExists(
            NotificationRule rule,
            AppUser recipient,
            String eventKey,
            String targetType,
            Long targetId,
            String reportingPeriod,
            String subject,
            String body
    ) {
        if (recipient.getEmail() == null
                || recipient.getEmail().isBlank()) {

            log.warn(
                    "Email notification skipped because user {} has no email",
                    recipient.getId()
            );

            return;
        }

        if (notificationEventRepository
                .existsByEventKeyAndChannel(
                        eventKey,
                        NotificationChannel.EMAIL
                )) {

            log.debug(
                    "Duplicate email notification skipped. eventKey={}",
                    eventKey
            );

            return;
        }

        NotificationEvent event =
                buildEvent(
                        rule,
                        recipient,
                        eventKey,
                        targetType,
                        targetId,
                        reportingPeriod,
                        NotificationChannel.EMAIL
                );

        try {
            event =
                    notificationEventRepository.save(
                            event
                    );

            emailQueueService.queueEmail(
                    recipient.getEmail(),
                    safeSubject(subject),
                    safeBody(body),
                    targetType,
                    targetId,
                    event.getId()
            );

            /*
             * Do not mark the event SENT here.
             *
             * At this point, the email was only inserted into
             * the queue. EmailQueueProcessor will mark the
             * NotificationEvent as SENT after SMTP succeeds.
             */
            event.setStatus(
                    NotificationStatus.PENDING
            );

            event.setSentAt(null);
            event.setErrorMessage(null);

            notificationEventRepository.save(event);

            log.info(
                    "Email notification queued. eventId={}, recipient={}, eventKey={}",
                    event.getId(),
                    recipient.getEmail(),
                    eventKey
            );

        } catch (DataIntegrityViolationException exception) {
            log.debug(
                    "Duplicate email notification event skipped. eventKey={}",
                    eventKey
            );

        } catch (Exception exception) {
            event.setStatus(
                    NotificationStatus.FAILED
            );

            event.setErrorMessage(
                    extractErrorMessage(exception)
            );

            notificationEventRepository.save(event);

            log.error(
                    "Failed to queue email notification. eventKey={}",
                    eventKey,
                    exception
            );
        }
    }

    /**
     * Creates the in-app notification immediately.
     *
     * Since the in-app record is saved synchronously, its
     * NotificationEvent can be marked SENT immediately.
     */
    @Transactional
    public void createInAppEventIfNotExists(
            NotificationRule rule,
            AppUser recipient,
            String eventKey,
            String targetType,
            Long targetId,
            String reportingPeriod,
            String title,
            String message,
            String actionUrl
    ) {
        if (notificationEventRepository
                .existsByEventKeyAndChannel(
                        eventKey,
                        NotificationChannel.IN_APP
                )) {

            log.debug(
                    "Duplicate in-app notification skipped. eventKey={}",
                    eventKey
            );

            return;
        }

        NotificationEvent event =
                buildEvent(
                        rule,
                        recipient,
                        eventKey,
                        targetType,
                        targetId,
                        reportingPeriod,
                        NotificationChannel.IN_APP
                );

        try {
            event =
                    notificationEventRepository.save(
                            event
                    );

            InAppNotification notification =
                    InAppNotification.builder()
                            .userId(
                                    recipient.getId()
                            )
                            .title(
                                    safeSubject(title)
                            )
                            .message(
                                    safeBody(message)
                            )
                            .type(
                                    rule.getEventType()
                            )
                            .actionUrl(actionUrl)
                            .targetType(targetType)
                            .targetId(targetId)
                            .reportingPeriod(
                                    reportingPeriod
                            )
                            .read(false)
                            .build();

            inAppNotificationRepository.save(
                    notification
            );

            event.setStatus(
                    NotificationStatus.SENT
            );

            event.setSentAt(
                    LocalDateTime.now()
            );

            event.setErrorMessage(null);

            notificationEventRepository.save(event);

            log.info(
                    "In-app notification created. eventId={}, userId={}, eventKey={}",
                    event.getId(),
                    recipient.getId(),
                    eventKey
            );

        } catch (DataIntegrityViolationException exception) {
            log.debug(
                    "Duplicate in-app notification event skipped. eventKey={}",
                    eventKey
            );

        } catch (Exception exception) {
            event.setStatus(
                    NotificationStatus.FAILED
            );

            event.setErrorMessage(
                    extractErrorMessage(exception)
            );

            notificationEventRepository.save(event);

            log.error(
                    "In-app notification failed. eventKey={}",
                    eventKey,
                    exception
            );
        }
    }

    private NotificationEvent buildEvent(
            NotificationRule rule,
            AppUser recipient,
            String eventKey,
            String targetType,
            Long targetId,
            String reportingPeriod,
            NotificationChannel channel
    ) {
        return NotificationEvent.builder()
                .rule(rule)
                .targetType(targetType)
                .targetId(targetId)
                .reportingPeriod(
                        reportingPeriod
                )
                .recipientUserId(
                        recipient.getId()
                )
                .recipientEmail(
                        recipient.getEmail()
                )
                .eventKey(eventKey)
                .channel(channel)
                .status(
                        NotificationStatus.PENDING
                )
                .sentAt(null)
                .errorMessage(null)
                .build();
    }

    private String buildRecipientEventKey(
            String eventKey,
            Long recipientUserId
    ) {
        return eventKey
                + ":USER:"
                + recipientUserId;
    }

    private String safeSubject(
            String value
    ) {
        return value == null
                || value.isBlank()
                ? "AVAR ESG Notification"
                : value.trim();
    }

    private String safeBody(
            String value
    ) {
        return value == null
                || value.isBlank()
                ? "You have a new notification in AVAR ESG."
                : value;
    }

    private String extractErrorMessage(
            Exception exception
    ) {
        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return exception
                .getClass()
                .getSimpleName();
    }
}