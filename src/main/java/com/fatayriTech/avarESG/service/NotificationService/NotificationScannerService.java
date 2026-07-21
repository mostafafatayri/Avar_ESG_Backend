package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.enums.*;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.KpiReadingRepository;
import com.fatayriTech.avarESG.repository.KpiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScannerService {

    private final KpiRepository kpiRepository;

    private final KpiReadingRepository
            kpiReadingRepository;

    private final NotificationRecipientResolver
            recipientResolver;

    private final NotificationDispatchService
            dispatchService;

    private final NotificationTemplateService
            templateService;

    private final KpiNotificationPeriodService
            periodService;

    @Transactional
    public void scanRule(
            NotificationRule rule
    ) {
        if (!Boolean.TRUE.equals(
                rule.getActive()
        )) {
            return;
        }

        switch (rule.getEventType()) {
            case KPI_DUE ->
                    scanDueKpis(rule);

            case KPI_OVERDUE ->
                    scanOverdueKpis(rule);

            /*
             * These event types are sent immediately
             * through NotificationTriggerService.
             */
            case KPI_SUBMITTED_FOR_APPROVAL,
                 KPI_APPROVED,
                 KPI_REJECTED,
                 READING_SUBMITTED,
                 READING_APPROVED,
                 READING_REJECTED,
                 GENERAL -> {
            }
        }
    }

    private void scanDueKpis(
            NotificationRule rule
    ) {
        LocalDate today =
                LocalDate.now();

        List<Kpi> kpis =
                findEligibleKpis();

        for (Kpi kpi : kpis) {
            if (kpi.getReportingFrequency() == null) {
                continue;
            }

            KpiNotificationPeriodService
                    .ReportingPeriod period =
                    periodService.getPeriodForDate(
                            kpi.getReportingFrequency(),
                            today
                    );

            if (!isKpiEffectiveForPeriod(
                    kpi,
                    period
            )) {
                continue;
            }

            int daysBefore =
                    rule.getDaysBefore() == null
                            ? 5
                            : rule.getDaysBefore();

            LocalDate reminderDate =
                    period.endDate()
                            .minusDays(daysBefore);

            if (!today.equals(reminderDate)) {
                continue;
            }

            boolean readingExists =
                    readingExists(
                            kpi.getId(),
                            period.key()
                    );

            if (readingExists) {
                continue;
            }

            sendKpiPeriodNotification(
                    rule,
                    kpi,
                    period,
                    "KPI_DUE",
                    "KPI reading due",
                    "The KPI {kpiName} requires a reading for {reportingPeriod}. "
                            + "The reporting period ends on {periodEndDate}."
            );
        }
    }

    private void scanOverdueKpis(
            NotificationRule rule
    ) {
        LocalDate today =
                LocalDate.now();

        List<Kpi> kpis =
                findEligibleKpis();

        for (Kpi kpi : kpis) {
            if (kpi.getReportingFrequency() == null) {
                continue;
            }

            KpiNotificationPeriodService
                    .ReportingPeriod currentPeriod =
                    periodService.getPeriodForDate(
                            kpi.getReportingFrequency(),
                            today
                    );

            KpiNotificationPeriodService
                    .ReportingPeriod previousPeriod =
                    periodService.previousPeriod(
                            kpi.getReportingFrequency(),
                            currentPeriod
                    );

            if (!isKpiEffectiveForPeriod(
                    kpi,
                    previousPeriod
            )) {
                continue;
            }

            int daysAfter =
                    rule.getDaysAfter() == null
                            ? 1
                            : rule.getDaysAfter();

            LocalDate notificationDate =
                    previousPeriod.endDate()
                            .plusDays(daysAfter);

            if (!today.equals(notificationDate)) {
                continue;
            }

            boolean readingExists =
                    readingExists(
                            kpi.getId(),
                            previousPeriod.key()
                    );

            if (readingExists) {
                continue;
            }

            sendKpiPeriodNotification(
                    rule,
                    kpi,
                    previousPeriod,
                    "KPI_OVERDUE",
                    "KPI reading overdue",
                    "The KPI {kpiName} has no reading for {reportingPeriod}. "
                            + "The reporting period ended on {periodEndDate}."
            );
        }
    }

    private List<Kpi> findEligibleKpis() {
        return kpiRepository.findAll()
                .stream()
                .filter(kpi ->
                        kpi.getStatus() ==
                                KpiStatus.ACTIVE
                )
                .toList();
    }

    private boolean readingExists(
            Long kpiId,
            String reportingPeriod
    ) {
        return kpiReadingRepository
                .existsByKpiIdAndReportingPeriodIgnoreCaseAndApprovalStatusNot(
                        kpiId,
                        reportingPeriod,
                        KpiReadingApprovalStatus.REJECTED
                );
    }

    private boolean isKpiEffectiveForPeriod(
            Kpi kpi,
            KpiNotificationPeriodService
                    .ReportingPeriod period
    ) {
        if (kpi.getEffectiveDate() == null) {
            return true;
        }

        return !period.endDate()
                .isBefore(
                        kpi.getEffectiveDate()
                );
    }

    private void sendKpiPeriodNotification(
            NotificationRule rule,
            Kpi kpi,
            KpiNotificationPeriodService
                    .ReportingPeriod period,
            String eventPrefix,
            String fallbackSubject,
            String fallbackBody
    ) {
        List<AppUser> recipients =
                recipientResolver
                        .resolveRecipients(
                                rule,
                                kpi
                        );

        Map<String, String> variables =
                buildVariables(
                        kpi,
                        period
                );

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
                    eventPrefix
                            + ":KPI:"
                            + kpi.getId()
                            + ":PERIOD:"
                            + period.key()
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
                    period.key(),
                    subject,
                    body,
                    "/kpis/" + kpi.getId()
            );
        }
    }

    private Map<String, String> buildVariables(
            Kpi kpi,
            KpiNotificationPeriodService
                    .ReportingPeriod period
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
                        "reportingPeriod",
                        safe(period.key())
                ),
                Map.entry(
                        "periodStartDate",
                        period.startDate().toString()
                ),
                Map.entry(
                        "periodEndDate",
                        period.endDate().toString()
                ),
                Map.entry(
                        "frequency",
                        kpi.getReportingFrequency()
                                .name()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                ),
                Map.entry(
                        "ownerName",
                        kpi.getResponsibleOwner() != null
                                ? safe(
                                kpi.getResponsibleOwner()
                                        .getFullName()
                        )
                                : "-"
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