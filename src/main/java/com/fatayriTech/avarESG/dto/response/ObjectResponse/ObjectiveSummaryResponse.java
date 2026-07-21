package com.fatayriTech.avarESG.dto.response.ObjectResponse;


import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveSummaryResponse {

    private Long id;

    private String objectiveTitle;

    private EsgPillar esgPillar;

    private Long relatedKpiId;

    private String relatedKpiName;

    private String relatedKpiUnit;

    private BigDecimal baselineValue;

    private BigDecimal targetValue;

    private BigDecimal currentValue;

    private LocalDate targetDate;

    private ObjectiveProgressStatus progressStatus;

    private BigDecimal progressPercentage;
}