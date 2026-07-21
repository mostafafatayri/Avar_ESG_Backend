package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.KpiReadingRequests.CreateKpiReadingRequest;
import com.fatayriTech.avarESG.dto.request.KpiReadingRequests.RejectKpiReadingRequest;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.KpiReadingOverviewResponse;
import com.fatayriTech.avarESG.dto.response.KpiReadingResponses.KpiReadingResponse;
import com.fatayriTech.avarESG.service.KpiReadingService.KpiReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "${api.prefix}/kpis/{kpiId}/readings"
)
@RequiredArgsConstructor
public class KpiReadingController {

    private final KpiReadingService
            kpiReadingService;

    @GetMapping
    @PreAuthorize(
            "hasAuthority('KPI_READING_VIEW')"
    )
    public List<KpiReadingResponse> getReadings(
            @PathVariable Long kpiId
    ) {
        return kpiReadingService.getReadings(
                kpiId
        );
    }

    @GetMapping("/overview")
    @PreAuthorize(
            "hasAuthority('KPI_READING_VIEW')"
    )
    public KpiReadingOverviewResponse
    getReadingOverview(
            @PathVariable Long kpiId
    ) {
        return kpiReadingService
                .getReadingOverview(kpiId);
    }

    @GetMapping("/{readingId}")
    @PreAuthorize(
            "hasAuthority('KPI_READING_VIEW')"
    )
    public KpiReadingResponse getReadingById(
            @PathVariable Long kpiId,
            @PathVariable Long readingId
    ) {
        return kpiReadingService.getReadingById(
                kpiId,
                readingId
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('KPI_READING_CREATE')"
    )
    public KpiReadingResponse createReading(
            @PathVariable Long kpiId,
            @Valid
            @RequestBody
            CreateKpiReadingRequest request
    ) {
        return kpiReadingService.createReading(
                kpiId,
                request
        );
    }

    @PostMapping("/{readingId}/approve")
    @PreAuthorize(
            "hasAuthority('KPI_READING_APPROVE')"
    )
    public KpiReadingResponse approveReading(
            @PathVariable Long kpiId,
            @PathVariable Long readingId
    ) {
        return kpiReadingService.approveReading(
                kpiId,
                readingId
        );
    }

    @PostMapping("/{readingId}/reject")
    @PreAuthorize("hasAuthority('KPI_READING_REJECT')")
    public KpiReadingResponse rejectReading(
            @PathVariable Long kpiId,
            @PathVariable Long readingId,
            @Valid
            @RequestBody
            RejectKpiReadingRequest request
    ) {
        return kpiReadingService.rejectReading(
                kpiId,
                readingId,
                request
        );
    }

    @DeleteMapping("/{readingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(
            "hasAuthority('KPI_READING_DELETE')"
    )
    public void deleteReading(
            @PathVariable Long kpiId,
            @PathVariable Long readingId
    ) {
        kpiReadingService.deleteReading(
                kpiId,
                readingId
        );
    }
}