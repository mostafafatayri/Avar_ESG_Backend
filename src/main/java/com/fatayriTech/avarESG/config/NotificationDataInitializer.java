package com.fatayriTech.avarESG.config;

import com.fatayriTech.avarESG.enums.NotificationEventType;
import com.fatayriTech.avarESG.enums.NotificationModule;
import com.fatayriTech.avarESG.enums.NotificationRecipientType;
import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationDataInitializer
        implements CommandLineRunner {

    private final NotificationRuleRepository
            notificationRuleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedKpiDueRule();
        seedKpiOverdueRule();

        seedReadingSubmittedRule();
        seedReadingApprovedRule();
        seedReadingRejectedRule();

        seedKpiSubmittedRule();
        seedKpiApprovedRule();
        seedKpiRejectedRule();
    }

    private void seedKpiDueRule() {
        upsert(
                "KPI_READING_DUE",
                "KPI Reading Due",
                NotificationModule.KPI,
                NotificationEventType.KPI_DUE,
                NotificationRecipientType.KPI_OWNER,
                null,
                5,
                null,
                "0 0 8 * * *",
                false,
                true,
                "KPI reading due: {kpiName}",
                "The KPI {kpiName} ({kpiCode}) requires a reading for "
                        + "{reportingPeriod}. The reporting period ends on "
                        + "{periodEndDate}."
        );
    }

    private void seedKpiOverdueRule() {
        upsert(
                "KPI_READING_OVERDUE",
                "KPI Reading Overdue",
                NotificationModule.KPI,
                NotificationEventType.KPI_OVERDUE,
                NotificationRecipientType.KPI_OWNER,
                null,
                null,
                1,
                "0 0 8 * * *",
                true,
                true,
                "KPI reading overdue: {kpiName}",
                "The KPI {kpiName} ({kpiCode}) has no reading for "
                        + "{reportingPeriod}. The period ended on "
                        + "{periodEndDate}."
        );
    }

    private void seedReadingSubmittedRule() {
        upsert(
                "KPI_READING_SUBMITTED",
                "KPI Reading Submitted",
                NotificationModule.KPI_READING,
                NotificationEventType.READING_SUBMITTED,
                NotificationRecipientType.ROLE,
                "APPROVER",
                null,
                null,
                null,
                true,
                true,
                "KPI reading awaiting approval",
                "A reading for KPI {kpiName} and period "
                        + "{reportingPeriod} was submitted."
        );
    }

    private void seedReadingApprovedRule() {
        upsert(
                "KPI_READING_APPROVED",
                "KPI Reading Approved",
                NotificationModule.KPI_READING,
                NotificationEventType.READING_APPROVED,
                NotificationRecipientType.KPI_OWNER,
                null,
                null,
                null,
                null,
                true,
                true,
                "KPI reading approved",
                "The reading for KPI {kpiName} and period "
                        + "{reportingPeriod} was approved."
        );
    }

    private void seedReadingRejectedRule() {
        upsert(
                "KPI_READING_REJECTED",
                "KPI Reading Rejected",
                NotificationModule.KPI_READING,
                NotificationEventType.READING_REJECTED,
                NotificationRecipientType.KPI_OWNER,
                null,
                null,
                null,
                null,
                true,
                true,
                "KPI reading rejected",
                "The reading for KPI {kpiName} and period "
                        + "{reportingPeriod} was rejected. "
                        + "Reason: {rejectionReason}"
        );
    }

    private void seedKpiSubmittedRule() {
        upsert(
                "KPI_SUBMITTED_FOR_APPROVAL",
                "KPI Submitted for Approval",
                NotificationModule.KPI,
                NotificationEventType.KPI_SUBMITTED_FOR_APPROVAL,
                NotificationRecipientType.ROLE,
                "APPROVER",
                null,
                null,
                null,
                true,
                true,
                "KPI awaiting approval: {kpiName}",
                "The KPI {kpiName} ({kpiCode}) was submitted "
                        + "for approval."
        );
    }

    private void seedKpiApprovedRule() {
        upsert(
                "KPI_APPROVED",
                "KPI Approved",
                NotificationModule.KPI,
                NotificationEventType.KPI_APPROVED,
                NotificationRecipientType.KPI_OWNER,
                null,
                null,
                null,
                null,
                true,
                true,
                "KPI approved: {kpiName}",
                "The KPI {kpiName} ({kpiCode}) was approved."
        );
    }

    private void seedKpiRejectedRule() {
        upsert(
                "KPI_REJECTED",
                "KPI Rejected",
                NotificationModule.KPI,
                NotificationEventType.KPI_REJECTED,
                NotificationRecipientType.KPI_OWNER,
                null,
                null,
                null,
                null,
                true,
                true,
                "KPI rejected: {kpiName}",
                "The KPI {kpiName} ({kpiCode}) was rejected. "
                        + "Reason: {rejectionReason}"
        );
    }

    private void upsert(
            String code,
            String name,
            NotificationModule module,
            NotificationEventType eventType,
            NotificationRecipientType recipientType,
            String recipientRoleCode,
            Integer daysBefore,
            Integer daysAfter,
            String cronExpression,
            boolean channelEmail,
            boolean channelInApp,
            String subject,
            String body
    ) {
        NotificationRule rule =
                notificationRuleRepository
                        .findByCodeIgnoreCase(code)
                        .orElseGet(
                                NotificationRule::new
                        );

        rule.setCode(code);
        rule.setName(name);
        rule.setDescription(name);
        rule.setModule(module);
        rule.setEventType(eventType);
        rule.setRecipientType(recipientType);

        rule.setRecipientRoleCode(
                recipientRoleCode
        );

        rule.setRecipientUserId(null);

        rule.setChannelEmail(channelEmail);
        rule.setChannelInApp(channelInApp);

        rule.setDaysBefore(daysBefore);
        rule.setDaysAfter(daysAfter);
        rule.setCronExpression(
                cronExpression
        );

        rule.setActive(true);
        rule.setSubjectTemplate(subject);
        rule.setBodyTemplate(body);

        notificationRuleRepository.save(rule);
    }
}