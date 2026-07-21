package com.fatayriTech.avarESG.model;

import com.fatayriTech.avarESG.enums.CarbonApprovalStatus;
import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.enums.CarbonVerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "carbon_emissions",
        uniqueConstraints = {
                /*
                 * A version group represents one logical Carbon record.
                 *
                 * Example:
                 * Diesel Generators + HO + October 2026
                 *
                 * Version 1 and Version 2 belong to the same group.
                 */
                @UniqueConstraint(
                        name = "uk_carbon_version_group_number",
                        columnNames = {
                                "version_group_id",
                                "version_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_carbon_scope",
                        columnList = "scope"
                ),
                @Index(
                        name = "idx_carbon_facility",
                        columnList = "facility_id"
                ),
                @Index(
                        name = "idx_carbon_period",
                        columnList = "reporting_period"
                ),
                @Index(
                        name = "idx_carbon_approval_status",
                        columnList = "approval_status"
                ),
                @Index(
                        name = "idx_carbon_reviewer",
                        columnList = "reviewer_id"
                ),
                @Index(
                        name = "idx_carbon_version_group",
                        columnList = "version_group_id"
                ),
                @Index(
                        name = "idx_carbon_active_version",
                        columnList = "active_version"
                ),
                @Index(
                        name = "idx_carbon_business_key",
                        columnList =
                                "facility_id, emission_source, reporting_period"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonEmission
        extends BaseAuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private CarbonScope scope;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "emission_source",
            nullable = false,
            length = 80
    )
    private CarbonEmissionSource emissionSource;

    @Column(
            name = "facility_id",
            nullable = false
    )
    private Long facilityId;

    /*
     * The first day of the reporting month is stored.
     *
     * Example:
     * 2026-03 is stored as 2026-03-01.
     */
    @Column(
            name = "reporting_period",
            nullable = false
    )
    private LocalDate reportingPeriod;

    @Column(
            nullable = false,
            precision = 20,
            scale = 6
    )
    private BigDecimal emissions;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "emissions_unit",
            nullable = false,
            length = 30
    )
    private CarbonEmissionUnit emissionsUnit;

    @Builder.Default
    @Column(
            name = "is_memo",
            nullable = false
    )
    private boolean memo = false;

    @Column(
            name = "data_source",
            length = 1000
    )
    private String dataSource;

    @Column(
            name = "data_source_file_name",
            length = 500
    )
    private String dataSourceFileName;

    @Column(
            name = "data_source_file_url",
            length = 2000
    )
    private String dataSourceFileUrl;

    @Column(
            name = "data_source_content_type",
            length = 255
    )
    private String dataSourceContentType;

    @Column(
            name = "data_source_file_size"
    )
    private Long dataSourceFileSize;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private CarbonVerificationStatus verification;

    @Column(
            columnDefinition = "TEXT"
    )
    private String remarks;

    @Column(
            name = "submitted_by_user_id"
    )
    private Long submittedByUserId;

    @Column(
            name = "submission_date"
    )
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_status",
            nullable = false,
            length = 30
    )
    private CarbonApprovalStatus approvalStatus;

    @Column(
            name = "reviewer_id"
    )
    private Long reviewerId;

    @Column(
            name = "reviewed_by_user_id"
    )
    private Long reviewedByUserId;

    @Column(
            name = "review_date"
    )
    private LocalDateTime reviewDate;

    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;

    /*
     * All versions of the same logical Carbon record
     * share the same version group.
     */
    @Column(
            name = "version_group_id",
            nullable = false,
            length = 36
    )
    private String versionGroupId;

    /*
     * Original record = version 1.
     * First correction = version 2.
     */
    @Builder.Default
    @Column(
            name = "version_number",
            nullable = false
    )
    private Integer versionNumber = 1;

    /*
     * false = original record
     * true = correction version
     */
    @Builder.Default
    @Column(
            name = "is_correction",
            nullable = false
    )
    private boolean correction = false;

    /*
     * Only one approved version in a version group
     * should be active.
     *
     * Carbon totals and KPI synchronization must use
     * activeVersion = true only.
     */
    @Builder.Default
    @Column(
            name = "active_version",
            nullable = false
    )
    private boolean activeVersion = false;

    /*
     * ID of the record directly replaced by this version.
     *
     * Example:
     * v2 supersedes v1
     * v3 supersedes v2
     */
    @Column(
            name = "supersedes_emission_id"
    )
    private Long supersedesEmissionId;

    @Column(
            name = "correction_reason",
            length = 1000
    )
    private String correctionReason;

    @PrePersist
    protected void applyDefaults() {
        if (approvalStatus == null) {
            approvalStatus =
                    CarbonApprovalStatus.DRAFT;
        }

        if (verification == null) {
            verification =
                    CarbonVerificationStatus.UNVERIFIED;
        }

        if (emissionsUnit == null) {
            emissionsUnit =
                    CarbonEmissionUnit.KG_CO2E;
        }

        if (versionGroupId == null
                || versionGroupId.isBlank()) {

            versionGroupId =
                    UUID.randomUUID()
                            .toString();
        }

        if (versionNumber == null
                || versionNumber < 1) {

            versionNumber = 1;
        }
    }
}