package com.fatayriTech.avarESG.dto.response.CarbonResponse;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CarbonDashboardResponse {

    private long totalRecords;

    private long draftRecords;
    private long pendingRecords;
    private long approvedRecords;
    private long rejectedRecords;

    private long memoRecords;

    /*
     * Totals are separated by their native unit.
     * No kg-to-tonne conversion is performed.
     */
    private BigDecimal totalKgCo2e;
    private BigDecimal totalTCo2e;

    private BigDecimal scope1KgCo2e;
    private BigDecimal scope2KgCo2e;
    private BigDecimal scope3KgCo2e;

    private BigDecimal scope1TCo2e;
    private BigDecimal scope2TCo2e;
    private BigDecimal scope3TCo2e;
}