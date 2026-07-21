package com.fatayriTech.avarESG.dto.request.KpiRequests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectKpiRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 1000)
    private String reason;
}