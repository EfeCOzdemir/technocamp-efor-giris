package com.technocamp.efor.performance;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PerformanceRepository extends JpaRepository<Performance, Long>, JpaSpecificationExecutor<Performance> {

    /** Boş bırakılan filtreler (null) uygulanmaz. */
    static Specification<Performance> filter(String agentId, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (agentId != null && !agentId.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("agentId"), agentId));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dateInfo"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dateInfo"), to));
            }
            return predicate;
        };
    }
}
