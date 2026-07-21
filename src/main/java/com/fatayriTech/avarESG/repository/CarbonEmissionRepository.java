package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.CarbonApprovalStatus;
import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonEmissionUnit;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.model.CarbonEmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarbonEmissionRepository
        extends JpaRepository<CarbonEmission, Long>,
        JpaSpecificationExecutor<CarbonEmission> {

    Optional<CarbonEmission>
    findFirstByFacilityIdAndEmissionSourceAndReportingPeriodOrderByVersionNumberDesc(
            Long facilityId,
            CarbonEmissionSource emissionSource,
            LocalDate reportingPeriod
    );

    List<CarbonEmission>
    findByFacilityIdAndEmissionSourceAndReportingPeriodOrderByVersionNumberDesc(
            Long facilityId,
            CarbonEmissionSource emissionSource,
            LocalDate reportingPeriod
    );

    Optional<CarbonEmission>
    findFirstByFacilityIdAndEmissionSourceAndReportingPeriodAndActiveVersionTrueOrderByVersionNumberDesc(
            Long facilityId,
            CarbonEmissionSource emissionSource,
            LocalDate reportingPeriod
    );

    List<CarbonEmission>
    findByVersionGroupIdOrderByVersionNumberDesc(
            String versionGroupId
    );

    Optional<CarbonEmission>
    findFirstByVersionGroupIdAndActiveVersionTrueOrderByVersionNumberDesc(
            String versionGroupId
    );

    Optional<CarbonEmission>
    findFirstByVersionGroupIdOrderByVersionNumberDesc(
            String versionGroupId
    );

    boolean existsByVersionGroupIdAndCorrectionTrueAndApprovalStatusIn(
            String versionGroupId,
            Collection<CarbonApprovalStatus> statuses
    );

    @Query("""
            SELECT COALESCE(
                MAX(emission.versionNumber),
                0
            )
            FROM CarbonEmission emission
            WHERE emission.versionGroupId = :versionGroupId
            """)
    Integer findMaximumVersionNumber(
            @Param("versionGroupId")
            String versionGroupId
    );

    boolean existsByFacilityIdAndEmissionSourceAndReportingPeriodAndVersionGroupIdNot(
            Long facilityId,
            CarbonEmissionSource emissionSource,
            LocalDate reportingPeriod,
            String versionGroupId
    );

    @Query("""
            SELECT COALESCE(
                SUM(emission.emissions),
                0
            )
            FROM CarbonEmission emission
            WHERE emission.scope = :scope
              AND emission.facilityId = :facilityId
              AND emission.reportingPeriod = :reportingPeriod
              AND emission.emissionsUnit = :emissionsUnit
              AND emission.memo = false
              AND emission.activeVersion = true
              AND emission.approvalStatus =
                  com.fatayriTech.avarESG.enums.CarbonApprovalStatus.APPROVED
            """)
    BigDecimal sumApprovedNonMemoEmissions(
            @Param("scope")
            CarbonScope scope,

            @Param("facilityId")
            Long facilityId,

            @Param("reportingPeriod")
            LocalDate reportingPeriod,

            @Param("emissionsUnit")
            CarbonEmissionUnit emissionsUnit
    );
}