package com.fatayriTech.avarESG.dto.response.NotificationResponses;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import com.fatayriTech.avarESG.enums.NotificationModule;
import com.fatayriTech.avarESG.enums.NotificationRecipientType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRuleResponse {

    private Long id;

    private String code;

    private String name;

    private String description;

    private NotificationModule module;

    private NotificationEventType eventType;

    private NotificationRecipientType recipientType;

    private Long recipientUserId;

    private String recipientRoleCode;

    private Boolean channelEmail;

    private Boolean channelInApp;

    private Integer daysBefore;

    private Integer daysAfter;

    private String cronExpression;

    private Boolean active;

    private String subjectTemplate;

    private String bodyTemplate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}