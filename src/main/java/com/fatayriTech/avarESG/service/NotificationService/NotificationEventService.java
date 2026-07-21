package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.dto.response.NotificationResponses.NotificationEngineStatsResponse;
import com.fatayriTech.avarESG.dto.response.NotificationResponses.NotificationEventResponse;
import com.fatayriTech.avarESG.enums.NotificationChannel;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import com.fatayriTech.avarESG.model.NotificationEvent;
import com.fatayriTech.avarESG.repository.NotificationEventRepository;
import com.fatayriTech.avarESG.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fatayriTech.avarESG.model.NotificationRule;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationEventService {

    private final NotificationEventRepository
            notificationEventRepository;

    private final NotificationRuleRepository
            notificationRuleRepository;

    @Transactional(readOnly = true)
    public List<NotificationEventResponse> getEvents() {
        return notificationEventRepository
                .findAllByOrderByCreationDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationEventResponse>
    getEventsByStatus(
            NotificationStatus status
    ) {
        return notificationEventRepository
                .findByStatusOrderByCreationDateDesc(
                        status
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationEngineStatsResponse
    getStats() {

        long totalRules =
                notificationRuleRepository
                        .count();

        long activeRules =
                notificationRuleRepository
                        .findByActiveTrueOrderByCreationDateDesc()
                        .size();

        return NotificationEngineStatsResponse.builder()
                .totalRules(totalRules)
                .activeRules(activeRules)
                .inactiveRules(
                        totalRules - activeRules
                )
                .pendingEvents(
                        notificationEventRepository
                                .countByStatus(
                                        NotificationStatus.PENDING
                                )
                )
                .sentEvents(
                        notificationEventRepository
                                .countByStatus(
                                        NotificationStatus.SENT
                                )
                )
                .failedEvents(
                        notificationEventRepository
                                .countByStatus(
                                        NotificationStatus.FAILED
                                )
                )
                .skippedEvents(
                        notificationEventRepository
                                .countByStatus(
                                        NotificationStatus.SKIPPED
                                )
                )
                .emailEvents(
                        notificationEventRepository
                                .countByChannel(
                                        NotificationChannel.EMAIL
                                )
                )
                .inAppEvents(
                        notificationEventRepository
                                .countByChannel(
                                        NotificationChannel.IN_APP
                                )
                )
                .build();
    }

    private NotificationEventResponse mapToResponse(
            NotificationEvent event
    ) {
        NotificationRule rule =
                event.getRule();

        return NotificationEventResponse.builder()
                .id(event.getId())
                .ruleId(rule.getId())
                .ruleCode(rule.getCode())
                .ruleName(rule.getName())
                .targetType(
                        event.getTargetType()
                )
                .targetId(
                        event.getTargetId()
                )
                .reportingPeriod(
                        event.getReportingPeriod()
                )
                .recipientUserId(
                        event.getRecipientUserId()
                )
                .recipientEmail(
                        event.getRecipientEmail()
                )
                .eventKey(
                        event.getEventKey()
                )
                .channel(event.getChannel())
                .status(event.getStatus())
                .sentAt(event.getSentAt())
                .errorMessage(
                        event.getErrorMessage()
                )
                .createdAt(
                        event.getCreationDate()
                )
                .updatedAt(
                        event.getModifiedDate()
                )
                .build();
    }
}