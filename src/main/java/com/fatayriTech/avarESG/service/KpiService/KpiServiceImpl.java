package com.fatayriTech.avarESG.service.KpiService;

import com.fatayriTech.avarESG.config.AppLoggingProperties;
import com.fatayriTech.avarESG.dto.request.KpiRequests.CreateKpiRequest;
import com.fatayriTech.avarESG.dto.request.KpiRequests.RejectKpiRequest;
import com.fatayriTech.avarESG.dto.request.KpiRequests.UpdateKpiRequest;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiResponse;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiSummaryResponse;
import com.fatayriTech.avarESG.enums.EsgCategory;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.KpiCompletionStatus;
import com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus;
import com.fatayriTech.avarESG.enums.KpiStatus;
import com.fatayriTech.avarESG.enums.ReportingFrequency;
import com.fatayriTech.avarESG.enums.TargetDirection;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.KpiReading;
import com.fatayriTech.avarESG.model.Site;
import com.fatayriTech.avarESG.repository.KpiReadingRepository;
import com.fatayriTech.avarESG.repository.KpiRepository;
import com.fatayriTech.avarESG.repository.SiteRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.service.NotificationService.NotificationTriggerService;
import com.fatayriTech.avarESG.service.SecurityService.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KpiServiceImpl implements KpiService {

    /*
     * When the reporting period is within this number of days
     * from its end date and no reading exists, the KPI becomes DUE.
     */
    private static final int COMPLETION_DUE_WARNING_DAYS = 5;

    private final KpiRepository kpiRepository;
    private final KpiReadingRepository kpiReadingRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AppLoggingProperties loggingProperties;
    private final NotificationTriggerService
            notificationTriggerService;
    /*
     * ============================================================
     * CREATE KPI
     * ============================================================
     */

    @Override
    public KpiResponse createKpi(
            CreateKpiRequest request
    ) {
        validateCategoryMatchesPillar(
                request.getPillar(),
                request.getCategory()
        );

        validateTargetConfiguration(
                request.getBaselineValue(),
                request.getTargetValue(),
                request.getTargetDirection()
        );

        String normalizedCode =
                normalizeCode(request.getCode());

        if (kpiRepository.existsByCodeIgnoreCase(
                normalizedCode
        )) {
            throw new BadRequestException(
                    "KPI code already exists: "
                            + normalizedCode
            );
        }

        Site site =
                resolveSite(request.getSiteId());

        AppUser responsibleOwner =
                resolveUser(
                        request.getResponsibleOwnerId(),
                        "Responsible owner"
                );

        KpiStatus initialStatus =
                request.isApprovalRequired()
                        ? KpiStatus.DRAFT
                        : KpiStatus.ACTIVE;

        Kpi kpi = Kpi.builder()
                .name(request.getName().trim())
                .code(normalizedCode)
                .pillar(request.getPillar())
                .category(request.getCategory())
                .site(site)
                .description(request.getDescription())
                .unitOfMeasure(
                        request.getUnitOfMeasure().trim()
                )
                .reportingFrequency(
                        request.getReportingFrequency()
                )
                .baselineValue(
                        request.getBaselineValue()
                )
                .targetValue(
                        request.getTargetValue()
                )
                .targetDirection(
                        request.getTargetDirection()
                )
                .dataSource(
                        request.getDataSource()
                )
                .effectiveDate(
                        request.getEffectiveDate()
                )
                .responsibleOwner(
                        responsibleOwner
                )
                .frameworks(
                        request.getFrameworks() == null
                                ? Set.of()
                                : request.getFrameworks()
                )
                .approvalRequired(
                        request.isApprovalRequired()
                )
                .status(initialStatus)
                .build();

        Kpi savedKpi =
                kpiRepository.save(kpi);

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Created KPI code={} status={} approvalRequired={}",
                    savedKpi.getCode(),
                    savedKpi.getStatus(),
                    savedKpi.isApprovalRequired()
            );
        }

        return mapKpiToResponse(savedKpi);
    }

    /*
     * ============================================================
     * GET KPIs
     * ============================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<KpiResponse> getAllKpis(
            EsgPillar pillar,
            EsgCategory category,
            KpiStatus status
    ) {
        List<Kpi> kpis;

        if (category != null) {
            kpis =
                    kpiRepository
                            .findByCategoryOrderByCreationDateDesc(
                                    category
                            );
        } else if (pillar != null) {
            kpis =
                    kpiRepository
                            .findByPillarOrderByCreationDateDesc(
                                    pillar
                            );
        } else if (status != null) {
            kpis =
                    kpiRepository
                            .findByStatusOrderByCreationDateDesc(
                                    status
                            );
        } else {
            kpis =
                    kpiRepository
                            .findAllByOrderByCreationDateDesc();
        }

        return kpis.stream()
                .map(this::mapKpiToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KpiResponse getKpiById(
            Long id
    ) {
        return mapKpiToResponse(
                getKpiEntity(id)
        );
    }

    /*
     * ============================================================
     * UPDATE KPI
     * ============================================================
     */

    @Override
    public KpiResponse updateKpi(
            Long id,
            UpdateKpiRequest request
    ) {
        Kpi kpi =
                getKpiEntity(id);

        validateEditableStatus(kpi);

        if (request.getCode() != null) {
            String normalizedCode =
                    normalizeCode(request.getCode());

            if (kpiRepository
                    .existsByCodeIgnoreCaseAndIdNot(
                            normalizedCode,
                            id
                    )) {
                throw new BadRequestException(
                        "KPI code already exists: "
                                + normalizedCode
                );
            }

            kpi.setCode(normalizedCode);
        }

        EsgPillar updatedPillar =
                request.getPillar() != null
                        ? request.getPillar()
                        : kpi.getPillar();

        EsgCategory updatedCategory =
                request.getCategory() != null
                        ? request.getCategory()
                        : kpi.getCategory();

        validateCategoryMatchesPillar(
                updatedPillar,
                updatedCategory
        );

        BigDecimal updatedBaseline =
                request.getBaselineValue() != null
                        ? request.getBaselineValue()
                        : kpi.getBaselineValue();

        BigDecimal updatedTarget =
                request.getTargetValue() != null
                        ? request.getTargetValue()
                        : kpi.getTargetValue();

        TargetDirection updatedDirection =
                request.getTargetDirection() != null
                        ? request.getTargetDirection()
                        : kpi.getTargetDirection();

        validateTargetConfiguration(
                updatedBaseline,
                updatedTarget,
                updatedDirection
        );

        if (request.getName() != null) {
            kpi.setName(
                    request.getName().trim()
            );
        }

        kpi.setPillar(updatedPillar);
        kpi.setCategory(updatedCategory);

        if (Boolean.TRUE.equals(
                request.getSiteSelectionProvided()
        )) {
            kpi.setSite(
                    resolveSite(request.getSiteId())
            );
        }

        if (request.getDescription() != null) {
            kpi.setDescription(
                    request.getDescription()
            );
        }

        if (request.getUnitOfMeasure() != null) {
            kpi.setUnitOfMeasure(
                    request.getUnitOfMeasure().trim()
            );
        }

        if (request.getReportingFrequency() != null) {
            kpi.setReportingFrequency(
                    request.getReportingFrequency()
            );
        }

        kpi.setBaselineValue(updatedBaseline);
        kpi.setTargetValue(updatedTarget);
        kpi.setTargetDirection(updatedDirection);

        if (request.getDataSource() != null) {
            kpi.setDataSource(
                    request.getDataSource()
            );
        }

        if (request.getEffectiveDate() != null) {
            kpi.setEffectiveDate(
                    request.getEffectiveDate()
            );
        }

        if (request.getResponsibleOwnerId() != null) {
            kpi.setResponsibleOwner(
                    resolveUser(
                            request.getResponsibleOwnerId(),
                            "Responsible owner"
                    )
            );
        }

        if (request.getFrameworks() != null) {
            kpi.setFrameworks(
                    request.getFrameworks()
            );
        }

        if (request.getApprovalRequired() != null) {
            boolean approvalRequired =
                    request.getApprovalRequired();

            kpi.setApprovalRequired(
                    approvalRequired
            );

            if (!approvalRequired
                    && kpi.getStatus()
                    == KpiStatus.DRAFT) {
                kpi.setStatus(
                        KpiStatus.ACTIVE
                );
            }

            if (approvalRequired
                    && kpi.getStatus()
                    == KpiStatus.ACTIVE) {
                kpi.setStatus(
                        KpiStatus.DRAFT
                );
            }
        }

        return mapKpiToResponse(
                kpiRepository.save(kpi)
        );
    }

    /*
     * ============================================================
     * DELETE KPI
     * ============================================================
     */

    @Override
    public void deleteKpi(
            Long id
    ) {
        Kpi kpi =
                getKpiEntity(id);

        if (kpi.getStatus()
                == KpiStatus.PENDING) {
            throw new BadRequestException(
                    "A pending KPI cannot be deleted"
            );
        }

        kpiReadingRepository.deleteByKpiId(id);
        kpiRepository.delete(kpi);

        if (loggingProperties.isVerbose()) {
            log.warn(
                    "Deleted KPI id={} code={}",
                    id,
                    kpi.getCode()
            );
        }
    }

    /*
     * ============================================================
     * KPI APPROVAL WORKFLOW
     * ============================================================
     */

    @Override
    public KpiResponse submitForApproval(
            Long id
    ) {
        Kpi kpi =
                getKpiEntity(id);

        if (!kpi.isApprovalRequired()) {
            throw new BadRequestException(
                    "This KPI does not require approval"
            );
        }

        if (kpi.getStatus() != KpiStatus.DRAFT
                && kpi.getStatus()
                != KpiStatus.REJECTED) {
            throw new BadRequestException(
                    "Only draft or rejected KPIs can be submitted"
            );
        }

        kpi.setStatus(
                KpiStatus.PENDING
        );

        kpi.setSubmittedForApprovalAt(
                LocalDateTime.now()
        );

        kpi.setSubmittedByUserId(
                currentUserService.getUserId()
        );

        kpi.setRejectedAt(null);
        kpi.setRejectedByUserId(null);
        kpi.setRejectionReason(null);

        Kpi savedKpi =
                kpiRepository.save(kpi);

        notificationTriggerService
                .triggerKpiSubmitted(savedKpi);

        return mapKpiToResponse(savedKpi);
    }

    @Override
    public KpiResponse approveKpi(
            Long id
    ) {
        Kpi kpi =
                getKpiEntity(id);

        if (kpi.getStatus()
                != KpiStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending KPIs can be approved"
            );
        }

        kpi.setStatus(
                KpiStatus.ACTIVE
        );

        kpi.setApprovedAt(
                LocalDateTime.now()
        );

        kpi.setApprovedByUserId(
                currentUserService.getUserId()
        );

        kpi.setRejectedAt(null);
        kpi.setRejectedByUserId(null);
        kpi.setRejectionReason(null);

        Kpi savedKpi =
                kpiRepository.save(kpi);

        notificationTriggerService
                .triggerKpiApproved(savedKpi);

        return mapKpiToResponse(savedKpi);
    }

    @Override
    public KpiResponse rejectKpi(
            Long id,
            RejectKpiRequest request
    ) {
        Kpi kpi =
                getKpiEntity(id);

        if (kpi.getStatus()
                != KpiStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending KPIs can be rejected"
            );
        }

        kpi.setStatus(
                KpiStatus.REJECTED
        );

        kpi.setRejectedAt(
                LocalDateTime.now()
        );

        kpi.setRejectedByUserId(
                currentUserService.getUserId()
        );

        kpi.setRejectionReason(
                request.getReason().trim()
        );

        Kpi savedKpi =
                kpiRepository.save(kpi);

        notificationTriggerService
                .triggerKpiRejected(savedKpi);

        return mapKpiToResponse(savedKpi);
    }

    @Override
    public KpiResponse archiveKpi(
            Long id
    ) {
        Kpi kpi =
                getKpiEntity(id);

        if (kpi.getStatus()
                == KpiStatus.PENDING) {
            throw new BadRequestException(
                    "A pending KPI cannot be archived"
            );
        }

        kpi.setStatus(
                KpiStatus.ARCHIVED
        );

        return mapKpiToResponse(
                kpiRepository.save(kpi)
        );
    }

    /*
     * ============================================================
     * KPI SUMMARY
     * ============================================================
     */

    @Override
    @Transactional(readOnly = true)
    public KpiSummaryResponse getKpiSummary() {
        List<KpiResponse> responses =
                kpiRepository.findAll()
                        .stream()
                        .map(this::mapKpiToResponse)
                        .toList();

        /*
         * Only fields known to already exist in your summary DTO
         * are used here. Completion-specific summary fields can
         * be added later.
         */
        return KpiSummaryResponse.builder()
                .totalKpis(responses.size())
                .activeKpis(
                        countStatus(
                                responses,
                                KpiStatus.ACTIVE
                        )
                )
                .draftKpis(
                        countStatus(
                                responses,
                                KpiStatus.DRAFT
                        )
                )
                .pendingKpis(
                        countStatus(
                                responses,
                                KpiStatus.PENDING
                        )
                )
                .build();
    }

    /*
     * ============================================================
     * CATEGORY LOOKUP
     * ============================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<EsgCategory> getCategoriesByPillar(
            EsgPillar pillar
    ) {
        if (pillar == null) {
            throw new BadRequestException(
                    "ESG pillar is required"
            );
        }

        return java.util.Arrays
                .stream(EsgCategory.values())
                .filter(category ->
                        category.getPillar() == pillar
                )
                .sorted(
                        Comparator.comparing(
                                Enum::name
                        )
                )
                .toList();
    }

    /*
     * ============================================================
     * RESPONSE MAPPING
     * ============================================================
     */

    private KpiResponse mapKpiToResponse(
            Kpi kpi
    ) {
        KpiReading latestApprovedReading =
                kpiReadingRepository
                        .findLatestApprovedReading(
                                kpi.getId()
                        )
                        .orElse(null);

        BigDecimal latestActualValue =
                latestApprovedReading != null
                        ? latestApprovedReading
                        .getActualValue()
                        : null;

        LocalDate latestReadingDate =
                latestApprovedReading != null
                        ? latestApprovedReading
                        .getPeriodEndDate()
                        : null;

        /*
         * Pending and approved readings count as submitted.
         * Rejected readings do not count toward completion.
         */
        List<KpiReading> validReadings =
                kpiReadingRepository
                        .findByKpiIdAndApprovalStatusNotOrderByPeriodStartDateAsc(
                                kpi.getId(),
                                KpiReadingApprovalStatus.REJECTED
                        );

        CompletionCalculation completionCalculation =
                calculateCompletionInformation(
                        kpi,
                        validReadings
                );

        Site site =
                kpi.getSite();

        AppUser responsibleOwner =
                kpi.getResponsibleOwner();

        return KpiResponse.builder()
                .id(kpi.getId())
                .name(kpi.getName())
                .code(kpi.getCode())
                .pillar(kpi.getPillar())
                .category(kpi.getCategory())

                .siteId(
                        site != null
                                ? site.getId()
                                : null
                )
                .siteName(
                        site != null
                                ? site.getName()
                                : "All Sites"
                )
                .allSites(site == null)

                .description(
                        kpi.getDescription()
                )
                .unitOfMeasure(
                        kpi.getUnitOfMeasure()
                )
                .reportingFrequency(
                        kpi.getReportingFrequency()
                )
                .baselineValue(
                        kpi.getBaselineValue()
                )
                .targetValue(
                        kpi.getTargetValue()
                )
                .targetDirection(
                        kpi.getTargetDirection()
                )
                .dataSource(
                        kpi.getDataSource()
                )
                .effectiveDate(
                        kpi.getEffectiveDate()
                )

                .responsibleOwnerId(
                        responsibleOwner != null
                                ? responsibleOwner.getId()
                                : null
                )
                .responsibleOwnerName(
                        responsibleOwner != null
                                ? responsibleOwner.getFullName()
                                : null
                )
                .responsibleOwnerEmail(
                        responsibleOwner != null
                                ? responsibleOwner.getEmail()
                                : null
                )

                .frameworks(
                        kpi.getFrameworks()
                )
                .approvalRequired(
                        kpi.isApprovalRequired()
                )
                .status(
                        kpi.getStatus()
                )

                .latestActualValue(
                        latestActualValue
                )
                .latestReadingDate(
                        latestReadingDate
                )

                .completion(
                        completionCalculation.completion()
                )
                .completionStatus(
                        completionCalculation.status()
                )
                .expectedReadingsCount(
                        completionCalculation.expectedCount()
                )
                .completedReadingsCount(
                        completionCalculation.completedCount()
                )
                .currentPeriodStartDate(
                        completionCalculation
                                .currentPeriod()
                                .startDate()
                )
                .currentPeriodEndDate(
                        completionCalculation
                                .currentPeriod()
                                .endDate()
                )

                .createdBy(
                        kpi.getCreatedBy()
                )
                .modifiedBy(
                        kpi.getModifiedBy()
                )
                .createdAt(
                        kpi.getCreationDate()
                )
                .updatedAt(
                        kpi.getModifiedDate()
                )
                .build();
    }

    /*
     * ============================================================
     * COMPLETION CALCULATION
     * ============================================================
     */

    private CompletionCalculation calculateCompletionInformation(
            Kpi kpi,
            List<KpiReading> validReadings
    ) {
        LocalDate today =
                LocalDate.now();

        CurrentPeriod currentPeriod =
                getPeriodForDate(
                        kpi.getReportingFrequency(),
                        today
                );

        /*
         * A KPI with no effective date or a future effective date
         * has not started yet.
         */
        if (kpi.getEffectiveDate() == null
                || kpi.getEffectiveDate()
                .isAfter(today)) {
            return new CompletionCalculation(
                    BigDecimal.ZERO
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            ),
                    KpiCompletionStatus.NOT_DUE,
                    0,
                    0,
                    currentPeriod
            );
        }

        List<CurrentPeriod> expectedPeriods =
                buildExpectedPeriods(
                        kpi.getReportingFrequency(),
                        kpi.getEffectiveDate(),
                        today
                );

        Set<String> submittedPeriods =
                validReadings.stream()
                        .map(
                                KpiReading::getReportingPeriod
                        )
                        .filter(period ->
                                period != null
                                        && !period.isBlank()
                        )
                        .map(period ->
                                period.trim()
                                        .toUpperCase(
                                                Locale.ROOT
                                        )
                        )
                        .collect(
                                java.util.stream.Collectors
                                        .toCollection(
                                                HashSet::new
                                        )
                        );

        int expectedCount =
                expectedPeriods.size();

        int completedCount =
                (int) expectedPeriods.stream()
                        .map(CurrentPeriod::key)
                        .filter(submittedPeriods::contains)
                        .count();

        BigDecimal completion =
                calculateCompletionPercentage(
                        completedCount,
                        expectedCount
                );

        KpiCompletionStatus completionStatus =
                determineCompletionStatus(
                        expectedPeriods,
                        submittedPeriods,
                        currentPeriod,
                        today
                );

        return new CompletionCalculation(
                completion,
                completionStatus,
                expectedCount,
                completedCount,
                currentPeriod
        );
    }

    private BigDecimal calculateCompletionPercentage(
            int completedCount,
            int expectedCount
    ) {
        if (expectedCount <= 0) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal completion =
                BigDecimal.valueOf(
                                completedCount
                        )
                        .divide(
                                BigDecimal.valueOf(
                                        expectedCount
                                ),
                                4,
                                RoundingMode.HALF_UP
                        )
                        .multiply(
                                BigDecimal.valueOf(100)
                        );

        if (completion.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {
            completion =
                    BigDecimal.valueOf(100);
        }

        return completion.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private KpiCompletionStatus determineCompletionStatus(
            List<CurrentPeriod> expectedPeriods,
            Set<String> submittedPeriods,
            CurrentPeriod currentPeriod,
            LocalDate today
    ) {
        /*
         * Any previous reporting period that has ended without
         * a valid reading makes the KPI overdue.
         */
        boolean hasOverduePeriod =
                expectedPeriods.stream()
                        .filter(period ->
                                period.endDate()
                                        .isBefore(today)
                        )
                        .anyMatch(period ->
                                !submittedPeriods.contains(
                                        period.key()
                                )
                        );

        if (hasOverduePeriod) {
            return KpiCompletionStatus.OVERDUE;
        }

        boolean currentPeriodCompleted =
                submittedPeriods.contains(
                        currentPeriod.key()
                );

        if (currentPeriodCompleted) {
            return KpiCompletionStatus.ON_TRACK;
        }

        LocalDate warningStartDate =
                currentPeriod.endDate()
                        .minusDays(
                                COMPLETION_DUE_WARNING_DAYS
                        );

        /*
         * On the last five days of the reporting period,
         * the reading becomes due.
         */
        if (!today.isBefore(
                warningStartDate
        )) {
            return KpiCompletionStatus.DUE;
        }

        return KpiCompletionStatus.NOT_DUE;
    }

    private List<CurrentPeriod> buildExpectedPeriods(
            ReportingFrequency frequency,
            LocalDate effectiveDate,
            LocalDate today
    ) {
        if (frequency == null) {
            throw new BadRequestException(
                    "KPI reporting frequency is missing"
            );
        }

        if (effectiveDate == null
                || effectiveDate.isAfter(today)) {
            return List.of();
        }

        CurrentPeriod firstPeriod =
                getPeriodForDate(
                        frequency,
                        effectiveDate
                );

        CurrentPeriod currentPeriod =
                getPeriodForDate(
                        frequency,
                        today
                );

        List<CurrentPeriod> periods =
                new ArrayList<>();

        CurrentPeriod cursor =
                firstPeriod;

        while (!cursor.startDate()
                .isAfter(
                        currentPeriod.startDate()
                )) {
            /*
             * Include a period if some portion of the period falls
             * on or after the KPI effective date.
             */
            if (!cursor.endDate()
                    .isBefore(effectiveDate)) {
                periods.add(cursor);
            }

            cursor =
                    nextPeriod(
                            frequency,
                            cursor
                    );
        }

        return periods;
    }

    private CurrentPeriod getPeriodForDate(
            ReportingFrequency frequency,
            LocalDate date
    ) {
        if (frequency == null) {
            throw new BadRequestException(
                    "KPI reporting frequency is missing"
            );
        }

        return switch (frequency) {
            case MONTHLY -> {
                YearMonth month =
                        YearMonth.from(date);

                yield new CurrentPeriod(
                        month.toString(),
                        month.atDay(1),
                        month.atEndOfMonth()
                );
            }

            case QUARTERLY -> {
                int quarter =
                        ((date.getMonthValue() - 1)
                                / 3) + 1;

                int startingMonth =
                        ((quarter - 1) * 3) + 1;

                LocalDate startDate =
                        LocalDate.of(
                                date.getYear(),
                                startingMonth,
                                1
                        );

                yield new CurrentPeriod(
                        date.getYear()
                                + "-Q"
                                + quarter,
                        startDate,
                        startDate.plusMonths(3)
                                .minusDays(1)
                );
            }

            case SEMI_ANNUAL -> {
                int half =
                        date.getMonthValue() <= 6
                                ? 1
                                : 2;

                int startingMonth =
                        half == 1
                                ? 1
                                : 7;

                LocalDate startDate =
                        LocalDate.of(
                                date.getYear(),
                                startingMonth,
                                1
                        );

                yield new CurrentPeriod(
                        date.getYear()
                                + "-H"
                                + half,
                        startDate,
                        startDate.plusMonths(6)
                                .minusDays(1)
                );
            }

            case ANNUAL -> {
                LocalDate startDate =
                        LocalDate.of(
                                date.getYear(),
                                1,
                                1
                        );

                yield new CurrentPeriod(
                        String.valueOf(
                                date.getYear()
                        ),
                        startDate,
                        LocalDate.of(
                                date.getYear(),
                                12,
                                31
                        )
                );
            }
        };
    }

    private CurrentPeriod nextPeriod(
            ReportingFrequency frequency,
            CurrentPeriod currentPeriod
    ) {
        LocalDate nextDate =
                switch (frequency) {
                    case MONTHLY ->
                            currentPeriod.startDate()
                                    .plusMonths(1);

                    case QUARTERLY ->
                            currentPeriod.startDate()
                                    .plusMonths(3);

                    case SEMI_ANNUAL ->
                            currentPeriod.startDate()
                                    .plusMonths(6);

                    case ANNUAL ->
                            currentPeriod.startDate()
                                    .plusYears(1);
                };

        return getPeriodForDate(
                frequency,
                nextDate
        );
    }

    /*
     * ============================================================
     * ENTITY LOOKUPS
     * ============================================================
     */

    private Kpi getKpiEntity(
            Long id
    ) {
        return kpiRepository
                .findWithDetailsById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KPI not found with id: "
                                        + id
                        )
                );
    }

    private Site resolveSite(
            Long siteId
    ) {
        if (siteId == null) {
            return null;
        }

        return siteRepository
                .findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Site not found with id: "
                                        + siteId
                        )
                );
    }

    private AppUser resolveUser(
            Long userId,
            String label
    ) {
        if (userId == null) {
            throw new BadRequestException(
                    label + " is required"
            );
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                label
                                        + " not found with id: "
                                        + userId
                        )
                );
    }

    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    private void validateCategoryMatchesPillar(
            EsgPillar pillar,
            EsgCategory category
    ) {
        if (pillar == null) {
            throw new BadRequestException(
                    "ESG pillar is required"
            );
        }

        if (category == null) {
            throw new BadRequestException(
                    "ESG category is required"
            );
        }

        if (category.getPillar() != pillar) {
            throw new BadRequestException(
                    "Category "
                            + category
                            + " does not belong to pillar "
                            + pillar
            );
        }
    }

    private void validateTargetConfiguration(
            BigDecimal baseline,
            BigDecimal target,
            TargetDirection direction
    ) {
        if (baseline == null) {
            throw new BadRequestException(
                    "Baseline value is required"
            );
        }

        if (target == null) {
            throw new BadRequestException(
                    "Target value is required"
            );
        }

        if (direction == null) {
            throw new BadRequestException(
                    "Target direction is required"
            );
        }

        if (baseline.compareTo(target) == 0) {
            throw new BadRequestException(
                    "Baseline and target values cannot be equal"
            );
        }

        if (direction == TargetDirection.INCREASE
                && target.compareTo(baseline) <= 0) {
            throw new BadRequestException(
                    "For an increase target, target value must be greater than baseline"
            );
        }

        if (direction == TargetDirection.DECREASE
                && target.compareTo(baseline) >= 0) {
            throw new BadRequestException(
                    "For a decrease target, target value must be lower than baseline"
            );
        }
    }

    private void validateEditableStatus(
            Kpi kpi
    ) {
        if (kpi.getStatus()
                == KpiStatus.PENDING) {
            throw new BadRequestException(
                    "A pending KPI cannot be edited"
            );
        }

        if (kpi.getStatus()
                == KpiStatus.ARCHIVED) {
            throw new BadRequestException(
                    "An archived KPI cannot be edited"
            );
        }
    }

    /*
     * ============================================================
     * UTILITY METHODS
     * ============================================================
     */

    private String normalizeCode(
            String code
    ) {
        if (code == null
                || code.isBlank()) {
            throw new BadRequestException(
                    "KPI code is required"
            );
        }

        return code.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll(
                        "[^A-Z0-9]+",
                        "-"
                )
                .replaceAll(
                        "^-+|-+$",
                        ""
                );
    }

    private long countStatus(
            List<KpiResponse> responses,
            KpiStatus status
    ) {
        return responses.stream()
                .filter(response ->
                        response.getStatus()
                                == status
                )
                .count();
    }

    /*
     * ============================================================
     * INTERNAL RECORDS
     * ============================================================
     */

    private record CurrentPeriod(
            String key,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    private record CompletionCalculation(
            BigDecimal completion,
            KpiCompletionStatus status,
            int expectedCount,
            int completedCount,
            CurrentPeriod currentPeriod
    ) {
    }
}