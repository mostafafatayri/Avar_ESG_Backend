package com.fatayriTech.avarESG.dto.request.CarbonRequests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignCarbonReviewerRequest {

    @NotNull(message = "Reviewer ID is required")
    private Long reviewerId;
}