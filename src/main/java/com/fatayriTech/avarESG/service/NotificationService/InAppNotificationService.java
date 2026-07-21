package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.dto.response.NotificationResponses.InAppNotificationResponse;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.InAppNotification;
import com.fatayriTech.avarESG.repository.InAppNotificationRepository;
import com.fatayriTech.avarESG.service.SecurityService.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InAppNotificationService {

    private final InAppNotificationRepository
            inAppNotificationRepository;

    private final CurrentUserService
            currentUserService;

    @Transactional(readOnly = true)
    public List<InAppNotificationResponse>
    getMyNotifications() {

        Long userId =
                currentUserService.getUserId();

        return inAppNotificationRepository
                .findTop30ByUserIdOrderByCreationDateDesc(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return inAppNotificationRepository
                .countByUserIdAndReadFalse(
                        currentUserService.getUserId()
                );
    }

    @Transactional
    public InAppNotificationResponse markAsRead(
            Long notificationId
    ) {
        Long userId =
                currentUserService.getUserId();

        InAppNotification notification =
                inAppNotificationRepository
                        .findByIdAndUserId(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found with id: "
                                                + notificationId
                                )
                        );

        if (!Boolean.TRUE.equals(
                notification.getRead()
        )) {
            notification.setRead(true);

            notification.setReadAt(
                    LocalDateTime.now()
            );
        }

        return mapToResponse(
                inAppNotificationRepository
                        .save(notification)
        );
    }

    @Transactional
    public void markAllAsRead() {
        Long userId =
                currentUserService.getUserId();

        List<InAppNotification> notifications =
                inAppNotificationRepository
                        .findByUserIdOrderByCreationDateDesc(
                                userId
                        );

        LocalDateTime now =
                LocalDateTime.now();

        notifications.stream()
                .filter(notification ->
                        !Boolean.TRUE.equals(
                                notification.getRead()
                        )
                )
                .forEach(notification -> {
                    notification.setRead(true);
                    notification.setReadAt(now);
                });

        inAppNotificationRepository.saveAll(
                notifications
        );
    }

    private InAppNotificationResponse mapToResponse(
            InAppNotification notification
    ) {
        return InAppNotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .actionUrl(
                        notification.getActionUrl()
                )
                .targetType(
                        notification.getTargetType()
                )
                .targetId(
                        notification.getTargetId()
                )
                .reportingPeriod(
                        notification.getReportingPeriod()
                )
                .read(notification.getRead())
                .readAt(notification.getReadAt())
                .createdAt(
                        notification.getCreationDate()
                )
                .build();
    }
}