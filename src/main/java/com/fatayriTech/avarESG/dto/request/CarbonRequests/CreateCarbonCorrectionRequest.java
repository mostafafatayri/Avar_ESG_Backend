package com.fatayriTech.avarESG.dto.request.CarbonRequests;

import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonVerificationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCarbonCorrectionRequest {

    @NotNull(
            message = "Corrected emissions value is required"
    )
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

    /*
     * Corrections must be independently reviewed.
     * They cannot use automatic approval.
     */
    @NotNull(
            message = "A reviewer is required for a correction"
    )
    private Long reviewerId;

    @NotBlank(
            message = "Correction reason is required"
    )
    @Size(
            max = 1000,
            message = "Correction reason cannot exceed 1000 characters"
    )
    private String correctionReason;
}