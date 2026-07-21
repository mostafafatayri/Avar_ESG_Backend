package com.fatayriTech.avarESG.dto.request.NotificationRequests;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import com.fatayriTech.avarESG.enums.NotificationModule;
import com.fatayriTech.avarESG.enums.NotificationRecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRuleRequest {

    @NotBlank(message = "Rule code is required")
    @Size(
            max = 100,
            message = "Rule code cannot exceed 100 characters"
    )
    private String code;

    @NotBlank(message = "Rule name is required")
    @Size(
            max = 180,
            message = "Rule name cannot exceed 180 characters"
    )
    private String name;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters"
    )
    private String description;

    @NotNull(message = "Notification module is required")
    private NotificationModule module;

    @NotNull(message = "Notification event type is required")
    private NotificationEventType eventType;

    @NotNull(message = "Recipient type is required")
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
}