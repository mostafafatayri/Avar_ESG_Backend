package com.fatayriTech.avarESG.dto.response.KpiResponse;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class KpiReadingResponse {

    private Long id;
    private Long kpiId;
    private BigDecimal value;
    private LocalDate readingDate;
    private String dataReference;
    private String remarks;

    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}