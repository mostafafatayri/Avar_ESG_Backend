package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import com.fatayriTech.avarESG.enums.ObjectiveStrategicPriority;
import com.fatayriTech.avarESG.enums.ReportingFramework;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "objectives",
        indexes = {
                @Index(
                        name = "idx_objective_pillar",
                        columnList = "esg_pillar"
                ),
                @Index(
                        name = "idx_objective_category",
                        columnList = "esg_category"
                ),
                @Index(
                        name = "idx_objective_related_kpi",
                        columnList = "related_kpi_id"
                ),
                @Index(
                        name = "idx_objective_owner",
                        columnList = "responsible_owner_id"
                ),
                @Index(
                        name = "idx_objective_progress_status",
                        columnList = "progress_status"
                ),
                @Index(
                        name = "idx_objective_approval_status",
                        columnList = "approval_status"
                ),
                @Index(
                        name = "idx_objective_target_date",
                        columnList = "target_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Objective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "objective_title",
            nullable = false,
            length = 255
    )
    private String objectiveTitle;

    @Column(
            name = "objective_description",
            columnDefinition = "TEXT"
    )
    private String objectiveDescription;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "esg_pillar",
            nullable = false,
            length = 30
    )
    private EsgPillar esgPillar;

    /*
     * Kept as text because ESG categories are normally configurable
     * and may be filtered according to the selected pillar.
     *
     * It can later be converted into a relation with an ESG category table.
     */
    @Column(
            name = "esg_category",
            nullable = false,
            length = 150
    )
    private String esgCategory;

    /*
     * Logical reference to the KPI module.
     * No JPA relation is used to avoid tightly coupling the two modules.
     */
    @Column(
            name = "related_kpi_id",
            nullable = false
    )
    private Long relatedKpiId;

    @Column(
            name = "baseline_value",
            nullable = false,
            precision = 24,
            scale = 6
    )
    private BigDecimal baselineValue;

    @Column(
            name = "target_value",
            nullable = false,
            precision = 24,
            scale = 6
    )
    private BigDecimal targetValue;

    /*
     * Current value is deliberately NOT stored here.
     * It is retrieved live from the linked KPI.
     */

    @Column(
            name = "target_date",
            nullable = false
    )
    private LocalDate targetDate;

    /*
     * Logical reference to the responsible platform user.
     */
    @Column(
            name = "responsible_owner_id",
            nullable = false
    )
    private Long responsibleOwnerId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "progress_status",
            nullable = false,
            length = 30
    )
    private ObjectiveProgressStatus progressStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_status",
            nullable = false,
            length = 30
    )
    private ObjectiveApprovalStatus approvalStatus;

    /*
     * Progress percentage is derived when returning the objective.
     * It is not persisted because currentValue changes with the KPI.
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "strategic_priority",
            nullable = false,
            length = 20
    )
    private ObjectiveStrategicPriority strategicPriority;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "objective_framework_references",
            joinColumns = @JoinColumn(
                    name = "objective_id",
                    foreignKey = @ForeignKey(
                            name = "fk_objective_framework_objective"
                    )
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(
            name = "framework_reference",
            nullable = false,
            length = 50
    )
    @Builder.Default
    private Set<ReportingFramework> frameworkReferences =
            new LinkedHashSet<>();

    @Column(
            name = "remarks",
            columnDefinition = "TEXT"
    )
    private String remarks;

    @Column(
            name = "created_by"
    )
    private Long createdBy;

    @Column(
            name = "updated_by"
    )
    private Long updatedBy;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "deleted",
            nullable = false
    )
    @Builder.Default
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (progressStatus == null) {
            progressStatus =
                    ObjectiveProgressStatus.PLANNED;
        }

        if (approvalStatus == null) {
            approvalStatus =
                    ObjectiveApprovalStatus.DRAFT;
        }

        if (strategicPriority == null) {
            strategicPriority =
                    ObjectiveStrategicPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}