package com.fatayriTech.avarESG.service.KpiService;

import com.fatayriTech.avarESG.dto.request.KpiRequests.*;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiReadingResponse;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiResponse;
import com.fatayriTech.avarESG.dto.response.KpiResponse.KpiSummaryResponse;
import com.fatayriTech.avarESG.enums.EsgCategory;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.KpiStatus;

import java.util.List;

public interface KpiService {

    KpiResponse createKpi(CreateKpiRequest request);

    List<KpiResponse> getAllKpis(
            EsgPillar pillar,
            EsgCategory category,
            KpiStatus status
    );

    KpiResponse getKpiById(Long id);

    KpiResponse updateKpi(Long id, UpdateKpiRequest request);

    void deleteKpi(Long id);

    KpiResponse submitForApproval(Long id);

    KpiResponse approveKpi(Long id);

    KpiResponse rejectKpi(Long id, RejectKpiRequest request);

    KpiResponse archiveKpi(Long id);




    KpiSummaryResponse getKpiSummary();

    List<EsgCategory> getCategoriesByPillar(EsgPillar pillar);
}