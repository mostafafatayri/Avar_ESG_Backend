package com.fatayriTech.avarESG.dto.request.CarbonRequests;

import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.enums.CarbonVerificationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCarbonEmissionRequest {

    private CarbonScope scope;

    private CarbonEmissionSource emissionSource;

    private Long facilityId;

    @Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
            message = "Reporting period must use the format YYYY-MM"
    )
    private String reportingPeriod;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Emissions cannot be negative"
    )
    private BigDecimal emissions;

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