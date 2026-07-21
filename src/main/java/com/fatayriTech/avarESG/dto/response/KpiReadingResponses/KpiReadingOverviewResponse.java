package com.fatayriTech.avarESG.dto.response.KpiReadingResponses;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiReadingOverviewResponse {

    private Long kpiId;

    private String kpiCode;

    private String kpiName;

    private String reportingFrequency;

    private String unitOfMeasure;

    private boolean approvalRequired;

    private int totalReadings;

    private int approvedReadings;

    private int pendingReadings;

    private int rejectedReadings;

    private List<MissingKpiPeriodResponse> missingPeriods;

    private List<KpiReadingResponse> readings;
}