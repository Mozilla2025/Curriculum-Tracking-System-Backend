package com.mozilla.curriculum_tracking_system.util;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingSearchRequest;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification utility for building dynamic queries for curriculum entity
 */
public class CurriculumTrackingSpecification {

    /**
     * Creates a specification based on search criteria
     *
     * @param criteria The search criteria
     * @return A specification for filtering curriculum trackings
     */

    public static Specification<CurriculumTracking> withCriteria(CurriculumTrackingSearchRequest criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            // Filter by status
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // Filter by current stage
            if (criteria.getCurrentStage() != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentStage"), criteria.getCurrentStage()));
            }

            // Filter by assigned user
            if (criteria.getAssignedToUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentAssignee"), criteria.getAssignedToUserId()));
            }

            // Filter by initiator
            if (criteria.getInitiatedByUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("initiatedBy"), criteria.getInitiatedByUserId()));
            }

            // Filter by curriculum ID
            if (criteria.getCurriculumId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("curriculum").get("id"), criteria.getCurriculumId()));
            }

            // Filter by school ID
            if (criteria.getSchoolId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("curriculum").get("school").get("id"), criteria.getSchoolId()));
            }

            // Filter by department ID
            if (criteria.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("curriculum").get("department").get("id"), criteria.getDepartmentId()));
            }

            // Filter by active status
            if (criteria.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), criteria.getIsActive()));
            }

            // Filter by initiation date range
            if (criteria.getInitiatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("initiatedAt"), criteria.getInitiatedAfter()));
            }

            if (criteria.getInitiatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("initiatedAt"), criteria.getInitiatedBefore()));
            }

            // Filter by due date
            if (criteria.getDueBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("estimatedCompletionDate"), criteria.getDueBefore()));
            }

            // Search in curriculum name, code, or notes
            if (StringUtils.hasText(criteria.getSearchTerm())) {
                String searchPattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";

                Predicate curriculumNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("curriculum").get("name")), searchPattern);

                Predicate curriculumCodePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("curriculum").get("code")), searchPattern);

                Predicate notesPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("notes")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        curriculumNamePredicate, curriculumCodePredicate, notesPredicate));
            }

            // Fetch related entities to avoid N+1 queries
            root.fetch("curriculum");
            root.fetch("curriculum").fetch("school");
            root.fetch("curriculum").fetch("department");

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

        };
    }

    /**
     * Creates a specification for finding overdue trackings
     *
     * @return A specification for overdue trackings
     */

    public static Specification<CurriculumTracking> isOverdue() {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Not completed
            predicates.add(criteriaBuilder.isNull(root.get("completedAt")));

            // Active
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            predicates.add(criteriaBuilder.lessThan(
                    root.get("estimatedCompletionDate"),
                    criteriaBuilder.currentTimestamp()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates a specification for finding trackings by multiple stages
     *
     * @param stages The stages to filter by
     * @return A specification for trackings in the specified stages
     */
    public static Specification<CurriculumTracking> hasStageIn(List<com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage> stages) {
        return (root, query, criteriaBuilder) ->
                root.get("currentStage").in(stages);
    }

    /**
     * Creates a specification for finding active trackings
     *
     * @return A specification for active trackings
     */
    public static Specification<CurriculumTracking> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isActive"));
    }

    /**
     * Creates a specification for finding completed trackings
     *
     * @return A specification for completed trackings
     */
    public static Specification<CurriculumTracking> isCompleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("completedAt"));
    }
}
