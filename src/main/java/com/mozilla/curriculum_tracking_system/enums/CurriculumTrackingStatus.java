package com.mozilla.curriculum_tracking_system.enums;

public enum CurriculumTrackingStatus {
    REVIEW_DUE,
    UNDER_REVIEW,        // Initial status when curriculum is entered by QA
    ACCREDITED,         // Final status - curriculum is fully approved and accredited
    APPROVED_BY_CUE,   // Approved by CUE
    MINOR_REVAMP,     // Needs minor changes
    MAJOR_REVAMP     // Needs major changes

}
