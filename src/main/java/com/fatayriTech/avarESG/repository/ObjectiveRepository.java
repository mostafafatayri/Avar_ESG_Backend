package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import com.fatayriTech.avarESG.model.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectiveRepository
        extends JpaRepository<Objective, Long>,
        JpaSpecificationExecutor<Objective> {

    Optional<Objective> findByIdAndDeletedFalse(Long id);

    List<Objective> findAllByDeletedFalseOrderByCreatedAtDesc();

    boolean existsByObjectiveTitleIgnoreCaseAndDeletedFalse(
            String objectiveTitle
    );

    boolean existsByObjectiveTitleIgnoreCaseAndIdNotAndDeletedFalse(
            String objectiveTitle,
            Long id
    );

    long countByDeletedFalse();

    long countByProgressStatusAndDeletedFalse(
            ObjectiveProgressStatus progressStatus
    );

    long countByApprovalStatusAndDeletedFalse(
            ObjectiveApprovalStatus approvalStatus
    );

    long countByEsgPillarAndDeletedFalse(
            EsgPillar esgPillar
    );
}