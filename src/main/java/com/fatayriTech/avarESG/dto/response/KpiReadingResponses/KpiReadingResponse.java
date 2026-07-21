package com.fatayriTech.avarESG.dto.response.KpiReadingResponses;

import com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus;
import com.fatayriTech.avarESG.enums.KpiReadingSource;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiReadingResponse {

    private Long id;

    private Long kpiId;

    private String kpiCode;

    private String kpiName;

    private String reportingPeriod;

    private String reportingPeriodLabel;

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;

    private BigDecimal actualValue;

    private String unitOfMeasure;

    private KpiReadingSource source;

    private Long submittedByUserId;

    private String submittedByUsername;

    private String submittedByFullName;

    private LocalDateTime submissionDate;

    private KpiReadingApprovalStatus approvalStatus;

    private Long reviewedByUserId;

    private String reviewedByFullName;

    private LocalDateTime reviewDate;

    private String rejectionReason;

    private String evidenceFileName;

    private String evidenceFileUrl;

    private String evidenceContentType;

    private Long evidenceFileSize;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}