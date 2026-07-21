package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.model.NotificationRule;
import com.fatayriTech.avarESG.repository.NotificationRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDynamicSchedulerService {

    private final NotificationRuleRepository
            notificationRuleRepository;

    private final NotificationScannerService
            notificationScannerService;

    private final TaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>>
            scheduledTasks =
            new ConcurrentHashMap<>();

    private final Map<Long, String>
            scheduledCronExpressions =
            new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        refreshSchedules();
    }

    @Scheduled(fixedDelay = 60000)
    public void refreshSchedules() {
        List<NotificationRule> activeRules =
                notificationRuleRepository
                        .findByActiveTrueOrderByCreationDateDesc()
                        .stream()
                        .filter(rule ->
                                rule.getCronExpression() != null
                        )
                        .filter(rule ->
                                !rule.getCronExpression()
                                        .isBlank()
                        )
                        .toList();

        for (NotificationRule rule :
                activeRules) {

            scheduleOrRefresh(rule);
        }

        scheduledTasks.keySet()
                .removeIf(ruleId -> {
                    boolean stillActive =
                            activeRules.stream()
                                    .anyMatch(rule ->
                                            rule.getId()
                                                    .equals(
                                                            ruleId
                                                    )
                                    );

                    if (!stillActive) {
                        cancelSchedule(ruleId);
                        return true;
                    }

                    return false;
                });
    }

    public synchronized void scheduleOrRefresh(
            NotificationRule rule
    ) {
        if (rule == null ||
                rule.getId() == null) {
            return;
        }

        String cron =
                rule.getCronExpression();

        if (!Boolean.TRUE.equals(
                rule.getActive()
        ) ||
                cron == null ||
                cron.isBlank()) {

            cancelSchedule(rule.getId());
            return;
        }

        String existingCron =
                scheduledCronExpressions
                        .get(rule.getId());

        if (cron.equals(existingCron)) {
            return;
        }

        cancelSchedule(rule.getId());

        CronTrigger trigger =
                new CronTrigger(
                        cron,
                        ZoneId.systemDefault()
                );

        ScheduledFuture<?> task =
                taskScheduler.schedule(
                        () ->
                                notificationRuleRepository
                                        .findById(
                                                rule.getId()
                                        )
                                        .filter(activeRule ->
                                                Boolean.TRUE.equals(
                                                        activeRule.getActive()
                                                )
                                        )
                                        .ifPresent(
                                                notificationScannerService
                                                        ::scanRule
                                        ),
                        trigger
                );

        if (task != null) {
            scheduledTasks.put(
                    rule.getId(),
                    task
            );

            scheduledCronExpressions.put(
                    rule.getId(),
                    cron
            );

            log.info(
                    "Notification rule scheduled. ruleId={}, cron={}",
                    rule.getId(),
                    cron
            );
        }
    }

    public synchronized void cancelSchedule(
            Long ruleId
    ) {
        ScheduledFuture<?> task =
                scheduledTasks.remove(ruleId);

        if (task != null) {
            task.cancel(false);
        }

        scheduledCronExpressions.remove(
                ruleId
        );
    }

    public synchronized void cancelAllSchedules() {
        scheduledTasks.forEach(
                (ruleId, task) ->
                        task.cancel(false)
        );

        scheduledTasks.clear();
        scheduledCronExpressions.clear();
    }
}