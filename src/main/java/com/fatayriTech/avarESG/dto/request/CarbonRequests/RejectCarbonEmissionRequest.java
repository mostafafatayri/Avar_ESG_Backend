package com.fatayriTech.avarESG.dto.request.CarbonRequests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectCarbonEmissionRequest {

    @NotBlank(
            message = "Rejection reason is required"
    )
    @Size(
            max = 1000,
            message = "Rejection reason cannot exceed 1000 characters"
    )
    private String reason;
}