package com.mozilla.curriculum_tracking_system.util.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TrackingStagePermissionHelper {

    private static final Set<String> QA_ROLES = Set.of("QA", "SUPER_ADMIN");
    private static final Set<String> DEAN_ROLES = Set.of("DEAN", "QA", "SUPER_ADMIN");
    private static final Set<String> DEPARTMENT_ROLES = Set.of("HOD", "DEPARTMENT_HEAD", "QA", "SUPER_ADMIN");

    /**
     * Check if user has permission to perform actions at a specific stage
     */
    public boolean hasStagePermission(TrackingStage stage, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        return switch (stage) {
            case IDEATION -> userRoles.stream().anyMatch(DEPARTMENT_ROLES::contains);
            case REVIEW_APPROVAL, SCHOOL_BOARD, DEAN_COMMITTEE -> userRoles.stream().anyMatch(role ->
                    QA_ROLES.contains(role) || DEAN_ROLES.contains(role));
            case SENATE, QA_INTERNAL_AUDIT, CUE_EXTERNAL_AUDIT,
                 VICE_CHANCELLOR_APPROVAL, ACCREDITED -> userRoles.stream().anyMatch(QA_ROLES::contains);
        };
    }

    /**
     * Get the roles allowed to act at a specific stage
     */
    public Set<String> getAllowedRoles(TrackingStage stage) {
        return switch (stage) {
            case IDEATION -> DEPARTMENT_ROLES;
            case REVIEW_APPROVAL, SCHOOL_BOARD, DEAN_COMMITTEE -> DEAN_ROLES;
            case SENATE, QA_INTERNAL_AUDIT, CUE_EXTERNAL_AUDIT,
                 VICE_CHANCELLOR_APPROVAL, ACCREDITED -> QA_ROLES;
        };
    }

    /**
     * Check if stage transition is valid
     */
    public boolean isValidTransition(TrackingStage fromStage, TrackingStage toStage) {
        if (fromStage == null || toStage == null) {
            return false;
        }

        // Forward progression
        if (toStage == fromStage.getNextStage()) {
            return true;
        }

        // Valid return stages
        TrackingStage[] validReturnStages = fromStage.getValidReturnStages();
        for (TrackingStage validStage : validReturnStages) {
            if (toStage == validStage) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if user can return curriculum to a specific stage
     */
    public boolean canReturnToStage(TrackingStage currentStage, TrackingStage targetStage, List<String> userRoles) {
        if (!hasStagePermission(currentStage, userRoles)) {
            return false;
        }

        TrackingStage[] validReturnStages = currentStage.getValidReturnStages();
        for (TrackingStage validStage : validReturnStages) {
            if (targetStage == validStage) {
                return true;
            }
        }

        return false;
    }
}
