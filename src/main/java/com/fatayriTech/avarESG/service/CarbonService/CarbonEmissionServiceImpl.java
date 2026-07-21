package com.fatayriTech.avarESG.service.CarbonService;

import com.fatayriTech.avarESG.dto.request.CarbonRequests.AssignCarbonReviewerRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.CreateCarbonCorrectionRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.CreateCarbonEmissionRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.RejectCarbonEmissionRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.UpdateCarbonEmissionRequest;
import com.fatayriTech.avarESG.dto.response.CarbonResponse.CarbonDashboardResponse;
import com.fatayriTech.avarESG.dto.response.CarbonResponse.CarbonEmissionResponse;
import com.fatayriTech.avarESG.dto.response.CarbonResponse.CarbonVersionHistoryResponse;
import com.fatayriTech.avarESG.enums.*;
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.exceptions.ResourceNotFoundException;
import com.fatayriTech.avarESG.model.AppUser;
import com.fatayriTech.avarESG.model.CarbonEmission;
import com.fatayriTech.avarESG.model.Site;
import com.fatayriTech.avarESG.repository.CarbonEmissionRepository;
import com.fatayriTech.avarESG.repository.SiteRepository;
import com.fatayriTech.avarESG.repository.UserRepository;
import com.fatayriTech.avarESG.repository.specification.CarbonEmissionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CarbonEmissionServiceImpl
        implements CarbonEmissionService {

    private static final List<CarbonApprovalStatus>
            OPEN_CORRECTION_STATUSES =
            List.of(
                    CarbonApprovalStatus.DRAFT,
                    CarbonApprovalStatus.PENDING
            );

    private final CarbonEmissionRepository
            carbonEmissionRepository;

    private final SiteRepository
            siteRepository;

    private final UserRepository
            userRepository;

    private final CarbonKpiIntegrationService
            carbonKpiIntegrationService;

    @Override
    @Transactional
    public CarbonEmissionResponse create(
            Long currentUserId,
            CreateCarbonEmissionRequest request
    ) {
        requireActiveUser(currentUserId);

        Site facility =
                requireFacility(
                        request.getFacilityId()
                );

        LocalDate reportingPeriod =
                parseReportingPeriod(
                        request.getReportingPeriod()
                );

        validateScopeAndSource(
                request.getScope(),
                request.getEmissionSource()
        );

        Long reviewerId =
                validateOptionalReviewer(
                        request.getReviewerId()
                );

        CarbonEmission latest =
                carbonEmissionRepository
                        .findFirstByFacilityIdAndEmissionSourceAndReportingPeriodOrderByVersionNumberDesc(
                                facility.getId(),
                                request.getEmissionSource(),
                                reportingPeriod
                        )
                        .orElse(null);

        if (latest == null) {
            CarbonEmission emission =
                    CarbonEmission.builder()
                            .scope(request.getScope())
                            .emissionSource(
                                    request.getEmissionSource()
                            )
                            .facilityId(
                                    facility.getId()
                            )
                            .reportingPeriod(
                                    reportingPeriod
                            )
                            .emissions(
                                    request.getEmissions()
                            )
                            .emissionsUnit(
                                    request.getEmissionsUnit()
                            )
                            .memo(
                                    resolveMemoValue(
                                            request.getEmissionSource(),
                                            request.getMemo()
                                    )
                            )
                            .dataSource(
                                    trimToNull(
                                            request.getDataSource()
                                    )
                            )
                            .verification(
                                    request.getVerification() != null
                                            ? request.getVerification()
                                            : CarbonVerificationStatus.UNVERIFIED
                            )
                            .remarks(
                                    trimToNull(
                                            request.getRemarks()
                                    )
                            )
                            .reviewerId(reviewerId)
                            .approvalStatus(
                                    CarbonApprovalStatus.DRAFT
                            )
                            .versionNumber(1)
                            .correction(false)
                            .activeVersion(false)
                            .supersedesEmissionId(null)
                            .correctionReason(null)
                            .build();

            return mapToResponse(
                    carbonEmissionRepository.save(
                            emission
                    ),
                    currentUserId
            );
        }

        if (latest.isActiveVersion()
                && latest.getApprovalStatus()
                == CarbonApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "An approved active carbon record already exists. " +
                            "Use the correction workflow to change it."
            );
        }

        if (latest.getApprovalStatus()
                == CarbonApprovalStatus.PENDING) {

            throw new BadRequestException(
                    "A matching carbon record is already pending approval."
            );
        }

        if (latest.isCorrection()) {
            throw new BadRequestException(
                    "A correction version already exists for this carbon record. " +
                            "Open its version history before creating another change."
            );
        }

        latest.setScope(
                request.getScope()
        );

        latest.setEmissions(
                request.getEmissions()
        );

        latest.setEmissionsUnit(
                request.getEmissionsUnit()
        );

        latest.setMemo(
                resolveMemoValue(
                        request.getEmissionSource(),
                        request.getMemo()
                )
        );

        latest.setDataSource(
                trimToNull(
                        request.getDataSource()
                )
        );

        latest.setVerification(
                request.getVerification() != null
                        ? request.getVerification()
                        : CarbonVerificationStatus.UNVERIFIED
        );

        latest.setRemarks(
                trimToNull(
                        request.getRemarks()
                )
        );

        latest.setReviewerId(
                reviewerId
        );

        latest.setApprovalStatus(
                CarbonApprovalStatus.DRAFT
        );

        latest.setSubmittedByUserId(null);
        latest.setSubmissionDate(null);

        clearReviewInformation(
                latest
        );

        CarbonEmissionResponse response =
                mapToResponse(
                        carbonEmissionRepository.save(
                                latest
                        ),
                        currentUserId
                );

        response.setExistingRecordUpdated(
                true
        );

        return response;
    }

    @Override
    @Transactional
    public CarbonEmissionResponse createCorrection(
            Long activeEmissionId,
            Long currentUserId,
            CreateCarbonCorrectionRequest request
    ) {
        AppUser requestingUser =
                requireActiveUser(
                        currentUserId
                );

        CarbonEmission activeEmission =
                requireEmission(
                        activeEmissionId
                );

        if (activeEmission.getApprovalStatus()
                != CarbonApprovalStatus.APPROVED
                || !activeEmission.isActiveVersion()) {

            throw new BadRequestException(
                    "A correction can only be created from the active approved version"
            );
        }

        if (activeEmission.getVersionGroupId()
                == null
                || activeEmission.getVersionGroupId()
                .isBlank()) {

            throw new BadRequestException(
                    "The selected carbon record does not have a valid version group"
            );
        }

        boolean openCorrectionExists =
                carbonEmissionRepository
                        .existsByVersionGroupIdAndCorrectionTrueAndApprovalStatusIn(
                                activeEmission.getVersionGroupId(),
                                OPEN_CORRECTION_STATUSES
                        );

        if (openCorrectionExists) {
            throw new BadRequestException(
                    "A draft or pending correction already exists for this carbon record"
            );
        }

        AppUser reviewer =
                requireActiveUser(
                        request.getReviewerId()
                );

        if (reviewer.getId()
                .equals(
                        requestingUser.getId()
                )) {

            throw new BadRequestException(
                    "The correction reviewer must be different from the person creating the correction"
            );
        }

        Integer maximumVersion =
                carbonEmissionRepository
                        .findMaximumVersionNumber(
                                activeEmission.getVersionGroupId()
                        );

        int nextVersion =
                maximumVersion == null
                        ? 2
                        : maximumVersion + 1;

        CarbonEmission correction =
                CarbonEmission.builder()
                        .scope(
                                activeEmission.getScope()
                        )
                        .emissionSource(
                                activeEmission.getEmissionSource()
                        )
                        .facilityId(
                                activeEmission.getFacilityId()
                        )
                        .reportingPeriod(
                                activeEmission.getReportingPeriod()
                        )
                        .emissions(
                                request.getEmissions()
                        )
                        .emissionsUnit(
                                request.getEmissionsUnit()
                        )
                        .memo(
                                resolveMemoValue(
                                        activeEmission.getEmissionSource(),
                                        request.getMemo()
                                )
                        )
                        .dataSource(
                                trimToNull(
                                        request.getDataSource()
                                )
                        )
                        .dataSourceFileName(
                                activeEmission.getDataSourceFileName()
                        )
                        .dataSourceFileUrl(
                                activeEmission.getDataSourceFileUrl()
                        )
                        .dataSourceContentType(
                                activeEmission.getDataSourceContentType()
                        )
                        .dataSourceFileSize(
                                activeEmission.getDataSourceFileSize()
                        )
                        .verification(
                                request.getVerification() != null
                                        ? request.getVerification()
                                        : activeEmission.getVerification()
                        )
                        .remarks(
                                trimToNull(
                                        request.getRemarks()
                                )
                        )
                       /* .reviewerId( //
                                reviewer.getId()
                        )
                        .approvalStatus(
                                CarbonApprovalStatus.DRAFT  //
                        )
                        .versionGroupId(
                                activeEmission.getVersionGroupId()  //
                        )*/
                        .reviewerId(
                                reviewer.getId()
                        )
                        .submittedByUserId(
                                requestingUser.getId()
                        )
                        .submissionDate(
                                LocalDateTime.now()
                        )
                        .approvalStatus(
                                CarbonApprovalStatus.PENDING
                        )
                        .versionGroupId(
                                activeEmission.getVersionGroupId()
                        )
                        .versionNumber(
                                nextVersion
                        )
                        .correction(true)
                        .activeVersion(false)
                        .supersedesEmissionId(
                                activeEmission.getId()
                        )
                        .correctionReason(
                                request.getCorrectionReason()
                                        .trim()
                        )
                        .build();

        CarbonEmission saved =
                carbonEmissionRepository.save(
                        correction
                );

        return mapToResponse(
                saved,
                currentUserId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CarbonVersionHistoryResponse getHistory(
            Long emissionId,
            Long currentUserId
    ) {
        CarbonEmission selected =
                requireEmission(
                        emissionId
                );

        List<CarbonEmission> versions =
                carbonEmissionRepository
                        .findByVersionGroupIdOrderByVersionNumberDesc(
                                selected.getVersionGroupId()
                        );

        CarbonEmission active =
                versions.stream()
                        .filter(
                                CarbonEmission::isActiveVersion
                        )
                        .findFirst()
                        .orElse(null);

        return CarbonVersionHistoryResponse.builder()
                .versionGroupId(
                        selected.getVersionGroupId()
                )
                .activeEmissionId(
                        active != null
                                ? active.getId()
                                : null
                )
                .activeVersionNumber(
                        active != null
                                ? active.getVersionNumber()
                                : null
                )
                .totalVersions(
                        versions.size()
                )
                .versions(
                        versions.stream()
                                .map(version ->
                                        mapToResponse(
                                                version,
                                                currentUserId
                                        )
                                )
                                .toList()
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarbonEmissionResponse> getAll(
            CarbonScope scope,
            CarbonEmissionSource source,
            Long facilityId,
            String reportingPeriod,
            CarbonApprovalStatus status,
            String search,
            Long currentUserId
    ) {
        LocalDate parsedPeriod =
                reportingPeriod == null
                        || reportingPeriod.isBlank()
                        ? null
                        : parseReportingPeriod(
                        reportingPeriod
                );

        Specification<CarbonEmission> specification =
                Specification.allOf(
                        CarbonEmissionSpecification
                                .hasScope(scope),

                        CarbonEmissionSpecification
                                .hasSource(source),

                        CarbonEmissionSpecification
                                .hasFacility(
                                        facilityId
                                ),

                        CarbonEmissionSpecification
                                .hasReportingPeriod(
                                        parsedPeriod
                                ),

                        CarbonEmissionSpecification
                                .hasStatus(status),

                        CarbonEmissionSpecification
                                .containsSearch(
                                        search
                                )
                );

        Sort sort =
                Sort.by(
                                Sort.Direction.DESC,
                                "reportingPeriod"
                        )
                        .and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "versionNumber"
                                )
                        )
                        .and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "creationDate"
                                )
                        );

        return carbonEmissionRepository
                .findAll(
                        specification,
                        sort
                )
                .stream()
                /*
                 * Main Carbon table displays:
                 *
                 * - active approved versions
                 * - draft corrections
                 * - pending corrections
                 * - rejected corrections
                 * - unapproved original records
                 *
                 * Historical inactive approved versions are
                 * available through the history endpoint.
                 */
                .filter(emission ->
                        emission.isActiveVersion()
                                || emission.getApprovalStatus()
                                != CarbonApprovalStatus.APPROVED
                )
                .map(emission ->
                        mapToResponse(
                                emission,
                                currentUserId
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarbonEmissionResponse getById(
            Long id,
            Long currentUserId
    ) {
        return mapToResponse(
                requireEmission(id),
                currentUserId
        );
    }

    @Override
    @Transactional
    public CarbonEmissionResponse update(
            Long id,
            Long currentUserId,
            UpdateCarbonEmissionRequest request
    ) {
        requireActiveUser(
                currentUserId
        );

        CarbonEmission emission =
                requireEmission(id);

        validateEditable(
                emission
        );

        if (emission.isCorrection()) {
            updateCorrection(
                    emission,
                    request
            );
        } else {
            updateOriginal(
                    emission,
                    request
            );
        }

        CarbonEmission saved =
                carbonEmissionRepository.save(
                        emission
                );

        return mapToResponse(
                saved,
                currentUserId
        );
    }

    private void updateCorrection(
            CarbonEmission emission,
            UpdateCarbonEmissionRequest request
    ) {
        /*
         * Corrections cannot change the identity of the
         * underlying Carbon record.
         */
        if (request.getScope() != null
                && request.getScope()
                != emission.getScope()) {

            throw new BadRequestException(
                    "A correction cannot change the Carbon scope"
            );
        }

        if (request.getEmissionSource() != null
                && request.getEmissionSource()
                != emission.getEmissionSource()) {

            throw new BadRequestException(
                    "A correction cannot change the emission source"
            );
        }

        if (request.getFacilityId() != null
                && !request.getFacilityId()
                .equals(
                        emission.getFacilityId()
                )) {

            throw new BadRequestException(
                    "A correction cannot change the facility"
            );
        }

        if (request.getReportingPeriod() != null) {
            LocalDate requestedPeriod =
                    parseReportingPeriod(
                            request.getReportingPeriod()
                    );

            if (!requestedPeriod.equals(
                    emission.getReportingPeriod()
            )) {
                throw new BadRequestException(
                        "A correction cannot change the reporting period"
                );
            }
        }

        applyEditableValues(
                emission,
                request
        );
    }

    private void updateOriginal(
            CarbonEmission emission,
            UpdateCarbonEmissionRequest request
    ) {
        if (request.getScope() != null) {
            emission.setScope(
                    request.getScope()
            );
        }

        if (request.getEmissionSource() != null) {
            emission.setEmissionSource(
                    request.getEmissionSource()
            );
        }

        if (request.getFacilityId() != null) {
            Site facility =
                    requireFacility(
                            request.getFacilityId()
                    );

            emission.setFacilityId(
                    facility.getId()
            );
        }

        if (request.getReportingPeriod() != null) {
            emission.setReportingPeriod(
                    parseReportingPeriod(
                            request.getReportingPeriod()
                    )
            );
        }

        validateScopeAndSource(
                emission.getScope(),
                emission.getEmissionSource()
        );

        validateBusinessKey(
                emission
        );

        applyEditableValues(
                emission,
                request
        );
    }

    private void applyEditableValues(
            CarbonEmission emission,
            UpdateCarbonEmissionRequest request
    ) {
        if (request.getEmissions() != null) {
            emission.setEmissions(
                    request.getEmissions()
            );
        }

        if (request.getEmissionsUnit() != null) {
            emission.setEmissionsUnit(
                    request.getEmissionsUnit()
            );
        }

        if (request.getMemo() != null) {
            emission.setMemo(
                    resolveMemoValue(
                            emission.getEmissionSource(),
                            request.getMemo()
                    )
            );
        }

        if (request.getDataSource() != null) {
            emission.setDataSource(
                    trimToNull(
                            request.getDataSource()
                    )
            );
        }

        if (request.getVerification() != null) {
            emission.setVerification(
                    request.getVerification()
            );
        }

        if (request.getRemarks() != null) {
            emission.setRemarks(
                    trimToNull(
                            request.getRemarks()
                    )
            );
        }

        if (request.getReviewerId() != null) {
            emission.setReviewerId(
                    requireActiveUser(
                            request.getReviewerId()
                    ).getId()
            );
        }

        if (emission.getApprovalStatus()
                == CarbonApprovalStatus.REJECTED) {

            emission.setApprovalStatus(
                    CarbonApprovalStatus.DRAFT
            );

            emission.setSubmittedByUserId(null);
            emission.setSubmissionDate(null);

            clearReviewInformation(
                    emission
            );
        }
    }

    @Override
    @Transactional
    public void delete(
            Long id,
            Long currentUserId
    ) {
        requireActiveUser(
                currentUserId
        );

        CarbonEmission emission =
                requireEmission(id);

        if (emission.getApprovalStatus()
                != CarbonApprovalStatus.DRAFT) {

            throw new BadRequestException(
                    "Only draft carbon emissions can be deleted"
            );
        }

        if (emission.isActiveVersion()) {
            throw new BadRequestException(
                    "An active Carbon version cannot be deleted"
            );
        }

        carbonEmissionRepository.delete(
                emission
        );
    }

    @Override
    @Transactional
    public CarbonEmissionResponse assignReviewer(
            Long id,
            Long currentUserId,
            AssignCarbonReviewerRequest request
    ) {
        requireActiveUser(
                currentUserId
        );

        CarbonEmission emission =
                requireEmission(id);

        validateEditable(
                emission
        );

        AppUser reviewer =
                requireActiveUser(
                        request.getReviewerId()
                );

        emission.setReviewerId(
                reviewer.getId()
        );

        return mapToResponse(
                carbonEmissionRepository.save(
                        emission
                ),
                currentUserId
        );
    }

    @Override
    @Transactional
    public CarbonEmissionResponse submit(
            Long id,
            Long currentUserId
    ) {
        AppUser submittingUser =
                requireActiveUser(
                        currentUserId
                );

        CarbonEmission emission =
                requireEmission(id);

        if (emission.getApprovalStatus()
                != CarbonApprovalStatus.DRAFT
                && emission.getApprovalStatus()
                != CarbonApprovalStatus.REJECTED) {

            throw new BadRequestException(
                    "Only draft or rejected emissions can be submitted"
            );
        }

        if (emission.isCorrection()) {
            validateCorrectionForSubmission(
                    emission,
                    submittingUser
            );
        }

        emission.setSubmittedByUserId(
                submittingUser.getId()
        );

        emission.setSubmissionDate(
                LocalDateTime.now()
        );

        clearReviewInformation(
                emission
        );

        /*
         * Corrections always require independent review.
         */
        if (emission.isCorrection()) {
            emission.setApprovalStatus(
                    CarbonApprovalStatus.PENDING
            );

            return mapToResponse(
                    carbonEmissionRepository.save(
                            emission
                    ),
                    currentUserId
            );
        }

        /*
         * Original records retain your existing optional
         * reviewer behavior.
         */
        if (emission.getReviewerId() == null) {
            emission.setApprovalStatus(
                    CarbonApprovalStatus.APPROVED
            );

            emission.setActiveVersion(
                    true
            );

            emission.setReviewedByUserId(
                    submittingUser.getId()
            );

            emission.setReviewDate(
                    LocalDateTime.now()
            );

            CarbonEmission saved =
                    carbonEmissionRepository
                            .saveAndFlush(
                                    emission
                            );

            synchronizeApprovedEmission(
                    saved
            );

            return mapToResponse(
                    saved,
                    currentUserId
            );
        }

        emission.setApprovalStatus(
                CarbonApprovalStatus.PENDING
        );

        return mapToResponse(
                carbonEmissionRepository.save(
                        emission
                ),
                currentUserId
        );
    }

    private void validateCorrectionForSubmission(
            CarbonEmission emission,
            AppUser submittingUser
    ) {
        if (emission.getReviewerId() == null) {
            throw new BadRequestException(
                    "A reviewer is required before submitting a correction"
            );
        }

        if (emission.getReviewerId()
                .equals(
                        submittingUser.getId()
                )) {

            throw new BadRequestException(
                    "The correction reviewer must be different from the submitter"
            );
        }

        if (emission.getCorrectionReason() == null
                || emission.getCorrectionReason()
                .isBlank()) {

            throw new BadRequestException(
                    "Correction reason is required"
            );
        }

        CarbonEmission superseded =
                requireEmission(
                        emission.getSupersedesEmissionId()
                );

        if (!superseded.isActiveVersion()
                || superseded.getApprovalStatus()
                != CarbonApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "The version being corrected is no longer the active approved version"
            );
        }
    }

    @Override
    @Transactional
    public CarbonEmissionResponse approve(
            Long id,
            Long currentUserId
    ) {
        AppUser reviewer =
                requireActiveUser(
                        currentUserId
                );

        CarbonEmission emission =
                requireEmission(id);

        validatePendingReview(
                emission,
                reviewer
        );

        if (emission.isCorrection()) {
            approveCorrection(
                    emission,
                    reviewer
            );
        } else {
            approveOriginal(
                    emission,
                    reviewer
            );
        }

        CarbonEmission saved =
                carbonEmissionRepository
                        .saveAndFlush(
                                emission
                        );

        synchronizeApprovedEmission(
                saved
        );

        return mapToResponse(
                saved,
                currentUserId
        );
    }

    private void approveOriginal(
            CarbonEmission emission,
            AppUser reviewer
    ) {
        CarbonEmission existingActive =
                carbonEmissionRepository
                        .findFirstByVersionGroupIdAndActiveVersionTrueOrderByVersionNumberDesc(
                                emission.getVersionGroupId()
                        )
                        .orElse(null);

        if (existingActive != null
                && !existingActive.getId()
                .equals(
                        emission.getId()
                )) {

            throw new BadRequestException(
                    "This version group already has another active approved version"
            );
        }

        emission.setApprovalStatus(
                CarbonApprovalStatus.APPROVED
        );

        emission.setActiveVersion(
                true
        );

        emission.setReviewedByUserId(
                reviewer.getId()
        );

        emission.setReviewDate(
                LocalDateTime.now()
        );

        emission.setRejectionReason(null);
    }

    private void approveCorrection(
            CarbonEmission correction,
            AppUser reviewer
    ) {
        CarbonEmission currentActive =
                carbonEmissionRepository
                        .findFirstByVersionGroupIdAndActiveVersionTrueOrderByVersionNumberDesc(
                                correction.getVersionGroupId()
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No active approved version exists for this correction"
                                )
                        );

        if (correction.getSupersedesEmissionId() == null
                || !correction.getSupersedesEmissionId()
                .equals(currentActive.getId())) {

            throw new BadRequestException(
                    "This correction is based on an outdated Carbon version"
            );
        }

        /*
         * Step 1:
         * Deactivate and immediately flush the existing
         * active version.
         *
         * This must reach PostgreSQL before the correction
         * is activated because the database allows only one
         * active version per version group.
         */
        currentActive.setActiveVersion(false);

        carbonEmissionRepository.saveAndFlush(
                currentActive
        );

        /*
         * Step 2:
         * Activate the approved correction only after the
         * old active version has been deactivated in the DB.
         */
        correction.setApprovalStatus(
                CarbonApprovalStatus.APPROVED
        );

        correction.setActiveVersion(true);

        correction.setReviewedByUserId(
                reviewer.getId()
        );

        correction.setReviewDate(
                LocalDateTime.now()
        );

        correction.setRejectionReason(null);
    }

    @Override
    @Transactional
    public CarbonEmissionResponse reject(
            Long id,
            Long currentUserId,
            RejectCarbonEmissionRequest request
    ) {
        AppUser reviewer =
                requireActiveUser(
                        currentUserId
                );

        CarbonEmission emission =
                requireEmission(id);

        validatePendingReview(
                emission,
                reviewer
        );

        emission.setApprovalStatus(
                CarbonApprovalStatus.REJECTED
        );

        emission.setActiveVersion(
                false
        );

        emission.setReviewedByUserId(
                reviewer.getId()
        );

        emission.setReviewDate(
                LocalDateTime.now()
        );

        emission.setRejectionReason(
                request.getReason().trim()
        );

        /*
         * The previous approved version stays active.
         */
        return mapToResponse(
                carbonEmissionRepository.save(
                        emission
                ),
                currentUserId
        );
    }

    private void validatePendingReview(
            CarbonEmission emission,
            AppUser reviewer
    ) {
        if (emission.getApprovalStatus()
                != CarbonApprovalStatus.PENDING) {

            throw new BadRequestException(
                    "Only pending carbon emissions can be reviewed"
            );
        }

        if (emission.getReviewerId() != null
                && !emission.getReviewerId()
                .equals(
                        reviewer.getId()
                )) {

            throw new BadRequestException(
                    "Only the assigned reviewer can review this emission"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CarbonDashboardResponse getDashboard() {
        List<CarbonEmission> allVersions =
                carbonEmissionRepository
                        .findAll();

        List<CarbonEmission> operationalRecords =
                allVersions.stream()
                        .filter(emission ->
                                emission.isActiveVersion()
                                        || emission.getApprovalStatus()
                                        != CarbonApprovalStatus.APPROVED
                        )
                        .toList();

        return CarbonDashboardResponse.builder()
                .totalRecords(
                        operationalRecords.size()
                )
                .draftRecords(
                        countStatus(
                                operationalRecords,
                                CarbonApprovalStatus.DRAFT
                        )
                )
                .pendingRecords(
                        countStatus(
                                operationalRecords,
                                CarbonApprovalStatus.PENDING
                        )
                )
                .approvedRecords(
                        operationalRecords.stream()
                                .filter(
                                        CarbonEmission::isActiveVersion
                                )
                                .filter(emission ->
                                        emission.getApprovalStatus()
                                                == CarbonApprovalStatus.APPROVED
                                )
                                .count()
                )
                .rejectedRecords(
                        countStatus(
                                operationalRecords,
                                CarbonApprovalStatus.REJECTED
                        )
                )
                .memoRecords(
                        operationalRecords.stream()
                                .filter(
                                        CarbonEmission::isMemo
                                )
                                .count()
                )
                .totalKgCo2e(
                        sum(
                                allVersions,
                                null,
                                CarbonEmissionUnit.KG_CO2E
                        )
                )
                .totalTCo2e(
                        sum(
                                allVersions,
                                null,
                                CarbonEmissionUnit.T_CO2E
                        )
                )
                .scope1KgCo2e(
                        sum(
                                allVersions,
                                CarbonScope.SCOPE_1,
                                CarbonEmissionUnit.KG_CO2E
                        )
                )
                .scope2KgCo2e(
                        sum(
                                allVersions,
                                CarbonScope.SCOPE_2,
                                CarbonEmissionUnit.KG_CO2E
                        )
                )
                .scope3KgCo2e(
                        sum(
                                allVersions,
                                CarbonScope.SCOPE_3,
                                CarbonEmissionUnit.KG_CO2E
                        )
                )
                .scope1TCo2e(
                        sum(
                                allVersions,
                                CarbonScope.SCOPE_1,
                                CarbonEmissionUnit.T_CO2E
                        )
                )
                .scope2TCo2e(
                        sum(
                                allVersions,
                                CarbonScope.SCOPE_2,
                                CarbonEmissionUnit.T_CO2E
                        )
                )
                .scope3TCo2e(
                        sum(
                                allVersions,
                                CarbonScope.SCOPE_3,
                                CarbonEmissionUnit.T_CO2E
                        )
                )
                .build();
    }

    private CarbonEmission requireEmission(
            Long id
    ) {
        return carbonEmissionRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Carbon emission not found with id: "
                                        + id
                        )
                );
    }

    private AppUser requireActiveUser(
            Long userId
    ) {
        AppUser user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + userId
                                )
                        );

        if (user.getStatus()
                != UserStatus.ACTIVE) {

            throw new BadRequestException(
                    "User account is not active"
            );
        }

        return user;
    }

    private Site requireFacility(
            Long facilityId
    ) {
        return siteRepository
                .findById(facilityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Facility not found with id: "
                                        + facilityId
                        )
                );
    }

    private Long validateOptionalReviewer(
            Long reviewerId
    ) {
        if (reviewerId == null) {
            return null;
        }

        return requireActiveUser(
                reviewerId
        ).getId();
    }

    private void validateEditable(
            CarbonEmission emission
    ) {
        if (emission.getApprovalStatus()
                == CarbonApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "Approved carbon emissions cannot be edited. Create a correction instead."
            );
        }

        if (emission.getApprovalStatus()
                == CarbonApprovalStatus.PENDING) {

            throw new BadRequestException(
                    "Pending carbon emissions cannot be edited"
            );
        }

        if (emission.isActiveVersion()) {
            throw new BadRequestException(
                    "Active carbon versions cannot be edited"
            );
        }
    }

    private void validateBusinessKey(
            CarbonEmission emission
    ) {
        boolean conflict =
                carbonEmissionRepository
                        .existsByFacilityIdAndEmissionSourceAndReportingPeriodAndVersionGroupIdNot(
                                emission.getFacilityId(),
                                emission.getEmissionSource(),
                                emission.getReportingPeriod(),
                                emission.getVersionGroupId()
                        );

        if (conflict) {
            throw new BadRequestException(
                    "Another Carbon record group already exists for this facility, source and reporting period"
            );
        }
    }

    private void validateScopeAndSource(
            CarbonScope scope,
            CarbonEmissionSource source
    ) {
        if (scope == null) {
            throw new BadRequestException(
                    "Carbon scope is required"
            );
        }

        if (source == null) {
            throw new BadRequestException(
                    "Emission source is required"
            );
        }

        boolean valid =
                switch (scope) {
                    case SCOPE_1 ->
                            source ==
                                    CarbonEmissionSource.DIESEL_GENERATORS
                                    || source ==
                                    CarbonEmissionSource.REFRIGERANTS
                                    || source ==
                                    CarbonEmissionSource.FIRE_SUPPRESSANT;

                    case SCOPE_2 ->
                            source ==
                                    CarbonEmissionSource.PURCHASED_ELECTRICITY;

                    case SCOPE_3 ->
                            source ==
                                    CarbonEmissionSource.BUSINESS_TRAVEL
                                    || source ==
                                    CarbonEmissionSource.EMPLOYEE_COMMUTING
                                    || source ==
                                    CarbonEmissionSource.WASTE_EMISSION_PREVENTED;
                };

        if (!valid) {
            throw new BadRequestException(
                    "Emission source "
                            + source
                            + " is not valid for "
                            + scope
            );
        }
    }

    private LocalDate parseReportingPeriod(
            String reportingPeriod
    ) {
        try {
            return YearMonth
                    .parse(
                            reportingPeriod.trim()
                    )
                    .atDay(1);

        } catch (Exception exception) {
            throw new BadRequestException(
                    "Reporting period must use the format YYYY-MM"
            );
        }
    }

    private CarbonEmissionResponse mapToResponse(
            CarbonEmission emission,
            Long currentUserId
    ) {
        Site facility =
                siteRepository
                        .findById(
                                emission.getFacilityId()
                        )
                        .orElse(null);

        AppUser submittedBy =
                findUser(
                        emission.getSubmittedByUserId()
                );

        AppUser reviewer =
                findUser(
                        emission.getReviewerId()
                );

        AppUser reviewedBy =
                findUser(
                        emission.getReviewedByUserId()
                );

        CarbonApprovalStatus status =
                emission.getApprovalStatus();

        boolean editable =
                (status == CarbonApprovalStatus.DRAFT
                        || status == CarbonApprovalStatus.REJECTED)
                        && !emission.isActiveVersion();

        boolean assignedReviewer =
                emission.getReviewerId() == null
                        || emission.getReviewerId()
                        .equals(
                                currentUserId
                        );

        boolean canReview =
                status == CarbonApprovalStatus.PENDING
                        && assignedReviewer;

        boolean openCorrectionExists =
                emission.getVersionGroupId() != null
                        && carbonEmissionRepository
                        .existsByVersionGroupIdAndCorrectionTrueAndApprovalStatusIn(
                                emission.getVersionGroupId(),
                                OPEN_CORRECTION_STATUSES
                        );

        return CarbonEmissionResponse.builder()
                .id(emission.getId())
                .scope(emission.getScope())
                .scopeLabel(
                        formatEnum(
                                emission.getScope()
                        )
                )
                .emissionSource(
                        emission.getEmissionSource()
                )
                .emissionSourceLabel(
                        formatEnum(
                                emission.getEmissionSource()
                        )
                )
                .facilityId(
                        emission.getFacilityId()
                )
                .facilityCode(
                        facility != null
                                ? facility.getCode()
                                : null
                )
                .facilityName(
                        facility != null
                                ? facility.getName()
                                : null
                )
                .reportingPeriod(
                        YearMonth
                                .from(
                                        emission.getReportingPeriod()
                                )
                                .toString()
                )
                .emissions(
                        emission.getEmissions()
                )
                .emissionsUnit(
                        emission.getEmissionsUnit()
                )
                .emissionsUnitLabel(
                        emission.getEmissionsUnit()
                                == CarbonEmissionUnit.KG_CO2E
                                ? "kg CO₂e"
                                : "t CO₂e"
                )
                .memo(
                        emission.isMemo()
                )
                .dataSource(
                        emission.getDataSource()
                )
                .dataSourceFileName(
                        emission.getDataSourceFileName()
                )
                .dataSourceFileUrl(
                        emission.getDataSourceFileUrl()
                )
                .dataSourceContentType(
                        emission.getDataSourceContentType()
                )
                .dataSourceFileSize(
                        emission.getDataSourceFileSize()
                )
                .verification(
                        emission.getVerification()
                )
                .verificationLabel(
                        formatEnum(
                                emission.getVerification()
                        )
                )
                .remarks(
                        emission.getRemarks()
                )
                .submittedByUserId(
                        emission.getSubmittedByUserId()
                )
                .submittedByName(
                        submittedBy != null
                                ? submittedBy.getFullName()
                                : null
                )
                .submissionDate(
                        emission.getSubmissionDate()
                )
                .approvalStatus(status)
                .approvalStatusLabel(
                        formatEnum(status)
                )
                .reviewerId(
                        emission.getReviewerId()
                )
                .reviewerName(
                        reviewer != null
                                ? reviewer.getFullName()
                                : null
                )
                .reviewedByUserId(
                        emission.getReviewedByUserId()
                )
                .reviewedByName(
                        reviewedBy != null
                                ? reviewedBy.getFullName()
                                : null
                )
                .reviewDate(
                        emission.getReviewDate()
                )
                .rejectionReason(
                        emission.getRejectionReason()
                )
                .versionGroupId(
                        emission.getVersionGroupId()
                )
                .versionNumber(
                        emission.getVersionNumber()
                )
                .correction(
                        emission.isCorrection()
                )
                .activeVersion(
                        emission.isActiveVersion()
                )
                .supersedesEmissionId(
                        emission.getSupersedesEmissionId()
                )
                .correctionReason(
                        emission.getCorrectionReason()
                )
                .canEdit(editable)
                .canDelete(
                        editable
                                && status
                                == CarbonApprovalStatus.DRAFT
                )
                .canSubmit(editable)
                .canApprove(canReview)
                .canReject(canReview)
                .canCreateCorrection(
                        status
                                == CarbonApprovalStatus.APPROVED
                                && emission.isActiveVersion()
                                && !openCorrectionExists
                )
                .canViewHistory(
                        emission.getVersionGroupId()
                                != null
                )
                .existingRecordUpdated(false)
                .createdBy(
                        emission.getCreatedBy()
                )
                .modifiedBy(
                        emission.getModifiedBy()
                )
                .createdAt(
                        emission.getCreationDate()
                )
                .updatedAt(
                        emission.getModifiedDate()
                )
                .build();
    }

    private AppUser findUser(
            Long userId
    ) {
        if (userId == null) {
            return null;
        }

        return userRepository
                .findById(userId)
                .orElse(null);
    }

    private void clearReviewInformation(
            CarbonEmission emission
    ) {
        emission.setReviewedByUserId(null);
        emission.setReviewDate(null);
        emission.setRejectionReason(null);
    }

    private long countStatus(
            List<CarbonEmission> emissions,
            CarbonApprovalStatus status
    ) {
        return emissions.stream()
                .filter(emission ->
                        emission.getApprovalStatus()
                                == status
                )
                .count();
    }

    private BigDecimal sum(
            List<CarbonEmission> emissions,
            CarbonScope scope,
            CarbonEmissionUnit unit
    ) {
        return emissions.stream()
                .filter(
                        CarbonEmission::isActiveVersion
                )
                .filter(emission ->
                        !emission.isMemo()
                )
                .filter(emission ->
                        emission.getApprovalStatus()
                                == CarbonApprovalStatus.APPROVED
                )
                .filter(emission ->
                        scope == null
                                || emission.getScope()
                                == scope
                )
                .filter(emission ->
                        emission.getEmissionsUnit()
                                == unit
                )
                .map(
                        CarbonEmission::getEmissions
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private String trimToNull(
            String value
    ) {
        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private String formatEnum(
            Enum<?> value
    ) {
        if (value == null) {
            return null;
        }

        String[] words =
                value.name()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .split("_");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            result.append(
                    word.substring(1)
            );
        }

        return result.toString();
    }

    private boolean resolveMemoValue(
            CarbonEmissionSource source,
            Boolean requestedMemo
    ) {
        if (source
                == CarbonEmissionSource
                .WASTE_EMISSION_PREVENTED) {

            return true;
        }

        return Boolean.TRUE.equals(
                requestedMemo
        );
    }

    private void synchronizeApprovedEmission(
            CarbonEmission emission
    ) {
        if (emission == null
                || emission.getApprovalStatus()
                != CarbonApprovalStatus.APPROVED
                || !emission.isActiveVersion()
                || emission.isMemo()) {

            return;
        }

        carbonKpiIntegrationService
                .synchronize(
                        emission
                );
    }
}