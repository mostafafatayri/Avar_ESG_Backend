package com.fatayriTech.avarESG.dto.request.KpiRequests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateKpiReadingRequest {

    @NotNull(message = "Reading value is required")
    private BigDecimal value;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;

    @Size(max = 500)
    private String dataReference;

    @Size(max = 5000)
    private String remarks;
}