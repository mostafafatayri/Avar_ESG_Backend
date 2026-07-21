package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.NotificationRequests.NotificationRuleRequest;
import com.fatayriTech.avarESG.dto.response.NotificationResponses.NotificationRuleResponse;
import com.fatayriTech.avarESG.service.NotificationService.NotificationRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "${api.prefix}/notification-rules"
)
@RequiredArgsConstructor
public class NotificationRuleController {

    private final NotificationRuleService
            notificationRuleService;

    @PostMapping
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_CREATE')"
    )
    public NotificationRuleResponse create(
            @Valid
            @RequestBody
            NotificationRuleRequest request
    ) {
        return notificationRuleService.create(
                request
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_VIEW')"
    )
    public List<NotificationRuleResponse>
    getAll() {
        return notificationRuleService.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_VIEW')"
    )
    public List<NotificationRuleResponse>
    getActive() {
        return notificationRuleService
                .getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_VIEW')"
    )
    public NotificationRuleResponse getById(
            @PathVariable Long id
    ) {
        return notificationRuleService
                .getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_UPDATE')"
    )
    public NotificationRuleResponse update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            NotificationRuleRequest request
    ) {
        return notificationRuleService.update(
                id,
                request
        );
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_UPDATE')"
    )
    public NotificationRuleResponse toggle(
            @PathVariable Long id
    ) {
        return notificationRuleService
                .toggleStatus(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_RULE_DELETE')"
    )
    public void delete(
            @PathVariable Long id
    ) {
        notificationRuleService.delete(id);
    }
}