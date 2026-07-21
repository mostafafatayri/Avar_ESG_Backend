package com.fatayriTech.avarESG.dto.response.KpiResponse;

import com.fatayriTech.avarESG.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class KpiResponse {

    private Long id;
    private String name;
    private String code;

    private EsgPillar pillar;
    private EsgCategory category;

    private Long siteId;
    private String siteName;
    private boolean allSites;

    private String description;
    private String unitOfMeasure;
    private ReportingFrequency reportingFrequency;

    private BigDecimal baselineValue;
    private BigDecimal targetValue;
    private TargetDirection targetDirection;

    private KpiDataSource dataSource;
    private LocalDate effectiveDate;

    private Long responsibleOwnerId;
    private String responsibleOwnerName;
    private String responsibleOwnerEmail;

    private Set<ReportingFramework> frameworks;

    private boolean approvalRequired;
    private KpiStatus status;

    private BigDecimal latestActualValue;
    private LocalDate latestReadingDate;




    private LocalDate nextReadingDueDate;
    private BigDecimal completion;

    private KpiCompletionStatus completionStatus;

    private Integer expectedReadingsCount;

    private Integer completedReadingsCount;

    private LocalDate currentPeriodStartDate;

    private LocalDate currentPeriodEndDate;

    private List<KpiReadingResponse> readings;

    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}