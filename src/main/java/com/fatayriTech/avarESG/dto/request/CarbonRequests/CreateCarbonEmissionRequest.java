package com.fatayriTech.avarESG.dto.request.CarbonRequests;

import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.enums.CarbonVerificationStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCarbonEmissionRequest {

    @NotNull(message = "Scope is required")
    private CarbonScope scope;

    @NotNull(
            message = "Emission source is required"
    )
    private CarbonEmissionSource emissionSource;

    @NotNull(message = "Facility is required")
    private Long facilityId;

    @NotBlank(
            message = "Reporting period is required"
    )
    @Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
            message = "Reporting period must use the format YYYY-MM"
    )
    private String reportingPeriod;

    @NotNull(message = "Emissions are required")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Emissions cannot be negative"
    )
    private BigDecimal emissions;

    @NotNull(
            message = "Emissions unit is required"
    )
    private CarbonEmissionUnit emissionsUnit;

    private Boolean memo;

    @Size(
            max = 1000,
            message = "Data source cannot exceed 1000 characters"
    )
    private String dataSource;

    private CarbonVerificationStatus verification;

    private String remarks;

    private Long reviewerId;
}