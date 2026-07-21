package com.fatayriTech.avarESG.dto.response.NotificationResponses;

import com.fatayriTech.avarESG.enums.NotificationChannel;
import com.fatayriTech.avarESG.enums.NotificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventResponse {

    private Long id;

    private Long ruleId;

    private String ruleCode;

    private String ruleName;

    private String targetType;

    private Long targetId;

    private String reportingPeriod;

    private Long recipientUserId;

    private String recipientEmail;

    private String eventKey;

    private NotificationChannel channel;

    private NotificationStatus status;

    private LocalDateTime sentAt;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}