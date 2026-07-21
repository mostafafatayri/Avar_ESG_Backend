package com.fatayriTech.avarESG.dto.response.CarbonResponse;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CarbonVersionHistoryResponse {

    private String versionGroupId;

    private Long activeEmissionId;

    private Integer activeVersionNumber;

    private int totalVersions;

    private List<CarbonEmissionResponse> versions;
}