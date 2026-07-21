package com.fatayriTech.avarESG.service.ObjectiveService;



import com.fatayriTech.avarESG.dto.request.ObjectiveRequest.CreateObjectiveRequest;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveResponse;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveStatisticsResponse;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveSummaryResponse;
import com.fatayriTech.avarESG.dto.request.ObjectiveRequest.UpdateObjectiveRequest;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ObjectiveService {

    ObjectiveResponse createObjective(
            CreateObjectiveRequest request,
            Long currentUserId
    );

    ObjectiveResponse updateObjective(
            Long objectiveId,
            UpdateObjectiveRequest request,
            Long currentUserId
    );

    ObjectiveResponse getObjectiveById(
            Long objectiveId
    );

    Page<ObjectiveSummaryResponse> getObjectives(
            String search,
            EsgPillar pillar,
            ObjectiveProgressStatus progressStatus,
            ObjectiveApprovalStatus approvalStatus,
            Pageable pageable
    );

    ObjectiveStatisticsResponse getStatistics();

    void deleteObjective(
            Long objectiveId,
            Long currentUserId
    );
}