package com.fatayriTech.avarESG.service.KpiReadingService;

import com.fatayriTech.avarESG.dto.request.KpiReadingRequests.CreateKpiReadingRequest;
import com.fatayriTech.avarESG.dto.request.KpiReadingRequests.RejectKpiReadingRequest;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.KpiReadingOverviewResponse;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.KpiReadingResponse;

import java.util.List;

public interface KpiReadingService {

    List<KpiReadingResponse> getReadings(
            Long kpiId
    );

    KpiReadingOverviewResponse getReadingOverview(
            Long kpiId
    );

    KpiReadingResponse getReadingById(
            Long kpiId,
            Long readingId
    );

    KpiReadingResponse createReading(
            Long kpiId,
            CreateKpiReadingRequest request
    );

    KpiReadingResponse approveReading(
            Long kpiId,
            Long readingId
    );

    KpiReadingResponse rejectReading(
            Long kpiId,
            Long readingId,
            RejectKpiReadingRequest request
    );

    void deleteReading(
            Long kpiId,
            Long readingId
    );
}