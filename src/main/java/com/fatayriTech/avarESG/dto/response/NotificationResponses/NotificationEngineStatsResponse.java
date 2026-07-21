package com.fatayriTech.avarESG.dto.response.NotificationResponses;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEngineStatsResponse {

    private long totalRules;

    private long activeRules;

    private long inactiveRules;

    private long pendingEvents;

    private long sentEvents;

    private long failedEvents;

    private long skippedEvents;

    private long emailEvents;

    private long inAppEvents;
}