package com.fatayriTech.avarESG.service.CarbonService;

import com.fatayriTech.avarESG.dto.request.CarbonRequests.AssignCarbonReviewerRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.CreateCarbonCorrectionRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.CreateCarbonEmissionRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.RejectCarbonEmissionRequest;
import com.fatayriTech.avarESG.dto.request.CarbonRequests.UpdateCarbonEmissionRequest;
import com.fatayriTech.avarESG.dto.response.CarbonResponse.CarbonDashboardResponse;
import com.fatayriTech.avarESG.dto.response.CarbonResponse.CarbonEmissionResponse;
import com.fatayriTech.avarESG.dto.response.CarbonResponse.CarbonVersionHistoryResponse;
import com.fatayriTech.avarESG.enums.CarbonApprovalStatus;
import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonScope;

import java.util.List;

public interface CarbonEmissionService {

    CarbonEmissionResponse create(
            Long currentUserId,
            CreateCarbonEmissionRequest request
    );

    CarbonEmissionResponse createCorrection(
            Long activeEmissionId,
            Long currentUserId,
            CreateCarbonCorrectionRequest request
    );

    CarbonVersionHistoryResponse getHistory(
            Long emissionId,
            Long currentUserId
    );

    List<CarbonEmissionResponse> getAll(
            CarbonScope scope,
            CarbonEmissionSource source,
            Long facilityId,
            String reportingPeriod,
            CarbonApprovalStatus status,
            String search,
            Long currentUserId
    );

    CarbonEmissionResponse getById(
            Long id,
            Long currentUserId
    );

    CarbonEmissionResponse update(
            Long id,
            Long currentUserId,
            UpdateCarbonEmissionRequest request
    );

    void delete(
            Long id,
            Long currentUserId
    );

    CarbonEmissionResponse assignReviewer(
            Long id,
            Long currentUserId,
            AssignCarbonReviewerRequest request
    );

    CarbonEmissionResponse submit(
            Long id,
            Long currentUserId
    );

    CarbonEmissionResponse approve(
            Long id,
            Long currentUserId
    );

    CarbonEmissionResponse reject(
            Long id,
            Long currentUserId,
            RejectCarbonEmissionRequest request
    );

    CarbonDashboardResponse getDashboard();
}