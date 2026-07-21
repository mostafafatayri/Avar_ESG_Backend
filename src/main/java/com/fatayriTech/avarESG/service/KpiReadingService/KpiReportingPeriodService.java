package com.fatayriTech.avarESG.service.KpiReadingService;

import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.MissingKpiPeriodResponse;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.model.Kpi;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KpiReportingPeriodService {

    private static final Pattern QUARTER_PATTERN =
            Pattern.compile("^(\\d{4})-Q([1-4])$");

    private static final Pattern HALF_YEAR_PATTERN =
            Pattern.compile("^(\\d{4})-H([1-2])$");

    private static final DateTimeFormatter MONTH_LABEL_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "MMMM yyyy",
                    Locale.ENGLISH
            );

    public PeriodDetails parsePeriod(
            Kpi kpi,
            String requestedPeriod
    ) {
        if (requestedPeriod == null ||
                requestedPeriod.isBlank()) {

            throw new BadRequestException(
                    "Reporting period is required"
            );
        }

        String normalized =
                requestedPeriod.trim().toUpperCase();

        String frequency =
                kpi.getReportingFrequency().name();

        return switch (frequency) {
            case "MONTHLY" ->
                    parseMonthly(normalized);

            case "QUARTERLY" ->
                    parseQuarterly(normalized);

            case "SEMI_ANNUAL" ->
                    parseSemiAnnual(normalized);

            case "ANNUAL" ->
                    parseAnnual(normalized);

            default -> throw new BadRequestException(
                    "Unsupported reporting frequency: " +
                            frequency
            );
        };
    }

    public List<MissingKpiPeriodResponse> findMissingPeriods(
            Kpi kpi,
            List<String> existingPeriods
    ) {
        LocalDate effectiveDate = kpi.getEffectiveDate();

        if (effectiveDate == null) {
            return List.of();
        }

        Set<String> existing = new HashSet<>();

        if (existingPeriods != null) {
            existingPeriods.stream()
                    .filter(period -> period != null)
                    .map(String::toUpperCase)
                    .forEach(existing::add);
        }

        LocalDate today = LocalDate.now();

        List<PeriodDetails> expectedPeriods =
                generateExpectedPeriods(
                        kpi,
                        effectiveDate,
                        today
                );

        return expectedPeriods.stream()
                .filter(period ->
                        !existing.contains(period.key())
                )
                .map(period ->
                        MissingKpiPeriodResponse.builder()
                                .period(period.key())
                                .label(period.label())
                                .startDate(period.startDate())
                                .endDate(period.endDate())
                                .overdue(
                                        period.endDate()
                                                .isBefore(today)
                                )
                                .currentPeriod(
                                        !today.isBefore(
                                                period.startDate()
                                        ) &&
                                                !today.isAfter(
                                                        period.endDate()
                                                )
                                )
                                .build()
                )
                .toList();
    }

    private List<PeriodDetails> generateExpectedPeriods(
            Kpi kpi,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String frequency =
                kpi.getReportingFrequency().name();

        return switch (frequency) {
            case "MONTHLY" ->
                    generateMonthlyPeriods(
                            startDate,
                            endDate
                    );

            case "QUARTERLY" ->
                    generateQuarterlyPeriods(
                            startDate,
                            endDate
                    );

            case "SEMI_ANNUAL" ->
                    generateSemiAnnualPeriods(
                            startDate,
                            endDate
                    );

            case "ANNUAL" ->
                    generateAnnualPeriods(
                            startDate,
                            endDate
                    );

            default -> List.of();
        };
    }

    private PeriodDetails parseMonthly(
            String period
    ) {
        try {
            YearMonth yearMonth =
                    YearMonth.parse(period);

            LocalDate start =
                    yearMonth.atDay(1);

            LocalDate end =
                    yearMonth.atEndOfMonth();

            return new PeriodDetails(
                    yearMonth.toString(),
                    yearMonth.format(
                            MONTH_LABEL_FORMATTER
                    ),
                    start,
                    end
            );
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(
                    "Monthly reporting period must use " +
                            "the format YYYY-MM, for example 2026-07"
            );
        }
    }

    private PeriodDetails parseQuarterly(
            String period
    ) {
        Matcher matcher =
                QUARTER_PATTERN.matcher(period);

        if (!matcher.matches()) {
            throw new BadRequestException(
                    "Quarterly reporting period must use " +
                            "the format YYYY-Q1, for example 2026-Q2"
            );
        }

        int year =
                Integer.parseInt(matcher.group(1));

        int quarter =
                Integer.parseInt(matcher.group(2));

        int firstMonth =
                ((quarter - 1) * 3) + 1;

        LocalDate start =
                LocalDate.of(
                        year,
                        firstMonth,
                        1
                );

        LocalDate end =
                start.plusMonths(3)
                        .minusDays(1);

        return new PeriodDetails(
                year + "-Q" + quarter,
                "Q" + quarter + " " + year,
                start,
                end
        );
    }

    private PeriodDetails parseSemiAnnual(
            String period
    ) {
        Matcher matcher =
                HALF_YEAR_PATTERN.matcher(period);

        if (!matcher.matches()) {
            throw new BadRequestException(
                    "Semi-annual reporting period must " +
                            "use YYYY-H1 or YYYY-H2"
            );
        }

        int year =
                Integer.parseInt(matcher.group(1));

        int half =
                Integer.parseInt(matcher.group(2));

        int firstMonth =
                half == 1 ? 1 : 7;

        LocalDate start =
                LocalDate.of(
                        year,
                        firstMonth,
                        1
                );

        LocalDate end =
                start.plusMonths(6)
                        .minusDays(1);

        return new PeriodDetails(
                year + "-H" + half,
                "H" + half + " " + year,
                start,
                end
        );
    }

    private PeriodDetails parseAnnual(
            String period
    ) {
        try {
            Year year = Year.parse(period);

            return new PeriodDetails(
                    year.toString(),
                    year.toString(),
                    year.atDay(1),
                    year.atMonth(12)
                            .atEndOfMonth()
            );
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(
                    "Annual reporting period must use " +
                            "the format YYYY, for example 2026"
            );
        }
    }

    private List<PeriodDetails> generateMonthlyPeriods(
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PeriodDetails> periods =
                new ArrayList<>();

        YearMonth current =
                YearMonth.from(startDate);

        YearMonth last =
                YearMonth.from(endDate);

        while (!current.isAfter(last)) {
            periods.add(
                    parseMonthly(current.toString())
            );

            current = current.plusMonths(1);
        }

        return periods;
    }

    private List<PeriodDetails> generateQuarterlyPeriods(
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PeriodDetails> periods =
                new ArrayList<>();

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        for (int year = startYear;
             year <= endYear;
             year++) {

            for (int quarter = 1;
                 quarter <= 4;
                 quarter++) {

                PeriodDetails period =
                        parseQuarterly(
                                year + "-Q" + quarter
                        );

                if (period.endDate()
                        .isBefore(startDate)) {
                    continue;
                }

                if (period.startDate()
                        .isAfter(endDate)) {
                    continue;
                }

                periods.add(period);
            }
        }

        return periods;
    }

    private List<PeriodDetails> generateSemiAnnualPeriods(
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PeriodDetails> periods =
                new ArrayList<>();

        for (int year = startDate.getYear();
             year <= endDate.getYear();
             year++) {

            for (int half = 1;
                 half <= 2;
                 half++) {

                PeriodDetails period =
                        parseSemiAnnual(
                                year + "-H" + half
                        );

                if (period.endDate()
                        .isBefore(startDate)) {
                    continue;
                }

                if (period.startDate()
                        .isAfter(endDate)) {
                    continue;
                }

                periods.add(period);
            }
        }

        return periods;
    }

    private List<PeriodDetails> generateAnnualPeriods(
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PeriodDetails> periods =
                new ArrayList<>();

        for (int year = startDate.getYear();
             year <= endDate.getYear();
             year++) {

            periods.add(
                    parseAnnual(String.valueOf(year))
            );
        }

        return periods;
    }

    public record PeriodDetails(
            String key,
            String label,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}