package com.mozilla.curriculum_tracking_system.util.specifications;

import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingStepSearchCriteria;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class TrackingStepSpecification {

    private TrackingStepSpecification() {
    }

    /**
     * Build a dynamic specification based on search criteria
     *
     * @param criteria The search criteria containing filter parameters
     * @return Specification for TrackingStep entities
     */
    public static Specification<TrackingStep> withCriteria(TrackingStepSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by tracking ID
            if (criteria.getTrackingId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("tracking").get("id"),
                        criteria.getTrackingId()
                ));
            }

            // Filter by stage
            if (criteria.getStage() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("stage"),
                        criteria.getStage()
                ));
            }

            // Filter by action
            if (criteria.getAction() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("action"),
                        criteria.getAction()
                ));
            }

            // Filter by user who performed the action
            if (criteria.getPerformedByUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("performedBy").get("id"),
                        criteria.getPerformedByUserId()
                ));
            }

            // Filter by user assigned to the step
            if (criteria.getAssignedToUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("assignedTo").get("id"),
                        criteria.getAssignedToUserId()
                ));
            }

            // Filter by performed date - after
            if (criteria.getPerformedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("performedAt"),
                        criteria.getPerformedAfter()
                ));
            }

            // Filter by performed date - before
            if (criteria.getPerformedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("performedAt"),
                        criteria.getPerformedBefore()
                ));
            }

            // Filter by milestone status
            if (criteria.getIsMilestone() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("isMilestone"),
                        criteria.getIsMilestone()
                ));
            }

            // Filter by stage transition status
            if (criteria.getIsStageTransition() != null) {
                if (criteria.getIsStageTransition()) {
                    // Is a stage transition: fromStage and toStage are not null and different
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.isNotNull(root.get("fromStage")),
                            criteriaBuilder.isNotNull(root.get("toStage")),
                            criteriaBuilder.notEqual(root.get("fromStage"), root.get("toStage"))
                    ));
                } else {
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.isNull(root.get("fromStage")),
                            criteriaBuilder.isNull(root.get("toStage")),
                            criteriaBuilder.equal(root.get("fromStage"), root.get("toStage"))
                    ));
                }
            }

            root.fetch("performedBy");
            root.fetch("assignedTo", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("tracking");

            // Ensure distinct results when using fetch joins
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find steps by tracking ID
     *
     * @param trackingId The tracking ID to filter by
     * @return Specification for steps belonging to the specified tracking
     */
    public static Specification<TrackingStep> byTrackingId(Long trackingId) {
        return (root, query, criteriaBuilder) -> {
            if (trackingId == null) {
                return criteriaBuilder.conjunction(); // Always true predicate
            }
            return criteriaBuilder.equal(root.get("tracking").get("id"), trackingId);
        };
    }

    /**
     * Specification to find steps by stage
     *
     * @param stage The tracking stage to filter by
     * @return Specification for steps at the specified stage
     */
    public static Specification<TrackingStep> byStage(com.mozilla.curriculum_tracking_system.enums.TrackingStage stage) {
        return (root, query, criteriaBuilder) -> {
            if (stage == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("stage"), stage);
        };
    }

    /**
     * Specification to find steps by action
     *
     * @param action The tracking action to filter by
     * @return Specification for steps with the specified action
     */
    public static Specification<TrackingStep> byAction(com.mozilla.curriculum_tracking_system.enums.TrackingAction action) {
        return (root, query, criteriaBuilder) -> {
            if (action == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("action"), action);
        };
    }

    /**
     * Specification to find steps performed by a specific user
     *
     * @param userId The ID of the user who performed the steps
     * @return Specification for steps performed by the specified user
     */
    public static Specification<TrackingStep> byPerformedBy(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("performedBy").get("id"), userId);
        };
    }

    /**
     * Specification to find steps assigned to a specific user
     *
     * @param userId The ID of the user assigned to the steps
     * @return Specification for steps assigned to the specified user
     */
    public static Specification<TrackingStep> byAssignedTo(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("assignedTo").get("id"), userId);
        };
    }

    /**
     * Specification to find milestone steps
     *
     * @return Specification for steps marked as milestones
     */
    public static Specification<TrackingStep> isMilestone() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isMilestone"));
    }

    /**
     * Specification to find stage transition steps
     *
     * @return Specification for steps that represent stage transitions
     */
    public static Specification<TrackingStep> isStageTransition() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("fromStage")),
                        criteriaBuilder.isNotNull(root.get("toStage")),
                        criteriaBuilder.notEqual(root.get("fromStage"), root.get("toStage"))
                );
    }

    /**
     * Specification to find steps within a date range
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Specification for steps performed within the date range
     */
    public static Specification<TrackingStep> byDateRange(java.time.LocalDateTime startDate,
                                                          java.time.LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("performedAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("performedAt"), endDate));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find steps performed after a specific date
     *
     * @param date The date after which steps were performed
     * @return Specification for steps performed after the specified date
     */
    public static Specification<TrackingStep> performedAfter(java.time.LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("performedAt"), date);
        };
    }

    /**
     * Specification to find steps performed before a specific date
     *
     * @param date The date before which steps were performed
     * @return Specification for steps performed before the specified date
     */
    public static Specification<TrackingStep> performedBefore(java.time.LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("performedAt"), date);
        };
    }

    /**
     * Specification that includes necessary fetch joins to avoid N+1 queries
     *
     * @return Specification with optimized fetch joins
     */
    public static Specification<TrackingStep> withFetchJoins() {
        return (root, query, criteriaBuilder) -> {
            // Fetch related entities to avoid N+1 queries
            root.fetch("performedBy");
            root.fetch("assignedTo", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("tracking");

            // Ensure distinct results when using fetch joins
            query.distinct(true);

            return criteriaBuilder.conjunction(); // Always true
        };
    }

    /**
     * Combines multiple specifications with AND logic
     *
     * @param specifications Variable number of specifications to combine
     * @return Combined specification using AND logic
     */
    @SafeVarargs
    public static Specification<TrackingStep> combineWithAnd(Specification<TrackingStep>... specifications) {
        Specification<TrackingStep> result = Specification.where(null);

        for (Specification<TrackingStep> spec : specifications) {
            if (spec != null) {
                result = result.and(spec);
            }
        }

        return result;
    }

    /**
     * Combines multiple specifications with OR logic
     *
     * @param specifications Variable number of specifications to combine
     * @return Combined specification using OR logic
     */
    @SafeVarargs
    public static Specification<TrackingStep> combineWithOr(Specification<TrackingStep>... specifications) {
        Specification<TrackingStep> result = null;

        for (Specification<TrackingStep> spec : specifications) {
            if (spec != null) {
                result = result == null ? Specification.where(spec) : result.or(spec);
            }
        }

        return result != null ? result : Specification.where(null);
    }
}