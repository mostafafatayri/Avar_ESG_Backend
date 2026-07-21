package com.fatayriTech.avarESG.service.KpiReadingService;

import com.fatayriTech.avarESG.config.AppLoggingProperties;
import com.fatayriTech.avarESG.dto.request.KpiReadingRequests.CreateKpiReadingRequest;
import com.fatayriTech.avarESG.dto.request.KpiReadingRequests.RejectKpiReadingRequest;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.KpiReadingOverviewResponse;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.KpiReadingResponse;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.MissingKpiPeriodResponse;
import com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus;
import com.fatayriTech.avarESG.enums.KpiReadingSource;
import com.fatayriTech.avarESG.enums.UserStatus;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.KpiReading;
import com.fatayriTech.avarESG.repository.KpiReadingRepository;
import com.fatayriTech.avarESG.repository.KpiRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.service.NotificationService.NotificationTriggerService;
import com.fatayriTech.avarESG.service.SecurityService.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KpiReadingServiceImpl
        implements KpiReadingService {
    private final NotificationTriggerService
            notificationTriggerService;
    private final KpiReadingRepository
            kpiReadingRepository;

    private final KpiRepository kpiRepository;

    private final UserRepository userRepository;

    private final CurrentUserService currentUserService;

    private final KpiReportingPeriodService
            reportingPeriodService;

    private final AppLoggingProperties
            loggingProperties;

    @Override
    @Transactional(readOnly = true)
    public List<KpiReadingResponse> getReadings(
            Long kpiId
    ) {
        validateKpiExists(kpiId);

        return kpiReadingRepository
                .findByKpiIdOrderByPeriodStartDateDesc(
                        kpiId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KpiReadingOverviewResponse
    getReadingOverview(Long kpiId) {

        Kpi kpi = getKpi(kpiId);

        List<KpiReading> readings =
                kpiReadingRepository
                        .findByKpiIdOrderByPeriodStartDateDesc(
                                kpiId
                        );

        List<String> existingPeriods =
                readings.stream()
                        .map(
                                KpiReading::getReportingPeriod
                        )
                        .toList();

        List<MissingKpiPeriodResponse>
                missingPeriods =
                reportingPeriodService
                        .findMissingPeriods(
                                kpi,
                                existingPeriods
                        );

        int approved = (int) readings.stream()
                .filter(reading ->
                        reading.getApprovalStatus() ==
                                KpiReadingApprovalStatus.APPROVED
                )
                .count();

        int pending = (int) readings.stream()
                .filter(reading ->
                        reading.getApprovalStatus() ==
                                KpiReadingApprovalStatus.PENDING
                )
                .count();

        int rejected = (int) readings.stream()
                .filter(reading ->
                        reading.getApprovalStatus() ==
                                KpiReadingApprovalStatus.REJECTED
                )
                .count();

        return KpiReadingOverviewResponse.builder()
                .kpiId(kpi.getId())
                .kpiCode(kpi.getCode())
                .kpiName(kpi.getName())
                .reportingFrequency(
                        kpi.getReportingFrequency().name()
                )
                .unitOfMeasure(
                        kpi.getUnitOfMeasure()
                )
                .approvalRequired(
                        Boolean.TRUE.equals(
                                kpi.isApprovalRequired()
                        )
                )
                .totalReadings(readings.size())
                .approvedReadings(approved)
                .pendingReadings(pending)
                .rejectedReadings(rejected)
                .missingPeriods(missingPeriods)
                .readings(
                        readings.stream()
                                .map(this::mapToResponse)
                                .toList()
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public KpiReadingResponse getReadingById(
            Long kpiId,
            Long readingId
    ) {
        return mapToResponse(
                getReading(kpiId, readingId)
        );
    }

    @Override
    public KpiReadingResponse createReading(
            Long kpiId,
            CreateKpiReadingRequest request
    ) {
        Kpi kpi = getKpi(kpiId);

        AppUser currentUser =
                getAuthenticatedUser();

        KpiReportingPeriodService.PeriodDetails
                periodDetails =
                reportingPeriodService.parsePeriod(
                        kpi,
                        request.getReportingPeriod()
                );

        validatePeriodAgainstKpi(
                kpi,
                periodDetails
        );

        if (kpiReadingRepository
                .existsByKpiIdAndReportingPeriodIgnoreCase(
                        kpiId,
                        periodDetails.key()
                )) {

            throw new BadRequestException(
                    "A reading already exists for period: " +
                            periodDetails.label()
            );
        }

        KpiReadingApprovalStatus approvalStatus =
                Boolean.TRUE.equals(
                        kpi.isApprovalRequired()
                )
                        ? KpiReadingApprovalStatus.PENDING
                        : KpiReadingApprovalStatus.APPROVED;

        KpiReading reading =
                KpiReading.builder()
                        .kpi(kpi)
                        .reportingPeriod(
                                periodDetails.key()
                        )
                        .periodStartDate(
                                periodDetails.startDate()
                        )
                        .periodEndDate(
                                periodDetails.endDate()
                        )
                        .actualValue(
                                request.getActualValue()
                        )
                        .source(
                                KpiReadingSource.MANUAL
                        )
                        .submittedBy(currentUser)
                        .submissionDate(
                                LocalDateTime.now()
                        )
                        .approvalStatus(
                                approvalStatus
                        )
                        .build();

        if (approvalStatus ==
                KpiReadingApprovalStatus.APPROVED) {

            reading.setReviewedBy(currentUser);
            reading.setReviewDate(
                    LocalDateTime.now()
            );
        }

        KpiReading savedReading =
                kpiReadingRepository.save(reading);

        if (loggingProperties.isVerbose()) {
            log.info(
                    "Created KPI reading. kpiId={}, " +
                            "readingId={}, period={}, userId={}",
                    kpiId,
                    savedReading.getId(),
                    savedReading.getReportingPeriod(),
                    currentUser.getId()
            );
        }



        notificationTriggerService
                .triggerReadingSubmitted(savedReading);

        return mapToResponse(savedReading);
    }

    @Override
    public KpiReadingResponse approveReading(
            Long kpiId,
            Long readingId
    ) {
        KpiReading reading =
                getReading(kpiId, readingId);

        AppUser reviewer =
                getAuthenticatedUser();

        if (reading.getApprovalStatus() ==
                KpiReadingApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "Reading is already approved"
            );
        }

        reading.setApprovalStatus(
                KpiReadingApprovalStatus.APPROVED
        );

        reading.setReviewedBy(reviewer);

        reading.setReviewDate(
                LocalDateTime.now()
        );

        reading.setRejectionReason(null);


        if (loggingProperties.isVerbose()) {
            log.info(
                    "Approved KPI reading. kpiId={}, " +
                            "readingId={}, reviewerId={}",
                    kpiId,
                    readingId,
                    reviewer.getId()
            );
        }

        KpiReading savedReading =
                kpiReadingRepository.save(reading);

        notificationTriggerService
                .triggerReadingSubmitted(savedReading);

        return mapToResponse(savedReading);
    }

    @Override
    public KpiReadingResponse rejectReading(
            Long kpiId,
            Long readingId,
            RejectKpiReadingRequest request
    ) {
        KpiReading reading =
                getReading(kpiId, readingId);

        AppUser reviewer =
                getAuthenticatedUser();

        if (reading.getApprovalStatus() ==
                KpiReadingApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "Approved reading cannot be rejected"
            );
        }

        reading.setApprovalStatus(
                KpiReadingApprovalStatus.REJECTED
        );

        reading.setReviewedBy(reviewer);

        reading.setReviewDate(
                LocalDateTime.now()
        );

        reading.setRejectionReason(
                request.getReason().trim()
        );

        KpiReading saved =
                kpiReadingRepository.save(reading);

        if (loggingProperties.isVerbose()) {
            log.warn(
                    "Rejected KPI reading. kpiId={}, " +
                            "readingId={}, reviewerId={}",
                    kpiId,
                    readingId,
                    reviewer.getId()
            );
        }


        notificationTriggerService
                .triggerReadingRejected(saved);

        return mapToResponse(saved);
    }

    @Override
    public void deleteReading(
            Long kpiId,
            Long readingId
    ) {
        KpiReading reading =
                getReading(kpiId, readingId);

        if (reading.getApprovalStatus() ==
                KpiReadingApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "Approved readings cannot be deleted. " +
                            "Archive or reverse the reading instead."
            );
        }

        kpiReadingRepository.delete(reading);

        if (loggingProperties.isVerbose()) {
            log.warn(
                    "Deleted KPI reading. kpiId={}, " +
                            "readingId={}",
                    kpiId,
                    readingId
            );
        }
    }

    private void validatePeriodAgainstKpi(
            Kpi kpi,
            KpiReportingPeriodService.PeriodDetails
                    periodDetails
    ) {
        LocalDate effectiveDate =
                kpi.getEffectiveDate();

        if (effectiveDate != null &&
                periodDetails.endDate()
                        .isBefore(effectiveDate)) {

            throw new BadRequestException(
                    "Reporting period cannot be before " +
                            "the KPI effective date"
            );
        }

        if (periodDetails.startDate()
                .isAfter(LocalDate.now())) {

            throw new BadRequestException(
                    "Future reporting periods are not allowed"
            );
        }
    }

    private Kpi getKpi(Long kpiId) {
        return kpiRepository.findById(kpiId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KPI not found with id: " +
                                        kpiId
                        )
                );
    }

    private void validateKpiExists(Long kpiId) {
        if (!kpiRepository.existsById(kpiId)) {
            throw new ResourceNotFoundException(
                    "KPI not found with id: " +
                            kpiId
            );
        }
    }

    private KpiReading getReading(
            Long kpiId,
            Long readingId
    ) {
        return kpiReadingRepository
                .findByIdAndKpiId(
                        readingId,
                        kpiId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KPI reading not found with id: " +
                                        readingId
                        )
                );
    }

    private AppUser getAuthenticatedUser() {
        Long userId =
                currentUserService.getUserId();

        AppUser user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated user not found"
                                )
                        );

        if (user.getStatus() !=
                UserStatus.ACTIVE) {

            throw new BadRequestException(
                    "User account is not active"
            );
        }

        return user;
    }

    private KpiReadingResponse mapToResponse(
            KpiReading reading
    ) {
        AppUser submittedBy =
                reading.getSubmittedBy();

        AppUser reviewedBy =
                reading.getReviewedBy();

        return KpiReadingResponse.builder()
                .id(reading.getId())
                .kpiId(reading.getKpi().getId())
                .kpiCode(
                        reading.getKpi().getCode()
                )
                .kpiName(
                        reading.getKpi().getName()
                )
                .reportingPeriod(
                        reading.getReportingPeriod()
                )
                .reportingPeriodLabel(
                        buildPeriodLabel(reading)
                )
                .periodStartDate(
                        reading.getPeriodStartDate()
                )
                .periodEndDate(
                        reading.getPeriodEndDate()
                )
                .actualValue(
                        reading.getActualValue()
                )
                .unitOfMeasure(
                        reading.getKpi()
                                .getUnitOfMeasure()
                )
                .source(reading.getSource())
                .submittedByUserId(
                        submittedBy.getId()
                )
                .submittedByUsername(
                        submittedBy.getUsername()
                )
                .submittedByFullName(
                        submittedBy.getFullName()
                )
                .submissionDate(
                        reading.getSubmissionDate()
                )
                .approvalStatus(
                        reading.getApprovalStatus()
                )
                .reviewedByUserId(
                        reviewedBy != null
                                ? reviewedBy.getId()
                                : null
                )
                .reviewedByFullName(
                        reviewedBy != null
                                ? reviewedBy.getFullName()
                                : null
                )
                .reviewDate(
                        reading.getReviewDate()
                )
                .rejectionReason(
                        reading.getRejectionReason()
                )
                .evidenceFileName(
                        reading.getEvidenceFileName()
                )
                .evidenceFileUrl(
                        reading.getEvidenceFileUrl()
                )
                .evidenceContentType(
                        reading.getEvidenceContentType()
                )
                .evidenceFileSize(
                        reading.getEvidenceFileSize()
                )
                .createdAt(
                        reading.getCreationDate()
                )
                .updatedAt(
                        reading.getModifiedDate()
                )
                .build();
    }

    private String buildPeriodLabel(
            KpiReading reading
    ) {
        String frequency =
                reading.getKpi()
                        .getReportingFrequency()
                        .name();

        return switch (frequency) {
            case "MONTHLY" ->
                    reading.getPeriodStartDate()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "MMMM yyyy",
                                            Locale.ENGLISH
                                    )
                            );

            case "QUARTERLY" -> {
                int quarter =
                        ((reading.getPeriodStartDate()
                                .getMonthValue() - 1) / 3) + 1;

                yield "Q" + quarter + " " +
                        reading.getPeriodStartDate()
                                .getYear();
            }

            case "SEMI_ANNUAL" -> {
                int half =
                        reading.getPeriodStartDate()
                                .getMonthValue() <= 6
                                ? 1
                                : 2;

                yield "H" + half + " " +
                        reading.getPeriodStartDate()
                                .getYear();
            }

            case "ANNUAL" ->
                    String.valueOf(
                            reading.getPeriodStartDate()
                                    .getYear()
                    );

            default ->
                    reading.getReportingPeriod();
        };
    }
}