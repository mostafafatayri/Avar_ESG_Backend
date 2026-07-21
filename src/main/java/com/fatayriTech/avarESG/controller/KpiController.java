package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.KpiRequests.CreateKpiRequest;
import com.fatayriTech.avarESG.dto.request.KpiRequests.RejectKpiRequest;
import com.fatayriTech.avarESG.dto.request.KpiRequests.UpdateKpiRequest;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiResponse;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiSummaryResponse;
import com.fatayriTech.avarESG.enums.EsgCategory;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.KpiStatus;
import com.fatayriTech.avarESG.service.KpiService.KpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/kpis")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    @PostMapping
    @PreAuthorize("hasAuthority('KPI_CREATE')")
    public KpiResponse createKpi(
            @Valid
            @RequestBody
            CreateKpiRequest request
    ) {
        return kpiService.createKpi(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('KPI_VIEW')")
    public List<KpiResponse> getAllKpis(
            @RequestParam(required = false)
            EsgPillar pillar,

            @RequestParam(required = false)
            EsgCategory category,

            @RequestParam(required = false)
            KpiStatus status
    ) {
        return kpiService.getAllKpis(
                pillar,
                category,
                status
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('KPI_VIEW')")
    public KpiSummaryResponse getKpiSummary() {
        return kpiService.getKpiSummary();
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('KPI_VIEW')")
    public List<EsgCategory> getCategoriesByPillar(
            @RequestParam
            EsgPillar pillar
    ) {
        return kpiService.getCategoriesByPillar(
                pillar
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('KPI_VIEW')")
    public KpiResponse getKpiById(
            @PathVariable
            Long id
    ) {
        return kpiService.getKpiById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('KPI_UPDATE')")
    public KpiResponse updateKpi(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateKpiRequest request
    ) {
        return kpiService.updateKpi(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('KPI_DELETE')")
    public void deleteKpi(
            @PathVariable
            Long id
    ) {
        kpiService.deleteKpi(id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('KPI_SUBMIT')")
    public KpiResponse submitForApproval(
            @PathVariable
            Long id
    ) {
        return kpiService.submitForApproval(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('KPI_APPROVE')")
    public KpiResponse approveKpi(
            @PathVariable
            Long id
    ) {
        return kpiService.approveKpi(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('KPI_REJECT')")
    public KpiResponse rejectKpi(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            RejectKpiRequest request
    ) {
        return kpiService.rejectKpi(
                id,
                request
        );
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('KPI_ARCHIVE')")
    public KpiResponse archiveKpi(
            @PathVariable
            Long id
    ) {
        return kpiService.archiveKpi(id);
    }
}