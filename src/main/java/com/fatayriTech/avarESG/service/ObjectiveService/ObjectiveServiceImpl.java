package com.fatayriTech.avarESG.service.ObjectiveService;



import com.fatayriTech.avarESG.dto.request.ObjectiveRequest.CreateObjectiveRequest;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveResponse;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveStatisticsResponse;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveSummaryResponse;
import com.fatayriTech.avarESG.dto.request.ObjectiveRequest.UpdateObjectiveRequest;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import com.fatayriTech.avarESG.enums.ObjectiveStrategicPriority;
import com.fatayriTech.avarESG.model.Objective;
import com.fatayriTech.avarESG.repository.ObjectiveRepository;
import com.fatayriTech.avarESG.service.ObjectiveService.ObjectiveKpiResolver;
import com.fatayriTech.avarESG.service.ObjectiveService.ObjectiveService;
import com.fatayriTech.avarESG.specification.ObjectiveSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
@Transactional
public class ObjectiveServiceImpl
        implements ObjectiveService {

    private final ObjectiveRepository objectiveRepository;
    private final ObjectiveKpiResolver objectiveKpiResolver;

    @Override
    public ObjectiveResponse createObjective(
            CreateObjectiveRequest request,
            Long currentUserId
    ) {
        validateUniqueTitle(
                request.getObjectiveTitle(),
                null
        );

        validateKpi(request.getRelatedKpiId());

        Objective objective = Objective.builder()
                .objectiveTitle(
                        normalizeRequiredText(
                                request.getObjectiveTitle()
                        )
                )
                .objectiveDescription(
                        normalizeNullableText(
                                request.getObjectiveDescription()
                        )
                )
                .esgPillar(request.getEsgPillar())
                .esgCategory(
                        normalizeRequiredText(
                                request.getEsgCategory()
                        )
                )
                .relatedKpiId(
                        request.getRelatedKpiId()
                )
                .baselineValue(
                        request.getBaselineValue()
                )
                .targetValue(
                        request.getTargetValue()
                )
                .targetDate(
                        request.getTargetDate()
                )
                .responsibleOwnerId(
                        request.getResponsibleOwnerId()
                )
                .progressStatus(
                        request.getProgressStatus() == null
                                ? ObjectiveProgressStatus.PLANNED
                                : request.getProgressStatus()
                )
                .approvalStatus(
                        request.getApprovalStatus() == null
                                ? ObjectiveApprovalStatus.DRAFT
                                : request.getApprovalStatus()
                )
                .strategicPriority(
                        request.getStrategicPriority() == null
                                ? ObjectiveStrategicPriority.MEDIUM
                                : request.getStrategicPriority()
                )
                .frameworkReferences(
                        request.getFrameworkReferences() == null
                                ? new LinkedHashSet<>()
                                : new LinkedHashSet<>(
                                request.getFrameworkReferences()
                        )
                )
                .remarks(
                        normalizeNullableText(
                                request.getRemarks()
                        )
                )
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .deleted(false)
                .build();

        Objective savedObjective =
                objectiveRepository.save(objective);

        return mapToResponse(savedObjective);
    }

    @Override
    public ObjectiveResponse updateObjective(
            Long objectiveId,
            UpdateObjectiveRequest request,
            Long currentUserId
    ) {
        Objective objective =
                getExistingObjective(objectiveId);

        validateUniqueTitle(
                request.getObjectiveTitle(),
                objectiveId
        );

        validateKpi(request.getRelatedKpiId());

        objective.setObjectiveTitle(
                normalizeRequiredText(
                        request.getObjectiveTitle()
                )
        );

        objective.setObjectiveDescription(
                normalizeNullableText(
                        request.getObjectiveDescription()
                )
        );

        objective.setEsgPillar(
                request.getEsgPillar()
        );

        objective.setEsgCategory(
                normalizeRequiredText(
                        request.getEsgCategory()
                )
        );

        objective.setRelatedKpiId(
                request.getRelatedKpiId()
        );

        objective.setBaselineValue(
                request.getBaselineValue()
        );

        objective.setTargetValue(
                request.getTargetValue()
        );

        objective.setTargetDate(
                request.getTargetDate()
        );

        objective.setResponsibleOwnerId(
                request.getResponsibleOwnerId()
        );

        objective.setProgressStatus(
                request.getProgressStatus()
        );

        objective.setApprovalStatus(
                request.getApprovalStatus()
        );

        objective.setStrategicPriority(
                request.getStrategicPriority()
        );

        objective.setFrameworkReferences(
                request.getFrameworkReferences() == null
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(
                        request.getFrameworkReferences()
                )
        );

        objective.setRemarks(
                normalizeNullableText(
                        request.getRemarks()
                )
        );

        objective.setUpdatedBy(currentUserId);

        Objective savedObjective =
                objectiveRepository.save(objective);

        return mapToResponse(savedObjective);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectiveResponse getObjectiveById(
            Long objectiveId
    ) {
        Objective objective =
                getExistingObjective(objectiveId);

        return mapToResponse(objective);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ObjectiveSummaryResponse> getObjectives(
            String search,
            EsgPillar pillar,
            ObjectiveProgressStatus progressStatus,
            ObjectiveApprovalStatus approvalStatus,
            Pageable pageable
    ) {
        Specification<Objective> specification =
                Specification
                        .where(
                                ObjectiveSpecification.notDeleted()
                        )
                        .and(
                                ObjectiveSpecification.search(
                                        search
                                )
                        )
                        .and(
                                ObjectiveSpecification.hasPillar(
                                        pillar
                                )
                        )
                        .and(
                                ObjectiveSpecification.hasProgressStatus(
                                        progressStatus
                                )
                        )
                        .and(
                                ObjectiveSpecification.hasApprovalStatus(
                                        approvalStatus
                                )
                        );

        return objectiveRepository
                .findAll(specification, pageable)
                .map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectiveStatisticsResponse getStatistics() {
        long total =
                objectiveRepository.countByDeletedFalse();

        long planned =
                objectiveRepository
                        .countByProgressStatusAndDeletedFalse(
                                ObjectiveProgressStatus.PLANNED
                        );

        long inProgress =
                objectiveRepository
                        .countByProgressStatusAndDeletedFalse(
                                ObjectiveProgressStatus.IN_PROGRESS
                        );

        long onHold =
                objectiveRepository
                        .countByProgressStatusAndDeletedFalse(
                                ObjectiveProgressStatus.ON_HOLD
                        );

        long completed =
                objectiveRepository
                        .countByProgressStatusAndDeletedFalse(
                                ObjectiveProgressStatus.COMPLETED
                        );

        long cancelled =
                objectiveRepository
                        .countByProgressStatusAndDeletedFalse(
                                ObjectiveProgressStatus.CANCELLED
                        );

        /*
         * Initial implementation.
         * We can later replace this with an optimized repository query.
         */
        long overdue =
                objectiveRepository
                        .findAllByDeletedFalseOrderByCreatedAtDesc()
                        .stream()
                        .filter(this::isOverdue)
                        .count();

        return ObjectiveStatisticsResponse.builder()
                .total(total)
                .planned(planned)
                .inProgress(inProgress)
                .onHold(onHold)
                .completed(completed)
                .cancelled(cancelled)
                .overdue(overdue)
                .build();
    }

    @Override
    public void deleteObjective(
            Long objectiveId,
            Long currentUserId
    ) {
        Objective objective =
                getExistingObjective(objectiveId);

        /*
         * Later we will validate that no active Initiative
         * is linked to this Objective before deletion.
         */

        objective.setDeleted(true);
        objective.setUpdatedBy(currentUserId);

        objectiveRepository.save(objective);
    }

    private Objective getExistingObjective(
            Long objectiveId
    ) {
        return objectiveRepository
                .findByIdAndDeletedFalse(objectiveId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Objective not found with ID: "
                                        + objectiveId
                        )
                );
    }

    private void validateUniqueTitle(
            String title,
            Long objectiveId
    ) {
        String normalizedTitle =
                normalizeRequiredText(title);

        boolean exists;

        if (objectiveId == null) {
            exists =
                    objectiveRepository
                            .existsByObjectiveTitleIgnoreCaseAndDeletedFalse(
                                    normalizedTitle
                            );
        } else {
            exists =
                    objectiveRepository
                            .existsByObjectiveTitleIgnoreCaseAndIdNotAndDeletedFalse(
                                    normalizedTitle,
                                    objectiveId
                            );
        }

        if (exists) {
            throw new IllegalArgumentException(
                    "An objective with this title already exists"
            );
        }
    }

    private void validateKpi(Long relatedKpiId) {
        if (
                relatedKpiId == null ||
                        !objectiveKpiResolver.existsById(
                                relatedKpiId
                        )
        ) {
            throw new IllegalArgumentException(
                    "The selected KPI does not exist"
            );
        }
    }

    private ObjectiveResponse mapToResponse(
            Objective objective
    ) {
        BigDecimal currentValue =
                objectiveKpiResolver.getLatestValue(
                        objective.getRelatedKpiId()
                );

        return ObjectiveResponse.builder()
                .id(objective.getId())
                .objectiveTitle(
                        objective.getObjectiveTitle()
                )
                .objectiveDescription(
                        objective.getObjectiveDescription()
                )
                .esgPillar(
                        objective.getEsgPillar()
                )
                .esgCategory(
                        objective.getEsgCategory()
                )
                .relatedKpiId(
                        objective.getRelatedKpiId()
                )
                .relatedKpiName(
                        objectiveKpiResolver.getKpiName(
                                objective.getRelatedKpiId()
                        )
                )
                .relatedKpiUnit(
                        objectiveKpiResolver.getKpiUnit(
                                objective.getRelatedKpiId()
                        )
                )
                .baselineValue(
                        objective.getBaselineValue()
                )
                .targetValue(
                        objective.getTargetValue()
                )
                .currentValue(currentValue)
                .targetDate(
                        objective.getTargetDate()
                )
                .responsibleOwnerId(
                        objective.getResponsibleOwnerId()
                )
                /*
                 * Will be resolved from AppUser after you send
                 * your current user repository/service.
                 */
                .responsibleOwnerName(null)
                .progressStatus(
                        objective.getProgressStatus()
                )
                .approvalStatus(
                        objective.getApprovalStatus()
                )
                .progressPercentage(
                        calculateProgressPercentage(
                                objective.getBaselineValue(),
                                objective.getTargetValue(),
                                currentValue
                        )
                )
                .strategicPriority(
                        objective.getStrategicPriority()
                )
                .frameworkReferences(
                        new LinkedHashSet<>(
                                objective.getFrameworkReferences()
                        )
                )
                .supportingInitiativeIds(
                        new LinkedHashSet<>()
                )
                .remarks(objective.getRemarks())
                .createdBy(objective.getCreatedBy())
                .updatedBy(objective.getUpdatedBy())
                .createdAt(objective.getCreatedAt())
                .updatedAt(objective.getUpdatedAt())
                .build();
    }

    private ObjectiveSummaryResponse mapToSummaryResponse(
            Objective objective
    ) {
        BigDecimal currentValue =
                objectiveKpiResolver.getLatestValue(
                        objective.getRelatedKpiId()
                );

        return ObjectiveSummaryResponse.builder()
                .id(objective.getId())
                .objectiveTitle(
                        objective.getObjectiveTitle()
                )
                .esgPillar(
                        objective.getEsgPillar()
                )
                .relatedKpiId(
                        objective.getRelatedKpiId()
                )
                .relatedKpiName(
                        objectiveKpiResolver.getKpiName(
                                objective.getRelatedKpiId()
                        )
                )
                .relatedKpiUnit(
                        objectiveKpiResolver.getKpiUnit(
                                objective.getRelatedKpiId()
                        )
                )
                .baselineValue(
                        objective.getBaselineValue()
                )
                .targetValue(
                        objective.getTargetValue()
                )
                .currentValue(currentValue)
                .targetDate(
                        objective.getTargetDate()
                )
                .progressStatus(
                        objective.getProgressStatus()
                )
                .progressPercentage(
                        calculateProgressPercentage(
                                objective.getBaselineValue(),
                                objective.getTargetValue(),
                                currentValue
                        )
                )
                .build();
    }

    private BigDecimal calculateProgressPercentage(
            BigDecimal baseline,
            BigDecimal target,
            BigDecimal current
    ) {
        if (
                baseline == null ||
                        target == null ||
                        current == null
        ) {
            return BigDecimal.ZERO;
        }

        BigDecimal denominator =
                baseline.subtract(target);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(target) == 0
                    ? new BigDecimal("100.00")
                    : BigDecimal.ZERO;
        }

        BigDecimal numerator =
                baseline.subtract(current);

        BigDecimal percentage =
                numerator
                        .divide(
                                denominator,
                                6,
                                RoundingMode.HALF_UP
                        )
                        .multiply(
                                new BigDecimal("100")
                        );

        if (
                percentage.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            return BigDecimal.ZERO;
        }

        if (
                percentage.compareTo(
                        new BigDecimal("100")
                ) > 0
        ) {
            return new BigDecimal("100.00");
        }

        return percentage.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private boolean isOverdue(
            Objective objective
    ) {
        if (objective.getTargetDate() == null) {
            return false;
        }

        if (
                objective.getProgressStatus()
                        == ObjectiveProgressStatus.COMPLETED ||
                        objective.getProgressStatus()
                                == ObjectiveProgressStatus.CANCELLED
        ) {
            return false;
        }

        return objective
                .getTargetDate()
                .isBefore(LocalDate.now());
    }

    private String normalizeRequiredText(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}