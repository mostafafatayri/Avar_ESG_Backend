package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.response.NotificationResponses.InAppNotificationResponse;
import com.fatayriTech.avarESG.service.NotificationService.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(
        "${api.prefix}/notifications"
)
@RequiredArgsConstructor
public class InAppNotificationController {

    private final InAppNotificationService
            inAppNotificationService;

    @GetMapping("/me")
    public List<InAppNotificationResponse>
    getMyNotifications() {
        return inAppNotificationService
                .getMyNotifications();
    }

    @GetMapping("/me/unread-count")
    public Map<String, Long> getUnreadCount() {
        return Map.of(
                "unreadCount",
                inAppNotificationService
                        .getUnreadCount()
        );
    }

    @PatchMapping("/{notificationId}/read")
    public InAppNotificationResponse markAsRead(
            @PathVariable
            Long notificationId
    ) {
        return inAppNotificationService
                .markAsRead(notificationId);
    }

    @PatchMapping("/me/read-all")
    public void markAllAsRead() {
        inAppNotificationService
                .markAllAsRead();
    }
}