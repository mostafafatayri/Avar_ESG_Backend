package com.fatayriTech.avarESG.service.ObjectiveService;

import java.math.BigDecimal;

public interface ObjectiveKpiResolver {

    boolean existsById(Long kpiId);

    String getKpiName(Long kpiId);

    String getKpiUnit(Long kpiId);

    BigDecimal getLatestValue(Long kpiId);
}