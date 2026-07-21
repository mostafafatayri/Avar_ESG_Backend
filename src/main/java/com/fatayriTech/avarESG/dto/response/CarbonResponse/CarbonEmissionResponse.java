package com.fatayriTech.avarESG.dto.response.CarbonResponse;

import com.fatayriTech.avarESG.enums.CarbonApprovalStatus;
import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.enums.CarbonVerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CarbonEmissionResponse {

    private Long id;

    private CarbonScope scope;
    private String scopeLabel;

    private CarbonEmissionSource emissionSource;
    private String emissionSourceLabel;

    private Long facilityId;
    private String facilityCode;
    private String facilityName;

    private String reportingPeriod;

    private BigDecimal emissions;

    private CarbonEmissionUnit emissionsUnit;
    private String emissionsUnitLabel;

    private boolean memo;

    private String dataSource;

    private String dataSourceFileName;
    private String dataSourceFileUrl;
    private String dataSourceContentType;
    private Long dataSourceFileSize;

    private CarbonVerificationStatus verification;
    private String verificationLabel;

    private String remarks;

    private Long submittedByUserId;
    private String submittedByName;
    private LocalDateTime submissionDate;

    private CarbonApprovalStatus approvalStatus;
    private String approvalStatusLabel;

    private Long reviewerId;
    private String reviewerName;

    private Long reviewedByUserId;
    private String reviewedByName;
    private LocalDateTime reviewDate;

    private String rejectionReason;

    /*
     * Version and correction information.
     */
    private String versionGroupId;
    private Integer versionNumber;

    private boolean correction;
    private boolean activeVersion;

    private Long supersedesEmissionId;
    private String correctionReason;

    /*
     * Frontend permissions.
     */
    private boolean canEdit;
    private boolean canDelete;
    private boolean canSubmit;
    private boolean canApprove;
    private boolean canReject;

    private boolean canCreateCorrection;
    private boolean canViewHistory;

    private String createdBy;
    private String modifiedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
     * Kept temporarily for compatibility with the
     * previous create-or-update implementation.
     *
     * We will remove it after Phase 2.
     */
    private boolean existingRecordUpdated;
}