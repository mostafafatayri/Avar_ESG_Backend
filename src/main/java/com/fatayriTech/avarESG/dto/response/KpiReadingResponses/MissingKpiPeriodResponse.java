package com.fatayriTech.avarESG.dto.response.KpiReadingResponses;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingKpiPeriodResponse {

    private String period;

    private String label;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean overdue;

    private boolean currentPeriod;
}