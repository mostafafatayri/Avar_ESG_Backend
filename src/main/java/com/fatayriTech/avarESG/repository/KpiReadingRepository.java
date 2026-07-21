package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus;
import com.fatayriTech.avarESG.model.KpiReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KpiReadingRepository
        extends JpaRepository<KpiReading, Long> {

    List<KpiReading> findByKpiIdOrderByPeriodStartDateDesc(
            Long kpiId
    );

    Optional<KpiReading> findByIdAndKpiId(
            Long id,
            Long kpiId
    );

    boolean existsByKpiIdAndReportingPeriodIgnoreCase(
            Long kpiId,
            String reportingPeriod
    );

    long countByKpiIdAndApprovalStatus(
            Long kpiId,
            KpiReadingApprovalStatus approvalStatus
    );

    @Query("""
            SELECT reading
            FROM KpiReading reading
            WHERE reading.kpi.id = :kpiId
              AND reading.approvalStatus =
                  com.fatayriTech.avarESG.enums.KpiReadingApprovalStatus.APPROVED
            ORDER BY reading.periodEndDate DESC,
                     reading.submissionDate DESC
            """)
    List<KpiReading> findApprovedReadingsNewestFirst(
            @Param("kpiId") Long kpiId
    );

    default Optional<KpiReading> findLatestApprovedReading(
            Long kpiId
    ) {
        return findApprovedReadingsNewestFirst(kpiId)
                .stream()
                .findFirst();
    }

    @Query("""
            SELECT reading.reportingPeriod
            FROM KpiReading reading
            WHERE reading.kpi.id = :kpiId
            """)
    List<String> findReportingPeriodsByKpiId(
            @Param("kpiId") Long kpiId
    );

    boolean existsByKpiId(Long kpiId);

    long countByKpiId(Long kpiId);

    @Modifying
    @Query("""
            DELETE FROM KpiReading reading
            WHERE reading.kpi.id = :kpiId
            """)
    void deleteByKpiId(
            @Param("kpiId") Long kpiId
    );

    List<KpiReading> findByKpiIdAndApprovalStatusNotOrderByPeriodStartDateAsc(
            Long kpiId,
            KpiReadingApprovalStatus approvalStatus
    );

    boolean existsByKpiIdAndReportingPeriodIgnoreCaseAndApprovalStatusNot(
            Long kpiId,
            String reportingPeriod,
            KpiReadingApprovalStatus approvalStatus
    );


    Optional<KpiReading>
    findByKpiIdAndReportingPeriodIgnoreCase(
            Long kpiId,
            String reportingPeriod
    );
}