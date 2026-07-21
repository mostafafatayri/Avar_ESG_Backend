package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.response.NotificationResponses.NotificationEngineStatsResponse;
import com.fatayriTech.avarESG.dto.response.NotificationResponses.NotificationEventResponse;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import com.fatayriTech.avarESG.service.NotificationService.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "${api.prefix}/notification-events"
)
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationEventService
            notificationEventService;

    @GetMapping
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_EVENT_VIEW')"
    )
    public List<NotificationEventResponse>
    getEvents() {
        return notificationEventService
                .getEvents();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_EVENT_VIEW')"
    )
    public List<NotificationEventResponse>
    getEventsByStatus(
            @PathVariable
            NotificationStatus status
    ) {
        return notificationEventService
                .getEventsByStatus(status);
    }

    @GetMapping("/stats")
    @PreAuthorize(
            "hasAuthority('NOTIFICATION_EVENT_VIEW')"
    )
    public NotificationEngineStatsResponse
    getStats() {
        return notificationEventService
                .getStats();
    }
}