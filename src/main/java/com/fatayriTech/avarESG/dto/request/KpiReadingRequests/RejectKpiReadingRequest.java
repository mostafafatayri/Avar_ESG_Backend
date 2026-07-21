package com.fatayriTech.avarESG.dto.request.KpiReadingRequests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectKpiReadingRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(
            max = 1000,
            message = "Rejection reason cannot exceed 1000 characters"
    )
    private String reason;
}