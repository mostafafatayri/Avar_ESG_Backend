package com.fatayriTech.avarESG.dto.response.KpiResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KpiSummaryResponse {

    private long totalKpis;
    private long activeKpis;
    private long draftKpis;
    private long pendingKpis;

    private long onTrackKpis;
    private long atRiskKpis;
    private long offTrackKpis;
    private long noDataKpis;

    private long onTimeKpis;
    private long dueKpis;
    private long overdueKpis;
}