package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.ObjectiveRequest.CreateObjectiveRequest;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveResponse;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveStatisticsResponse;
import com.fatayriTech.avarESG.dto.response.ObjectResponse.ObjectiveSummaryResponse;
import com.fatayriTech.avarESG.dto.request.ObjectiveRequest.UpdateObjectiveRequest;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import com.fatayriTech.avarESG.service.ObjectiveService.ObjectiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix:/api/v1}/objectives")
@RequiredArgsConstructor
public class ObjectiveController {

    private final ObjectiveService objectiveService;

    @PostMapping
    public ResponseEntity<ObjectiveResponse> createObjective(
            @Valid
            @RequestBody
            CreateObjectiveRequest request,

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            )
            Long currentUserId
    ) {
        ObjectiveResponse response =
                objectiveService.createObjective(
                        request,
                        currentUserId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{objectiveId}")
    public ResponseEntity<ObjectiveResponse> updateObjective(
            @PathVariable
            Long objectiveId,

            @Valid
            @RequestBody
            UpdateObjectiveRequest request,

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            )
            Long currentUserId
    ) {
        ObjectiveResponse response =
                objectiveService.updateObjective(
                        objectiveId,
                        request,
                        currentUserId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{objectiveId}")
    public ResponseEntity<ObjectiveResponse> getObjectiveById(
            @PathVariable
            Long objectiveId
    ) {
        return ResponseEntity.ok(
                objectiveService.getObjectiveById(
                        objectiveId
                )
        );
    }

    @GetMapping
    public ResponseEntity<Page<ObjectiveSummaryResponse>> getObjectives(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            EsgPillar pillar,

            @RequestParam(required = false)
            ObjectiveProgressStatus progressStatus,

            @RequestParam(required = false)
            ObjectiveApprovalStatus approvalStatus,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                objectiveService.getObjectives(
                        search,
                        pillar,
                        progressStatus,
                        approvalStatus,
                        pageable
                )
        );
    }

    @GetMapping("/statistics")
    public ResponseEntity<ObjectiveStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(
                objectiveService.getStatistics()
        );
    }

    @DeleteMapping("/{objectiveId}")
    public ResponseEntity<Void> deleteObjective(
            @PathVariable
            Long objectiveId,

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            )
            Long currentUserId
    ) {
        objectiveService.deleteObjective(
                objectiveId,
                currentUserId
        );

        return ResponseEntity.noContent().build();
    }
}