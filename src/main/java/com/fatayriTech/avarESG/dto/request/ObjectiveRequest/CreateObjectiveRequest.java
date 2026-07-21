package com.fatayriTech.avarESG.dto.request.ObjectiveRequest;

import com.fatayriTech.avarESG.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateObjectiveRequest {

    @NotBlank(message = "Objective title is required")
    @Size(
            max = 255,
            message = "Objective title cannot exceed 255 characters"
    )
    private String objectiveTitle;

    private String objectiveDescription;

    @NotNull(message = "ESG pillar is required")
    private EsgPillar esgPillar;

    @NotBlank(message = "ESG category is required")
    @Size(
            max = 150,
            message = "ESG category cannot exceed 150 characters"
    )
    private String esgCategory;

    @NotNull(message = "Related KPI is required")
    @Positive(message = "Related KPI ID must be positive")
    private Long relatedKpiId;

    @NotNull(message = "Baseline value is required")
    private BigDecimal baselineValue;

    @NotNull(message = "Target value is required")
    private BigDecimal targetValue;

    @NotNull(message = "Target date is required")
    private LocalDate targetDate;

    @NotNull(message = "Responsible owner is required")
    @Positive(message = "Responsible owner ID must be positive")
    private Long responsibleOwnerId;

    private ObjectiveProgressStatus progressStatus;

    private ObjectiveApprovalStatus approvalStatus;

    private ObjectiveStrategicPriority strategicPriority;

    @Builder.Default
    private Set<ReportingFramework> frameworkReferences =
            new LinkedHashSet<>();

    private String remarks;
}