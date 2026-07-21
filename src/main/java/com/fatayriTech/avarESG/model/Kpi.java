package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "kpis",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_kpi_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(name = "idx_kpi_pillar", columnList = "pillar"),
                @Index(name = "idx_kpi_category", columnList = "category"),
                @Index(name = "idx_kpi_status", columnList = "status"),
                @Index(name = "idx_kpi_site", columnList = "site_id"),
                @Index(name = "idx_kpi_owner", columnList = "responsible_owner_id"),

        }

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kpi extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 80)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EsgPillar pillar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private EsgCategory category;

    /*
     * Null means portfolio-level KPI covering all sites.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "site_id",
            foreignKey = @ForeignKey(name = "fk_kpi_site")
    )
    private Site site;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_of_measure", nullable = false, length = 80)
    private String unitOfMeasure;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporting_frequency", nullable = false, length = 30)
    private ReportingFrequency reportingFrequency;

    @Column(name = "baseline_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal baselineValue;

    @Column(name = "target_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal targetValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_direction", nullable = false, length = 20)
    private TargetDirection targetDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 40)
    private KpiDataSource dataSource;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "responsible_owner_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_kpi_responsible_owner")
    )
    private AppUser responsibleOwner;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "kpi_framework_mappings",
            joinColumns = @JoinColumn(name = "kpi_id"),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_kpi_framework",
                            columnNames = {"kpi_id", "framework"}
                    )
            }
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "framework", nullable = false, length = 40)
    @Builder.Default
    private Set<ReportingFramework> frameworks = new HashSet<>();

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KpiStatus status;

    @Column(name = "submitted_for_approval_at")
    private java.time.LocalDateTime submittedForApprovalAt;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "rejected_at")
    private java.time.LocalDateTime rejectedAt;

    @Column(name = "rejected_by_user_id")
    private Long rejectedByUserId;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

}