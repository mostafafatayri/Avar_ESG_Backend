package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus;
import com.fatayriTech.avarESG.enums.KpiReadingSource;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "kpi_readings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_kpi_reading_period",
                        columnNames = {
                                "kpi_id",
                                "reporting_period"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_kpi_readings_kpi",
                        columnList = "kpi_id"
                ),
                @Index(
                        name = "idx_kpi_readings_period",
                        columnList = "reporting_period"
                ),
                @Index(
                        name = "idx_kpi_readings_approval_status",
                        columnList = "approval_status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiReading extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Rename Kpi below only if your existing KPI entity
     * has a different class name.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "kpi_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_kpi_reading_kpi")
    )
    private Kpi kpi;

    /*
     * Canonical values:
     *
     * Monthly:
     * 2026-01
     *
     * Quarterly:
     * 2026-Q1
     *
     * Semi-annual:
     * 2026-H1
     *
     * Annual:
     * 2026
     */
    @Column(
            name = "reporting_period",
            nullable = false,
            length = 20
    )
    private String reportingPeriod;

    @Column(
            name = "period_start_date",
            nullable = false
    )
    private LocalDate periodStartDate;

    @Column(
            name = "period_end_date",
            nullable = false
    )
    private LocalDate periodEndDate;

    @Column(
            name = "actual_value",
            nullable = false,
            precision = 24,
            scale = 6
    )
    private BigDecimal actualValue;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "source",
            nullable = false,
            length = 40
    )
    private KpiReadingSource source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "submitted_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_kpi_reading_submitted_by"
            )
    )
    private AppUser submittedBy;

    @Column(
            name = "submission_date",
            nullable = false
    )
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_status",
            nullable = false,
            length = 20
    )
    private KpiReadingApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reviewed_by_user_id",
            foreignKey = @ForeignKey(
                    name = "fk_kpi_reading_reviewed_by"
            )
    )
    private AppUser reviewedBy;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;

    /*
     * Optional evidence metadata.
     *
     * The actual file can later be stored in Supabase,
     * S3, or your attachment service.
     */
    @Column(
            name = "evidence_file_name",
            length = 255
    )
    private String evidenceFileName;

    @Column(
            name = "evidence_file_url",
            length = 2000
    )
    private String evidenceFileUrl;

    @Column(
            name = "evidence_content_type",
            length = 150
    )
    private String evidenceContentType;

    @Column(name = "evidence_file_size")
    private Long evidenceFileSize;
}