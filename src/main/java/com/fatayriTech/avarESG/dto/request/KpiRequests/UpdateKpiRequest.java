package com.fatayriTech.avarESG.dto.request.KpiRequests;

import com.fatayriTech.avarESG.enums.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateKpiRequest {

    @Size(max = 180)
    private String name;

    @Size(max = 80)
    private String code;

    private EsgPillar pillar;
    private EsgCategory category;

    /*
     * Include siteSelectionProvided=true to intentionally change scope.
     * siteId null means all sites.
     */
    private Boolean siteSelectionProvided;
    private Long siteId;

    @Size(max = 5000)
    private String description;

    @Size(max = 80)
    private String unitOfMeasure;

    private ReportingFrequency reportingFrequency;
    private BigDecimal baselineValue;
    private BigDecimal targetValue;
    private TargetDirection targetDirection;
    private KpiDataSource dataSource;
    private LocalDate effectiveDate;
    private Long responsibleOwnerId;
    private Set<ReportingFramework> frameworks;
    private Boolean approvalRequired;
}