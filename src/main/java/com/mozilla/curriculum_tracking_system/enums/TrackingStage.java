package com.mozilla.curriculum_tracking_system.enums;

import lombok.Getter;

@Getter
public enum TrackingStage {
    IDEATION("Curriculum Ideation", "DEPARTMENT"),
    REVIEW_APPROVAL("Review & Tracking Approval", "QA,DEAN"),
    SCHOOL_BOARD("School Board Review", "QA,DEAN"),
    DEAN_COMMITTEE("Dean's Committee Review", "QA,DEAN"),
    SENATE("Senate Review", "QA"),
    QA_INTERNAL_AUDIT("QA Internal Audit", "QA"),
    CUE_EXTERNAL_AUDIT("CUE External Audit", "QA"),
    VICE_CHANCELLOR_APPROVAL("Vice Chancellor Approval", "QA"),
    ACCREDITED("Tracking Completed", "QA");

    private final String displayName;
    private final String allowedRoles;

    TrackingStage(String displayName, String allowedRoles) {
        this.displayName = displayName;
        this.allowedRoles = allowedRoles;
    }

    public TrackingStage getNextStage() {
        return switch (this) {
            case IDEATION -> REVIEW_APPROVAL;
            case REVIEW_APPROVAL -> SCHOOL_BOARD;
            case SCHOOL_BOARD -> DEAN_COMMITTEE;
            case DEAN_COMMITTEE -> SENATE;
            case SENATE -> QA_INTERNAL_AUDIT;
            case QA_INTERNAL_AUDIT -> CUE_EXTERNAL_AUDIT;
            case CUE_EXTERNAL_AUDIT -> VICE_CHANCELLOR_APPROVAL;
            case VICE_CHANCELLOR_APPROVAL, ACCREDITED -> ACCREDITED;
        };
    }

    /**
     * Get valid return stages for the current stage
     */
    public TrackingStage[] getValidReturnStages() {
        return switch (this) {
            case DEAN_COMMITTEE -> new TrackingStage[]{SCHOOL_BOARD};
            case SENATE -> new TrackingStage[]{DEAN_COMMITTEE};
            case QA_INTERNAL_AUDIT, CUE_EXTERNAL_AUDIT, VICE_CHANCELLOR_APPROVAL -> new TrackingStage[]{SCHOOL_BOARD};
            default -> new TrackingStage[]{};
        };
    }
}
