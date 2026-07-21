package com.fatayriTech.avarESG.dto.response.ObjectResponse;


import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import com.fatayriTech.avarESG.enums.ObjectiveStrategicPriority;
import com.fatayriTech.avarESG.enums.ReportingFramework;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveResponse {

    private Long id;

    private String objectiveTitle;

    private String objectiveDescription;

    private EsgPillar esgPillar;

    private String esgCategory;

    private Long relatedKpiId;

    private String relatedKpiName;

    private String relatedKpiUnit;

    private BigDecimal baselineValue;

    private BigDecimal targetValue;

    /*
     * Dynamically resolved from the linked KPI.
     */
    private BigDecimal currentValue;

    private LocalDate targetDate;

    private Long responsibleOwnerId;

    private String responsibleOwnerName;

    private ObjectiveProgressStatus progressStatus;

    private ObjectiveApprovalStatus approvalStatus;

    /*
     * Dynamically calculated using baseline, target and live current value.
     */
    private BigDecimal progressPercentage;

    private ObjectiveStrategicPriority strategicPriority;

    @Builder.Default
    private Set<ReportingFramework> frameworkReferences =
            new LinkedHashSet<>();

    private String remarks;

    /*
     * Reverse relationship from Initiative.
     * Objective does not maintain this relationship directly.
     */
    @Builder.Default
    private Set<Long> supportingInitiativeIds =
            new LinkedHashSet<>();

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}