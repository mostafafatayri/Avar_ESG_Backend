package com.fatayriTech.avarESG.service.CarbonService;

import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.enums.EsgCategory;
import com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus;
import com.fatayriTech.avarESG.enums.KpiReadingSource;
import com.fatayriTech.avarESG.enums.KpiStatus;
import com.fatayriTech.avarESG.enums.ReportingFrequency;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.CarbonEmission;
import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.KpiReading;
import com.fatayriTech.avarESG.repository.CarbonEmissionRepository;
import com.fatayriTech.avarESG.repository.KpiReadingRepository;
import com.fatayriTech.avarESG.repository.KpiRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarbonKpiIntegrationService {

    private final CarbonEmissionRepository
            carbonEmissionRepository;

    private final KpiRepository
            kpiRepository;

    private final KpiReadingRepository
            kpiReadingRepository;

    private final UserRepository
            userRepository;

    /**
     * Synchronizes one approved Carbon emission with
     * all active KPIs having:
     *
     * 1. The same site/building
     * 2. A Carbon emissions category matching its scope
     * 3. A compatible Carbon unit
     * 4. A reporting period containing the Carbon month
     */
    @Transactional
    public void synchronize(
            CarbonEmission emission
    ) {
        if (!isSynchronizableEmission(emission)) {
            return;
        }

        /*
         * Carbon scope is no longer read from a manually
         * configured KPI carbonScope field.
         *
         * We first retrieve active KPIs for the building,
         * then derive each KPI's Carbon scope from its
         * ESG category.
         */
        List<Kpi> siteKpis =
                kpiRepository
                        .findBySiteIdAndStatus(
                                emission.getFacilityId(),
                                KpiStatus.ACTIVE
                        );

        List<Kpi> matchingKpis =
                siteKpis.stream()
                        .filter(kpi ->
                                emission.getScope()
                                        == resolveCarbonScope(
                                        kpi.getCategory()
                                )
                        )
                        .toList();

        for (Kpi kpi : matchingKpis) {
            synchronizeKpiPeriod(
                    kpi,
                    emission.getReportingPeriod()
            );
        }
    }

    /**
     * Determines whether the Carbon record is eligible
     * to affect KPI readings.
     */
    private boolean isSynchronizableEmission(
            CarbonEmission emission
    ) {
        if (emission == null) {
            return false;
        }

        if (emission.getScope() == null
                || emission.getFacilityId() == null
                || emission.getReportingPeriod() == null) {

            return false;
        }

        /*
         * Memo records remain visible in Carbon reporting
         * but must never affect Carbon-linked KPI readings.
         */
        if (emission.isMemo()) {
            return false;
        }

        /*
         * The calling Carbon service should already call this
         * only after approval, but this check protects the
         * integration service from incorrect direct calls.
         */
        return emission.getApprovalStatus()
                != null
                && emission.getApprovalStatus()
                .name()
                .equals("APPROVED");
    }

    /**
     * Creates or refreshes the KPI reading for the period
     * containing the approved Carbon month.
     */
    private void synchronizeKpiPeriod(
            Kpi kpi,
            LocalDate carbonMonth
    ) {
        CarbonScope carbonScope =
                resolveCarbonScope(
                        kpi.getCategory()
                );

        /*
         * Non-Carbon categories must never receive
         * Carbon values.
         */
        if (carbonScope == null) {
            return;
        }

        /*
         * A building-specific Carbon KPI is required
         * for the current matching model.
         */
        if (kpi.getSite() == null
                || kpi.getSite().getId() == null) {

            return;
        }

        PeriodDetails period =
                resolvePeriod(
                        kpi.getReportingFrequency(),
                        carbonMonth
                );

        CarbonEmissionUnit unit =
                resolveCarbonUnit(
                        kpi.getUnitOfMeasure()
                );

        /*
         * This prevents mixing kilograms with tonnes.
         */
        if (unit == null) {
            return;
        }

        BigDecimal total =
                calculatePeriodTotal(
                        kpi,
                        carbonScope,
                        period,
                        unit
                );

        KpiReading existing =
                kpiReadingRepository
                        .findByKpiIdAndReportingPeriodIgnoreCase(
                                kpi.getId(),
                                period.reportingPeriod()
                        )
                        .orElse(null);

        /*
         * When the Carbon total becomes zero, remove an
         * automatically generated Carbon reading.
         *
         * Manual readings are not deleted here.
         */
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            if (existing != null
                    && isCarbonReading(existing)) {

                kpiReadingRepository.delete(
                        existing
                );
            }

            return;
        }

        AppUser systemSubmitter =
                resolveSystemSubmitter(
                        kpi
                );

        /*
         * No reading exists for this KPI and period:
         * create an automatically approved Carbon reading.
         */
        if (existing == null) {
            KpiReading reading =
                    KpiReading.builder()
                            .kpi(kpi)
                            .reportingPeriod(
                                    period.reportingPeriod()
                            )
                            .periodStartDate(
                                    period.startDate()
                            )
                            .periodEndDate(
                                    period.endDate()
                            )
                            .actualValue(total)
                            .source(
                                    KpiReadingSource.CARBON_IMPORT
                            )
                            .submittedBy(
                                    systemSubmitter
                            )
                            .submissionDate(
                                    LocalDateTime.now()
                            )
                            .approvalStatus(
                                    KpiReadingApprovalStatus.APPROVED
                            )
                            .reviewedBy(
                                    systemSubmitter
                            )
                            .reviewDate(
                                    LocalDateTime.now()
                            )
                            .rejectionReason(null)
                            .build();

            kpiReadingRepository.save(
                    reading
            );

            return;
        }

        /*
         * For a Scope 1, Scope 2, or Scope 3 KPI,
         * Carbon is the authoritative source.
         *
         * Therefore, even if the period originally had a
         * manual reading, it is converted into a Carbon
         * reading and updated with the calculated value.
         *
         * This prevents Carbon and KPI values from differing.
         */
        existing.setActualValue(total);

        existing.setSource(
                KpiReadingSource.CARBON_IMPORT
        );

        existing.setPeriodStartDate(
                period.startDate()
        );

        existing.setPeriodEndDate(
                period.endDate()
        );

        existing.setSubmittedBy(
                systemSubmitter
        );

        existing.setSubmissionDate(
                LocalDateTime.now()
        );

        existing.setApprovalStatus(
                KpiReadingApprovalStatus.APPROVED
        );

        existing.setReviewedBy(
                systemSubmitter
        );

        existing.setReviewDate(
                LocalDateTime.now()
        );

        existing.setRejectionReason(null);

        kpiReadingRepository.save(
                existing
        );
    }

    /**
     * Recalculates the complete KPI reporting period from
     * approved, non-memo Carbon records.
     *
     * It does not add only the latest record because that
     * could cause duplicate totals when synchronization is
     * executed more than once.
     */
    private BigDecimal calculatePeriodTotal(
            Kpi kpi,
            CarbonScope carbonScope,
            PeriodDetails period,
            CarbonEmissionUnit unit
    ) {
        BigDecimal total =
                BigDecimal.ZERO;

        YearMonth month =
                YearMonth.from(
                        period.startDate()
                );

        YearMonth finalMonth =
                YearMonth.from(
                        period.endDate()
                );

        while (!month.isAfter(finalMonth)) {
            BigDecimal monthlyTotal =
                    carbonEmissionRepository
                            .sumApprovedNonMemoEmissions(
                                    carbonScope,
                                    kpi.getSite().getId(),
                                    month.atDay(1),
                                    unit
                            );

            if (monthlyTotal != null) {
                total =
                        total.add(
                                monthlyTotal
                        );
            }

            month =
                    month.plusMonths(1);
        }

        return total;
    }

    /**
     * Automatically maps an ESG category to its Carbon scope.
     *
     * Update the enum constants below only when the exact
     * names in EsgCategory are different.
     */
    private CarbonScope resolveCarbonScope(
            EsgCategory category
    ) {
        if (category == null) {
            return null;
        }

        return switch (category) {
            case SCOPE_1_EMISSIONS ->
                    CarbonScope.SCOPE_1;

            case SCOPE_2_EMISSIONS ->
                    CarbonScope.SCOPE_2;

            case SCOPE_3_EMISSIONS ->
                    CarbonScope.SCOPE_3;

            default -> null;
        };
    }

    /**
     * Converts the Carbon month into the KPI's configured
     * reporting period.
     */
    private PeriodDetails resolvePeriod(
            ReportingFrequency frequency,
            LocalDate carbonMonth
    ) {
        if (frequency == null) {
            throw new IllegalStateException(
                    "KPI reporting frequency is required for Carbon synchronization"
            );
        }

        YearMonth month =
                YearMonth.from(
                        carbonMonth
                );

        int year =
                month.getYear();

        return switch (frequency) {
            case MONTHLY ->
                    new PeriodDetails(
                            month.toString(),
                            month.atDay(1),
                            month.atEndOfMonth()
                    );

            case QUARTERLY -> {
                int quarter =
                        ((month.getMonthValue() - 1)
                                / 3) + 1;

                int firstMonth =
                        ((quarter - 1) * 3) + 1;

                YearMonth start =
                        YearMonth.of(
                                year,
                                firstMonth
                        );

                YearMonth end =
                        start.plusMonths(2);

                yield new PeriodDetails(
                        year + "-Q" + quarter,
                        start.atDay(1),
                        end.atEndOfMonth()
                );
            }

            case SEMI_ANNUAL -> {
                boolean firstHalf =
                        month.getMonthValue() <= 6;

                YearMonth start =
                        YearMonth.of(
                                year,
                                firstHalf ? 1 : 7
                        );

                YearMonth end =
                        start.plusMonths(5);

                yield new PeriodDetails(
                        year
                                + (firstHalf
                                ? "-H1"
                                : "-H2"),
                        start.atDay(1),
                        end.atEndOfMonth()
                );
            }

            case ANNUAL -> {
                YearMonth start =
                        YearMonth.of(
                                year,
                                1
                        );

                YearMonth end =
                        YearMonth.of(
                                year,
                                12
                        );

                yield new PeriodDetails(
                        String.valueOf(year),
                        start.atDay(1),
                        end.atEndOfMonth()
                );
            }
        };
    }

    /**
     * Converts the KPI's text unit into the Carbon enum unit.
     */
    private CarbonEmissionUnit resolveCarbonUnit(
            String unitOfMeasure
    ) {
        if (unitOfMeasure == null
                || unitOfMeasure.isBlank()) {

            return null;
        }

        String normalized =
                unitOfMeasure
                        .trim()
                        .toUpperCase()
                        .replace("₂", "2")
                        .replace(" ", "")
                        .replace("_", "")
                        .replace("-", "");

        if (normalized.equals("KGCO2E")) {
            return CarbonEmissionUnit.KG_CO2E;
        }

        if (normalized.equals("TCO2E")
                || normalized.equals("TONCO2E")
                || normalized.equals("TONSCO2E")
                || normalized.equals("TONNECO2E")
                || normalized.equals("TONNESCO2E")) {

            return CarbonEmissionUnit.T_CO2E;
        }

        return null;
    }

    /**
     * Uses the KPI owner as the submitter of an automatically
     * generated KPI reading because submittedBy is mandatory.
     */
    private AppUser resolveSystemSubmitter(
            Kpi kpi
    ) {
        if (kpi.getResponsibleOwner() == null
                || kpi.getResponsibleOwner().getId() == null) {

            throw new IllegalStateException(
                    "KPI responsible owner is required for Carbon synchronization"
            );
        }

        return userRepository
                .findById(
                        kpi.getResponsibleOwner()
                                .getId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "KPI responsible owner was not found"
                        )
                );
    }

    private boolean isCarbonReading(
            KpiReading reading
    ) {
        return reading != null
                && reading.getSource()
                == KpiReadingSource.CARBON_IMPORT;
    }

    private record PeriodDetails(
            String reportingPeriod,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}