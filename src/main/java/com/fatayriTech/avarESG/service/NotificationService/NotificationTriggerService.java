package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import com.fatayriTech.avarESG.enums.NotificationModule;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.KpiReading;
import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationTriggerService {

    private final NotificationRuleRepository
            notificationRuleRepository;

    private final NotificationRecipientResolver
            recipientResolver;

    private final NotificationDispatchService
            dispatchService;

    private final NotificationTemplateService
            templateService;

    public void triggerKpiSubmitted(
            Kpi kpi
    ) {
        triggerKpiEvent(
                kpi,
                NotificationEventType
                        .KPI_SUBMITTED_FOR_APPROVAL,
                "KPI submitted for approval",
                "The KPI {kpiName} ({kpiCode}) was submitted for approval."
        );
    }

    public void triggerKpiApproved(
            Kpi kpi
    ) {
        triggerKpiEvent(
                kpi,
                NotificationEventType.KPI_APPROVED,
                "KPI approved",
                "The KPI {kpiName} ({kpiCode}) was approved."
        );
    }

    public void triggerKpiRejected(
            Kpi kpi
    ) {
        triggerKpiEvent(
                kpi,
                NotificationEventType.KPI_REJECTED,
                "KPI rejected",
                "The KPI {kpiName} ({kpiCode}) was rejected. Reason: {rejectionReason}"
        );
    }

    public void triggerReadingSubmitted(
            KpiReading reading
    ) {
        triggerReadingEvent(
                reading,
                NotificationEventType.READING_SUBMITTED,
                "KPI reading submitted",
                "A reading for KPI {kpiName} was submitted for period {reportingPeriod}."
        );
    }

    public void triggerReadingApproved(
            KpiReading reading
    ) {
        triggerReadingEvent(
                reading,
                NotificationEventType.READING_APPROVED,
                "KPI reading approved",
                "The reading for KPI {kpiName} and period {reportingPeriod} was approved."
        );
    }

    public void triggerReadingRejected(
            KpiReading reading
    ) {
        triggerReadingEvent(
                reading,
                NotificationEventType.READING_REJECTED,
                "KPI reading rejected",
                "The reading for KPI {kpiName} and period {reportingPeriod} was rejected. "
                        + "Reason: {rejectionReason}"
        );
    }

    private void triggerKpiEvent(
            Kpi kpi,
            NotificationEventType eventType,
            String fallbackSubject,
            String fallbackBody
    ) {
        List<NotificationRule> rules =
                notificationRuleRepository
                        .findByModuleAndEventTypeAndActiveTrue(
                                NotificationModule.KPI,
                                eventType
                        );

        for (NotificationRule rule : rules) {
            List<AppUser> recipients =
                    recipientResolver
                            .resolveRecipients(
                                    rule,
                                    kpi
                            );

            Map<String, String> variables =
                    buildKpiVariables(kpi);

            String subject =
                    templateService.render(
                            rule.getSubjectTemplate(),
                            fallbackSubject,
                            variables
                    );

            String body =
                    templateService.render(
                            rule.getBodyTemplate(),
                            fallbackBody,
                            variables
                    );

            for (AppUser recipient : recipients) {
                String eventKey =
                        eventType.name()
                                + ":KPI:"
                                + kpi.getId()
                                + ":USER:"
                                + recipient.getId()
                                + ":RULE:"
                                + rule.getCode();

                dispatchService.dispatch(
                        rule,
                        recipient,
                        eventKey,
                        "KPI",
                        kpi.getId(),
                        null,
                        subject,
                        body,
                        "/kpis/" + kpi.getId()
                );
            }
        }
    }

    private void triggerReadingEvent(
            KpiReading reading,
            NotificationEventType eventType,
            String fallbackSubject,
            String fallbackBody
    ) {
        Kpi kpi =
                reading.getKpi();

        List<NotificationRule> rules =
                notificationRuleRepository
                        .findByModuleAndEventTypeAndActiveTrue(
                                NotificationModule.KPI_READING,
                                eventType
                        );

        for (NotificationRule rule : rules) {
            List<AppUser> recipients =
                    recipientResolver
                            .resolveRecipients(
                                    rule,
                                    kpi
                            );

            Map<String, String> variables =
                    buildReadingVariables(reading);

            String subject =
                    templateService.render(
                            rule.getSubjectTemplate(),
                            fallbackSubject,
                            variables
                    );

            String body =
                    templateService.render(
                            rule.getBodyTemplate(),
                            fallbackBody,
                            variables
                    );

            for (AppUser recipient : recipients) {
                String eventKey =
                        eventType.name()
                                + ":READING:"
                                + reading.getId()
                                + ":USER:"
                                + recipient.getId()
                                + ":RULE:"
                                + rule.getCode();

                dispatchService.dispatch(
                        rule,
                        recipient,
                        eventKey,
                        "KPI_READING",
                        reading.getId(),
                        reading.getReportingPeriod(),
                        subject,
                        body,
                        "/kpis/" + kpi.getId()
                );
            }
        }
    }

    private Map<String, String> buildKpiVariables(
            Kpi kpi
    ) {
        return Map.ofEntries(
                Map.entry(
                        "kpiId",
                        String.valueOf(kpi.getId())
                ),
                Map.entry(
                        "kpiCode",
                        safe(kpi.getCode())
                ),
                Map.entry(
                        "kpiName",
                        safe(kpi.getName())
                ),
                Map.entry(
                        "status",
                        kpi.getStatus() != null
                                ? kpi.getStatus().name()
                                : "-"
                ),
                Map.entry(
                        "rejectionReason",
                        safe(
                                kpi.getRejectionReason()
                        )
                )
        );
    }

    private Map<String, String>
    buildReadingVariables(
            KpiReading reading
    ) {
        return Map.ofEntries(
                Map.entry(
                        "readingId",
                        String.valueOf(
                                reading.getId()
                        )
                ),
                Map.entry(
                        "kpiId",
                        String.valueOf(
                                reading.getKpi()
                                        .getId()
                        )
                ),
                Map.entry(
                        "kpiCode",
                        safe(
                                reading.getKpi()
                                        .getCode()
                        )
                ),
                Map.entry(
                        "kpiName",
                        safe(
                                reading.getKpi()
                                        .getName()
                        )
                ),
                Map.entry(
                        "reportingPeriod",
                        safe(
                                reading.getReportingPeriod()
                        )
                ),
                Map.entry(
                        "actualValue",
                        reading.getActualValue() != null
                                ? reading.getActualValue()
                                .toPlainString()
                                : "-"
                ),
                Map.entry(
                        "approvalStatus",
                        reading.getApprovalStatus() != null
                                ? reading.getApprovalStatus()
                                .name()
                                : "-"
                ),
                Map.entry(
                        "rejectionReason",
                        safe(
                                reading.getRejectionReason()
                        )
                )
        );
    }

    private String safe(
            String value
    ) {
        return value == null ||
                value.isBlank()
                ? "-"
                : value;
    }
}