package com.fatayriTech.avarESG.repository.specification;

import com.fatayriTech.avarESG.enums.CarbonApprovalStatus;
import com.fatayriTech.avarESG.enums.CarbonEmissionSource;
import com.fatayriTech.avarESG.enums.CarbonScope;
import com.fatayriTech.avarESG.model.CarbonEmission;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class CarbonEmissionSpecification {

    private CarbonEmissionSpecification() {
    }

    public static Specification<CarbonEmission>
    hasScope(CarbonScope scope) {
        return (root, query, builder) ->
                scope == null
                        ? builder.conjunction()
                        : builder.equal(
                        root.get("scope"),
                        scope
                );
    }

    public static Specification<CarbonEmission>
    hasSource(
            CarbonEmissionSource source
    ) {
        return (root, query, builder) ->
                source == null
                        ? builder.conjunction()
                        : builder.equal(
                        root.get("emissionSource"),
                        source
                );
    }

    public static Specification<CarbonEmission>
    hasFacility(Long facilityId) {
        return (root, query, builder) ->
                facilityId == null
                        ? builder.conjunction()
                        : builder.equal(
                        root.get("facilityId"),
                        facilityId
                );
    }

    public static Specification<CarbonEmission>
    hasStatus(
            CarbonApprovalStatus status
    ) {
        return (root, query, builder) ->
                status == null
                        ? builder.conjunction()
                        : builder.equal(
                        root.get("approvalStatus"),
                        status
                );
    }

    public static Specification<CarbonEmission>
    hasReportingPeriod(
            LocalDate reportingPeriod
    ) {
        return (root, query, builder) ->
                reportingPeriod == null
                        ? builder.conjunction()
                        : builder.equal(
                        root.get("reportingPeriod"),
                        reportingPeriod
                );
    }

    public static Specification<CarbonEmission>
    containsSearch(String search) {
        return (root, query, builder) -> {
            if (search == null ||
                    search.isBlank()) {
                return builder.conjunction();
            }

            String pattern =
                    "%" +
                            search.trim()
                                    .toLowerCase() +
                            "%";

            return builder.or(
                    builder.like(
                            builder.lower(
                                    root.get("dataSource")
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get("remarks")
                            ),
                            pattern
                    )
            );
        };
    }
}