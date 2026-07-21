package com.fatayriTech.avarESG.dto.response.NotificationResponses;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InAppNotificationResponse {

    private Long id;

    private Long userId;

    private String title;

    private String message;

    private NotificationEventType type;

    private String actionUrl;

    private String targetType;

    private Long targetId;

    private String reportingPeriod;

    private Boolean read;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}