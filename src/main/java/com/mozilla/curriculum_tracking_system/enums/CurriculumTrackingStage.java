package com.mozilla.curriculum_tracking_system.enums;

import lombok.Getter;

@Getter
public enum CurriculumTrackingStage {

    SCHOOL_BOARD("School Board", "SCHOOL_BOARD"),
    DEAN_COMMITTEE("Dean Committee", "DEAN"),
    SENATE("Senate", "SENATE"),
    QA_INTERNAL_REVIEW("Quality Assurance - Internal Review", "QA"),
    VICE_CHANCELLOR_REVIEW("Vice Chancellor Review", "QA"), // QA handles VC interaction
    CUE_EXTERNAL_REVIEW("Commission of University Education - External Review", "QA"), // QA handles CUE interaction
    COMPLETED("Completed", "QA");

    private final String displayName;
    private final String requiredRole;

    CurriculumTrackingStage(String displayName, String requiredRole) {
        this.displayName = displayName;
        this.requiredRole = requiredRole;
    }

    /**
     * Get the next stage in the approval process
     */
    public CurriculumTrackingStage getNextStage() {
        return switch (this) {
            case SCHOOL_BOARD -> DEAN_COMMITTEE;
            case DEAN_COMMITTEE -> SENATE;
            case SENATE -> QA_INTERNAL_REVIEW;
            case QA_INTERNAL_REVIEW -> VICE_CHANCELLOR_REVIEW;
            case VICE_CHANCELLOR_REVIEW -> CUE_EXTERNAL_REVIEW;
            case CUE_EXTERNAL_REVIEW, COMPLETED -> COMPLETED;
        };
    }

    /**
     * Get the previous stage for sending back curriculum
     */
    public CurriculumTrackingStage getPreviousStage() {
        return switch (this) {
            case DEAN_COMMITTEE -> SCHOOL_BOARD;
            case SENATE -> DEAN_COMMITTEE;
            case QA_INTERNAL_REVIEW -> SENATE;
            case VICE_CHANCELLOR_REVIEW -> QA_INTERNAL_REVIEW;
            case CUE_EXTERNAL_REVIEW -> VICE_CHANCELLOR_REVIEW;
            case COMPLETED -> CUE_EXTERNAL_REVIEW;
            case SCHOOL_BOARD -> SCHOOL_BOARD; // Cannot go back further
        };
    }

    /**
     * Check if this stage can send curriculum back to a specific stage
     */
    public boolean canSendBackTo(CurriculumTrackingStage targetStage) {
        return switch (this) {
            case DEAN_COMMITTEE -> targetStage == SCHOOL_BOARD;
            case SENATE, QA_INTERNAL_REVIEW -> targetStage == DEAN_COMMITTEE;
            case VICE_CHANCELLOR_REVIEW, CUE_EXTERNAL_REVIEW ->
                    targetStage == DEAN_COMMITTEE || targetStage == QA_INTERNAL_REVIEW;
            default -> false;
        };
    }
}
