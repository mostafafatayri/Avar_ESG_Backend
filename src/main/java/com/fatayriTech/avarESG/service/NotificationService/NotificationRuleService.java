package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.dto.request.NotificationRequests.NotificationRuleRequest;
import com.fatayriTech.avarESG.dto.response.NotificationResponses.NotificationRuleResponse;
import com.fatayriTech.avarESG.enums.NotificationRecipientType;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationRuleService {

    private final NotificationRuleRepository
            notificationRuleRepository;

    private final NotificationDynamicSchedulerService
            schedulerService;

    public NotificationRuleResponse create(
            NotificationRuleRequest request
    ) {
        validateRequest(request);

        String normalizedCode =
                normalizeCode(request.getCode());

        if (notificationRuleRepository
                .existsByCodeIgnoreCase(
                        normalizedCode
                )) {
            throw new BadRequestException(
                    "Notification rule code already exists: "
                            + normalizedCode
            );
        }

        NotificationRule rule =
                NotificationRule.builder()
                        .code(normalizedCode)
                        .name(
                                request.getName().trim()
                        )
                        .description(
                                request.getDescription()
                        )
                        .module(
                                request.getModule()
                        )
                        .eventType(
                                request.getEventType()
                        )
                        .recipientType(
                                request.getRecipientType()
                        )
                        .recipientUserId(
                                request.getRecipientUserId()
                        )
                        .recipientRoleCode(
                                normalizeRoleCode(
                                        request.getRecipientRoleCode()
                                )
                        )
                        .channelEmail(
                                Boolean.TRUE.equals(
                                        request.getChannelEmail()
                                )
                        )
                        .channelInApp(
                                request.getChannelInApp() == null ||
                                        Boolean.TRUE.equals(
                                                request.getChannelInApp()
                                        )
                        )
                        .daysBefore(
                                request.getDaysBefore()
                        )
                        .daysAfter(
                                request.getDaysAfter()
                        )
                        .cronExpression(
                                request.getCronExpression()
                        )
                        .active(
                                request.getActive() == null ||
                                        Boolean.TRUE.equals(
                                                request.getActive()
                                        )
                        )
                        .subjectTemplate(
                                request.getSubjectTemplate()
                        )
                        .bodyTemplate(
                                request.getBodyTemplate()
                        )
                        .build();

        NotificationRule savedRule =
                notificationRuleRepository
                        .save(rule);

        schedulerService.scheduleOrRefresh(
                savedRule
        );

        return mapToResponse(savedRule);
    }

    @Transactional(readOnly = true)
    public List<NotificationRuleResponse> getAll() {
        return notificationRuleRepository
                .findAllByOrderByCreationDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationRuleResponse> getActive() {
        return notificationRuleRepository
                .findByActiveTrueOrderByCreationDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationRuleResponse getById(
            Long id
    ) {
        return mapToResponse(
                findRule(id)
        );
    }

    public NotificationRuleResponse update(
            Long id,
            NotificationRuleRequest request
    ) {
        validateRequest(request);

        NotificationRule rule =
                findRule(id);

        String normalizedCode =
                normalizeCode(request.getCode());

        if (notificationRuleRepository
                .existsByCodeIgnoreCaseAndIdNot(
                        normalizedCode,
                        id
                )) {
            throw new BadRequestException(
                    "Notification rule code already exists: "
                            + normalizedCode
            );
        }

        rule.setCode(normalizedCode);
        rule.setName(
                request.getName().trim()
        );
        rule.setDescription(
                request.getDescription()
        );
        rule.setModule(
                request.getModule()
        );
        rule.setEventType(
                request.getEventType()
        );
        rule.setRecipientType(
                request.getRecipientType()
        );
        rule.setRecipientUserId(
                request.getRecipientUserId()
        );
        rule.setRecipientRoleCode(
                normalizeRoleCode(
                        request.getRecipientRoleCode()
                )
        );
        rule.setChannelEmail(
                Boolean.TRUE.equals(
                        request.getChannelEmail()
                )
        );
        rule.setChannelInApp(
                Boolean.TRUE.equals(
                        request.getChannelInApp()
                )
        );
        rule.setDaysBefore(
                request.getDaysBefore()
        );
        rule.setDaysAfter(
                request.getDaysAfter()
        );
        rule.setCronExpression(
                request.getCronExpression()
        );
        rule.setActive(
                request.getActive() != null
                        ? request.getActive()
                        : rule.getActive()
        );
        rule.setSubjectTemplate(
                request.getSubjectTemplate()
        );
        rule.setBodyTemplate(
                request.getBodyTemplate()
        );

        NotificationRule savedRule =
                notificationRuleRepository
                        .save(rule);

        schedulerService.scheduleOrRefresh(
                savedRule
        );

        return mapToResponse(savedRule);
    }

    public NotificationRuleResponse toggleStatus(
            Long id
    ) {
        NotificationRule rule =
                findRule(id);

        rule.setActive(
                !Boolean.TRUE.equals(
                        rule.getActive()
                )
        );

        NotificationRule savedRule =
                notificationRuleRepository
                        .save(rule);

        schedulerService.scheduleOrRefresh(
                savedRule
        );

        return mapToResponse(savedRule);
    }

    public void delete(
            Long id
    ) {
        NotificationRule rule =
                findRule(id);

        /*
         * Soft delete because events reference the rule.
         */
        rule.setActive(false);

        notificationRuleRepository.save(rule);

        schedulerService.cancelSchedule(
                rule.getId()
        );
    }

    private NotificationRule findRule(
            Long id
    ) {
        return notificationRuleRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification rule not found with id: "
                                        + id
                        )
                );
    }

    private void validateRequest(
            NotificationRuleRequest request
    ) {
        if (request.getCode() == null ||
                request.getCode().isBlank()) {
            throw new BadRequestException(
                    "Rule code is required"
            );
        }

        if (request.getName() == null ||
                request.getName().isBlank()) {
            throw new BadRequestException(
                    "Rule name is required"
            );
        }

        if (request.getModule() == null) {
            throw new BadRequestException(
                    "Notification module is required"
            );
        }

        if (request.getEventType() == null) {
            throw new BadRequestException(
                    "Notification event type is required"
            );
        }

        if (request.getRecipientType() == null) {
            throw new BadRequestException(
                    "Recipient type is required"
            );
        }

        if (!Boolean.TRUE.equals(
                request.getChannelEmail()
        ) &&
                !Boolean.TRUE.equals(
                        request.getChannelInApp()
                )) {

            throw new BadRequestException(
                    "At least one notification channel is required"
            );
        }

        if (request.getRecipientType() ==
                NotificationRecipientType.USER &&
                request.getRecipientUserId() == null) {

            throw new BadRequestException(
                    "Recipient user is required"
            );
        }

        if (request.getRecipientType() ==
                NotificationRecipientType.ROLE &&
                (
                        request.getRecipientRoleCode() == null ||
                                request.getRecipientRoleCode()
                                        .isBlank()
                )) {

            throw new BadRequestException(
                    "Recipient role code is required"
            );
        }

        String cron =
                request.getCronExpression();

        if (cron != null &&
                !cron.isBlank() &&
                !CronExpression.isValidExpression(
                        cron
                )) {

            throw new BadRequestException(
                    "Invalid cron expression"
            );
        }

        if (request.getDaysBefore() != null &&
                request.getDaysBefore() < 0) {

            throw new BadRequestException(
                    "Days before cannot be negative"
            );
        }

        if (request.getDaysAfter() != null &&
                request.getDaysAfter() < 0) {

            throw new BadRequestException(
                    "Days after cannot be negative"
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        return value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll(
                        "[^A-Z0-9]+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );
    }

    private String normalizeRoleCode(
            String value
    ) {
        if (value == null ||
                value.isBlank()) {
            return null;
        }

        return value.trim()
                .toUpperCase(Locale.ROOT);
    }

    private NotificationRuleResponse mapToResponse(
            NotificationRule rule
    ) {
        return NotificationRuleResponse.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .description(
                        rule.getDescription()
                )
                .module(rule.getModule())
                .eventType(
                        rule.getEventType()
                )
                .recipientType(
                        rule.getRecipientType()
                )
                .recipientUserId(
                        rule.getRecipientUserId()
                )
                .recipientRoleCode(
                        rule.getRecipientRoleCode()
                )
                .channelEmail(
                        rule.getChannelEmail()
                )
                .channelInApp(
                        rule.getChannelInApp()
                )
                .daysBefore(
                        rule.getDaysBefore()
                )
                .daysAfter(
                        rule.getDaysAfter()
                )
                .cronExpression(
                        rule.getCronExpression()
                )
                .active(rule.getActive())
                .subjectTemplate(
                        rule.getSubjectTemplate()
                )
                .bodyTemplate(
                        rule.getBodyTemplate()
                )
                .createdAt(
                        rule.getCreationDate()
                )
                .updatedAt(
                        rule.getModifiedDate()
                )
                .build();
    }
}