package com.fatayriTech.avarESG.controller;

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
import com.fatayriTech.avarESG.exceptions.BadRequestException;
import com.fatayriTech.avarESG.service.CarbonService.CarbonEmissionService;
import com.fatayriTech.avarESG.service.SecurityService.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "${api.prefix}/carbon-emissions"
)
@RequiredArgsConstructor
public class CarbonEmissionController {

    private final CarbonEmissionService
            carbonEmissionService;

    @PostMapping
    @PreAuthorize(
            "hasAuthority('CARBON_CREATE')"
    )
    public CarbonEmissionResponse create(
            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            CreateCarbonEmissionRequest request
    ) {
        return carbonEmissionService.create(
                requireUserId(currentUser),
                request
        );
    }

    @PostMapping("/{id}/corrections")
    @PreAuthorize(
            "hasAuthority('CARBON_UPDATE')"
    )
    public CarbonEmissionResponse createCorrection(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            CreateCarbonCorrectionRequest request
    ) {
        return carbonEmissionService
                .createCorrection(
                        id,
                        requireUserId(currentUser),
                        request
                );
    }

    @GetMapping("/{id}/history")
    @PreAuthorize(
            "hasAuthority('CARBON_VIEW')"
    )
    public CarbonVersionHistoryResponse getHistory(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        return carbonEmissionService.getHistory(
                id,
                requireUserId(currentUser)
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAuthority('CARBON_VIEW')"
    )
    public List<CarbonEmissionResponse> getAll(
            @RequestParam(required = false)
            CarbonScope scope,

            @RequestParam(required = false)
            CarbonEmissionSource source,

            @RequestParam(required = false)
            Long facilityId,

            @RequestParam(required = false)
            String reportingPeriod,

            @RequestParam(required = false)
            CarbonApprovalStatus status,

            @RequestParam(required = false)
            String search,

            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        return carbonEmissionService.getAll(
                scope,
                source,
                facilityId,
                reportingPeriod,
                status,
                search,
                requireUserId(currentUser)
        );
    }

    @GetMapping("/dashboard")
    @PreAuthorize(
            "hasAuthority('CARBON_VIEW')"
    )
    public CarbonDashboardResponse getDashboard() {
        return carbonEmissionService
                .getDashboard();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('CARBON_VIEW')"
    )
    public CarbonEmissionResponse getById(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        return carbonEmissionService.getById(
                id,
                requireUserId(currentUser)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('CARBON_UPDATE')"
    )
    public CarbonEmissionResponse update(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            UpdateCarbonEmissionRequest request
    ) {
        return carbonEmissionService.update(
                id,
                requireUserId(currentUser),
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('CARBON_DELETE')"
    )
    public void delete(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        carbonEmissionService.delete(
                id,
                requireUserId(currentUser)
        );
    }

    @PutMapping("/{id}/reviewer")
    @PreAuthorize(
            "hasAuthority('CARBON_UPDATE')"
    )
    public CarbonEmissionResponse assignReviewer(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            AssignCarbonReviewerRequest request
    ) {
        return carbonEmissionService
                .assignReviewer(
                        id,
                        requireUserId(currentUser),
                        request
                );
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize(
            "hasRole('SUPER_ADMIN') " +
                    "or hasAuthority('CARBON_APPROVE')"
    )
    public CarbonEmissionResponse submit(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        return carbonEmissionService.submit(
                id,
                requireUserId(currentUser)
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize(
            "hasRole('SUPER_ADMIN') " +
                    "or hasAuthority('CARBON_APPROVE')"
    )
    public CarbonEmissionResponse approve(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser
    ) {
        return carbonEmissionService.approve(
                id,
                requireUserId(currentUser)
        );
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize(
            "hasRole('SUPER_ADMIN') " +
                    "or hasAuthority('CARBON_REJECT')"
    )
    public CarbonEmissionResponse reject(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CurrentUser currentUser,

            @Valid
            @RequestBody
            RejectCarbonEmissionRequest request
    ) {
        return carbonEmissionService.reject(
                id,
                requireUserId(currentUser),
                request
        );
    }

    private Long requireUserId(
            CurrentUser currentUser
    ) {
        if (currentUser == null
                || currentUser.userId() == null) {

            throw new BadRequestException(
                    "Authenticated user could not be resolved"
            );
        }

        return currentUser.userId();
    }
}