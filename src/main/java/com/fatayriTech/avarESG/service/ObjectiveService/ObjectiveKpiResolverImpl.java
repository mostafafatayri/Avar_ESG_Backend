package com.fatayriTech.avarESG.service.ObjectiveService;

import com.fatayriTech.avarESG.model.Kpi;
import com.fatayriTech.avarESG.model.KpiReading;
import com.fatayriTech.avarESG.repository.KpiReadingRepository;
import com.fatayriTech.avarESG.repository.KpiRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObjectiveKpiResolverImpl
        implements ObjectiveKpiResolver {

    private final KpiRepository kpiRepository;
    private final KpiReadingRepository kpiReadingRepository;

    @Override
    public boolean existsById(Long kpiId) {
        return kpiId != null
                && kpiRepository.existsById(kpiId);
    }

    @Override
    public String getKpiName(Long kpiId) {
        return getKpi(kpiId).getName();
    }

    @Override
    public String getKpiUnit(Long kpiId) {
        return getKpi(kpiId).getUnitOfMeasure();
    }

    @Override
    public BigDecimal getLatestValue(Long kpiId) {
        KpiReading latestReading =
                kpiReadingRepository
                        .findLatestApprovedReading(kpiId)
                        .orElse(null);

        if (latestReading == null) {
            return null;
        }

        return latestReading.getActualValue();
    }

    private Kpi getKpi(Long kpiId) {
        return kpiRepository
                .findById(kpiId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "KPI not found with ID: " + kpiId
                        )
                );
    }
}