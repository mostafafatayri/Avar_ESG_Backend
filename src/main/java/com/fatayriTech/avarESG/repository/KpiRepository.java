package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.EsgCategory;
import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.KpiStatus;
import com.fatayriTech.avarESG.model.Kpi;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiRepository
        extends JpaRepository<Kpi, Long> {

    boolean existsByCodeIgnoreCase(
            String code
    );

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            Long id
    );

    List<Kpi> findAllByOrderByCreationDateDesc();

    List<Kpi> findByPillarOrderByCreationDateDesc(
            EsgPillar pillar
    );

    List<Kpi> findByCategoryOrderByCreationDateDesc(
            EsgCategory category
    );

    List<Kpi> findByStatusOrderByCreationDateDesc(
            KpiStatus status
    );

    @EntityGraph(attributePaths = {
            "site",
            "responsibleOwner",
            "frameworks"
    })
    Optional<Kpi> findWithDetailsById(
            Long id
    );

    @EntityGraph(attributePaths = {
            "site",
            "responsibleOwner"
    })
    List<Kpi> findBySiteIdAndStatus(
            Long siteId,
            KpiStatus status
    );
}