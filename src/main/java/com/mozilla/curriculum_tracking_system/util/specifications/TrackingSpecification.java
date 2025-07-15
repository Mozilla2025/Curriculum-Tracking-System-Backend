package com.mozilla.curriculum_tracking_system.util.specifications;

import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingSearchCriteria;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrackingSpecification {

    public static Specification<CurriculumTracking> withCriteria(TrackingSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getCurrentStage() != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentStage"), criteria.getCurrentStage()));
            }

            if (criteria.getInitiatedByUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("initiatedBy").get("id"), criteria.getInitiatedByUserId()));
            }

            if (criteria.getCurrentAssigneeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentAssignee").get("id"), criteria.getCurrentAssigneeId()));
            }

            if (criteria.getSchoolId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("school").get("id"), criteria.getSchoolId()));
            }

            if (criteria.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("id"), criteria.getDepartmentId()));
            }

            if (criteria.getAcademicLevelId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("academicLevel").get("id"), criteria.getAcademicLevelId()));
            }

            if (criteria.getCurriculumId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("curriculum").get("id"), criteria.getCurriculumId()));
            }

            if (StringUtils.hasText(criteria.getSearchTerm())) {
                String searchPattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";

                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("proposedCurriculumName")),
                                searchPattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("proposedCurriculumCode")),
                                searchPattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("trackingId")),
                                searchPattern
                        ),
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("curriculum")),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("curriculum").get("name")),
                                        searchPattern
                                )
                        ),
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("curriculum")),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("curriculum").get("code")),
                                        searchPattern
                                )
                        )
                );
                predicates.add(searchPredicate);
            }

            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
            }

            if (criteria.getExpectedCompletionBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expectedCompletionDate"), criteria.getExpectedCompletionBefore()));
            }

            if (criteria.getIsIdeationStage() != null) {
                if (criteria.getIsIdeationStage()) {
                    predicates.add(criteriaBuilder.isNull(root.get("curriculum")));
                } else {
                    predicates.add(criteriaBuilder.isNotNull(root.get("curriculum")));
                }
            }

            if (criteria.getHasLinkedCurriculum() != null) {
                if (criteria.getHasLinkedCurriculum()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("curriculum")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("curriculum")));
                }
            }

            if (criteria.getIsOverdue() != null && criteria.getIsOverdue()) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.lessThan(root.get("expectedCompletionDate"), now),
                                criteriaBuilder.isNull(root.get("actualCompletionDate"))
                        )
                );
            }

            root.fetch("school");
            root.fetch("department");
            root.fetch("academicLevel");
            root.fetch("initiatedBy");
            root.fetch("currentAssignee");

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
