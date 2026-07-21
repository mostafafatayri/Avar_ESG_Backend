package com.fatayriTech.avarESG.service.NotificationService;

import com.fatayriTech.avarESG.enums.ReportingFrequency;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class KpiNotificationPeriodService {

    public ReportingPeriod getPeriodForDate(
            ReportingFrequency frequency,
            LocalDate date
    ) {
        return switch (frequency) {
            case MONTHLY -> {
                YearMonth month =
                        YearMonth.from(date);

                yield new ReportingPeriod(
                        month.toString(),
                        month.atDay(1),
                        month.atEndOfMonth()
                );
            }

            case QUARTERLY -> {
                int quarter =
                        ((date.getMonthValue() - 1)
                                / 3) + 1;

                int firstMonth =
                        ((quarter - 1) * 3) + 1;

                LocalDate start =
                        LocalDate.of(
                                date.getYear(),
                                firstMonth,
                                1
                        );

                yield new ReportingPeriod(
                        date.getYear()
                                + "-Q"
                                + quarter,
                        start,
                        start.plusMonths(3)
                                .minusDays(1)
                );
            }

            case SEMI_ANNUAL -> {
                int half =
                        date.getMonthValue() <= 6
                                ? 1
                                : 2;

                int firstMonth =
                        half == 1 ? 1 : 7;

                LocalDate start =
                        LocalDate.of(
                                date.getYear(),
                                firstMonth,
                                1
                        );

                yield new ReportingPeriod(
                        date.getYear()
                                + "-H"
                                + half,
                        start,
                        start.plusMonths(6)
                                .minusDays(1)
                );
            }

            case ANNUAL -> {
                LocalDate start =
                        LocalDate.of(
                                date.getYear(),
                                1,
                                1
                        );

                yield new ReportingPeriod(
                        String.valueOf(
                                date.getYear()
                        ),
                        start,
                        LocalDate.of(
                                date.getYear(),
                                12,
                                31
                        )
                );
            }
        };
    }

    public ReportingPeriod previousPeriod(
            ReportingFrequency frequency,
            ReportingPeriod current
    ) {
        LocalDate previousDate =
                switch (frequency) {
                    case MONTHLY ->
                            current.startDate()
                                    .minusMonths(1);

                    case QUARTERLY ->
                            current.startDate()
                                    .minusMonths(3);

                    case SEMI_ANNUAL ->
                            current.startDate()
                                    .minusMonths(6);

                    case ANNUAL ->
                            current.startDate()
                                    .minusYears(1);
                };

        return getPeriodForDate(
                frequency,
                previousDate
        );
    }

    public record ReportingPeriod(
            String key,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}