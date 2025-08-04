package com.mozilla.curriculum_tracking_system.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Stages in the curriculum tracking workflow")
public enum TrackingStage {
    @Schema(description = "Initial curriculum ideation and proposal stage")
    IDEATION("Curriculum Ideation", "DEPARTMENT"),

    @Schema(description = "Review and approval by QA and Dean")
    REVIEW_APPROVAL("Review & Tracking Approval", "QA,DEAN"),

    @Schema(description = "School board review and approval")
    SCHOOL_BOARD("School Board Review", "QA,DEAN"),

    @Schema(description = "Dean's committee review")
    DEAN_COMMITTEE("Dean's Committee Review", "QA,DEAN"),

    @Schema(description = "University senate review")
    SENATE("Senate Review", "QA"),

    @Schema(description = "Internal audit by QA department")
    QA_INTERNAL_AUDIT("QA Internal Audit", "QA"),

    @Schema(description = "External audit by Commission for University Education")
    CUE_EXTERNAL_AUDIT("CUE External Audit", "QA"),

    @Schema(description = "Final approval by Vice Chancellor")
    VICE_CHANCELLOR_APPROVAL("Vice Chancellor Approval", "QA"),

    @Schema(description = "Curriculum fully accredited and active")
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
