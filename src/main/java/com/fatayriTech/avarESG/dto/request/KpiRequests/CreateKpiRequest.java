package com.fatayriTech.avarESG.dto.request.KpiRequests;

import com.fatayriTech.avarESG.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class CreateKpiRequest {

    @NotBlank(message = "KPI name is required")
    @Size(max = 180)
    private String name;

    @NotBlank(message = "KPI code is required")
    @Size(max = 80)
    private String code;

    @NotNull(message = "ESG pillar is required")
    private EsgPillar pillar;

    @NotNull(message = "ESG category is required")
    private EsgCategory category;

    /*
     * Null means all sites.
     */
    private Long siteId;

    @Size(max = 5000)
    private String description;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 80)
    private String unitOfMeasure;

    @NotNull(message = "Reporting frequency is required")
    private ReportingFrequency reportingFrequency;

    @NotNull(message = "Baseline value is required")
    private BigDecimal baselineValue;

    @NotNull(message = "Target value is required")
    private BigDecimal targetValue;

    @NotNull(message = "Target direction is required")
    private TargetDirection targetDirection;

    @NotNull(message = "Data source is required")
    private KpiDataSource dataSource;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotNull(message = "Responsible owner is required")
    private Long responsibleOwnerId;

    private Set<ReportingFramework> frameworks = new HashSet<>();

    private boolean approvalRequired;
}