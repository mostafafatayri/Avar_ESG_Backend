package com.fatayriTech.avarESG.dto.request.KpiReadingRequests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateKpiReadingRequest {

    @NotBlank(message = "Reporting period is required")
    @Size(
            max = 20,
            message = "Reporting period cannot exceed 20 characters"
    )
    private String reportingPeriod;

    @NotNull(message = "Actual value is required")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Actual value cannot be negative"
    )
    private BigDecimal actualValue;
}