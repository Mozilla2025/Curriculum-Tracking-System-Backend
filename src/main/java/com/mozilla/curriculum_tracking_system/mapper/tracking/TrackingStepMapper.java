package com.mozilla.curriculum_tracking_system.mapper.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingStepDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingStepPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingActionRequest;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mozilla.curriculum_tracking_system.enums.TrackingStage.*;

/**
 * Mapper for TrackingStep entities and DTOs
 * Handles conversions between tracking step entities and their corresponding DTOs
 */
@Component
public class TrackingStepMapper {

    /**
     * Convert TrackingStep entity to DTO
     */
    public TrackingStepDto toDto(TrackingStep step) {
        if (step == null) {
            return null;
        }

        return TrackingStepDto.builder()
                .id(step.getId())
                .stage(step.getStage())
                .stageDisplayName(step.getStage().getDisplayName())
                .action(step.getAction())
                .actionDisplayName(step.getAction().getDisplayName())
                .performedByName(getFullName(step.getPerformedBy()))
                .performedByEmail(step.getPerformedBy().getEmail())
                .assignedToName(step.getAssignedTo() != null ? getFullName(step.getAssignedTo()) : null)
                .assignedToEmail(step.getAssignedTo() != null ? step.getAssignedTo().getEmail() : null)
                .fromStage(step.getFromStage())
                .toStage(step.getToStage())
                .notes(step.getNotes())
                .performedAt(step.getPerformedAt())
                .dueDate(step.getDueDate())
                .isMilestone(step.getIsMilestone())
                .isStageTransition(step.isStageTransition())
                .isForwardMovement(step.isForwardMovement())
                .isBackwardMovement(step.isStageTransition() && !step.isForwardMovement())
                .build();
    }

    /**
     * Convert TrackingActionRequest to TrackingStep entity
     */
    public TrackingStep toEntity(TrackingActionRequest request,
                                 CurriculumTracking tracking,
                                 User performedBy,
                                 User assignedTo) {
        if (request == null || tracking == null || performedBy == null) {
            return null;
        }

        return TrackingStep.builder()
                .tracking(tracking)
                .stage(tracking.getCurrentStage())
                .action(request.getAction())
                .performedBy(performedBy)
                .assignedTo(assignedTo)
                .fromStage(tracking.getCurrentStage())
                .toStage(determineToStage(request, tracking))
                .notes(request.getNotes())
                .dueDate(request.getDueDate())
                .isMilestone(request.getIsMilestone() != null ? request.getIsMilestone() : false)
                .build();
    }

    /**
     * Convert Page of TrackingStep entities to paginated response
     */
    public TrackingStepPageResponse toPageResponse(Page<TrackingStep> stepPage) {
        if (stepPage == null) {
            return null;
        }

        List<TrackingStepDto> steps = stepPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return TrackingStepPageResponse.builder()
                .steps(steps)
                .currentPage(stepPage.getNumber())
                .totalPages(stepPage.getTotalPages())
                .totalElements(stepPage.getTotalElements())
                .pageSize(stepPage.getSize())
                .hasNext(stepPage.hasNext())
                .hasPrevious(stepPage.hasPrevious())
                .build();
    }

    /**
     * Convert list of TrackingStep entities to DTOs
     */
    public List<TrackingStepDto> toDtoList(List<TrackingStep> steps) {
        if (steps == null) {
            return null;
        }

        return steps.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to get full name from User entity
     */
    private String getFullName(User user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";

        return (firstName + " " + lastName).trim();
    }

    /**
     * Determine the target stage based on the action and current state
     */
    private com.mozilla.curriculum_tracking_system.enums.TrackingStage determineToStage(
            TrackingActionRequest request,
            CurriculumTracking tracking) {

        return switch (request.getAction()) {
            case APPROVE -> tracking.getCurrentStage().getNextStage();
            case RETURN -> request.getReturnToStage();
            case REJECT ->
                    determineRejectionStage(tracking.getCurrentStage());
            default -> tracking.getCurrentStage();
        };
    }

    /**
     * Determine which stage to return to when rejecting
     */
    private com.mozilla.curriculum_tracking_system.enums.TrackingStage determineRejectionStage(
            com.mozilla.curriculum_tracking_system.enums.TrackingStage currentStage) {

        switch (currentStage) {
            case REVIEW_APPROVAL:
                return IDEATION;
            case SCHOOL_BOARD:
                return REVIEW_APPROVAL;
            case DEAN_COMMITTEE:
                return SCHOOL_BOARD;
            case SENATE:
                return DEAN_COMMITTEE;
            case QA_INTERNAL_AUDIT:
                return SENATE;
            case CUE_EXTERNAL_AUDIT:
                return QA_INTERNAL_AUDIT;
            case VICE_CHANCELLOR_APPROVAL:
                return CUE_EXTERNAL_AUDIT;
            default:
                return currentStage;
        }
    }
}
