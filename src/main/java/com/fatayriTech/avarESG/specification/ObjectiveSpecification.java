package com.fatayriTech.avarESG.specification;

import com.fatayriTech.avarESG.enums.EsgPillar;
import com.fatayriTech.avarESG.enums.ObjectiveApprovalStatus;
import com.fatayriTech.avarESG.enums.ObjectiveProgressStatus;
import com.fatayriTech.avarESG.model.Objective;
import org.springframework.data.jpa.domain.Specification;

public final class ObjectiveSpecification {

    private ObjectiveSpecification() {
    }

    public static Specification<Objective> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(
                        root.get("deleted")
                );
    }

    public static Specification<Objective> search(
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            if (
                    search == null ||
                            search.trim().isEmpty()
            ) {
                return criteriaBuilder.conjunction();
            }

            String pattern =
                    "%" + search.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("objectiveTitle")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("objectiveDescription")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("esgCategory")
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Objective> hasPillar(
            EsgPillar pillar
    ) {
        return (root, query, criteriaBuilder) -> {
            if (pillar == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("esgPillar"),
                    pillar
            );
        };
    }

    public static Specification<Objective> hasProgressStatus(
            ObjectiveProgressStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("progressStatus"),
                    status
            );
        };
    }

    public static Specification<Objective> hasApprovalStatus(
            ObjectiveApprovalStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("approvalStatus"),
                    status
            );
        };
    }
}