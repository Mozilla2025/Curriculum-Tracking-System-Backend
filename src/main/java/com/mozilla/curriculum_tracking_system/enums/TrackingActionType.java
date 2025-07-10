package com.mozilla.curriculum_tracking_system.enums;

public enum TrackingActionType {
    SUBMITTED,              // Curriculum submitted to next stage
    APPROVED,               // Curriculum approved at current stage
    SENT_BACK,              // Curriculum sent back to previous stage
    REVIEWED,               // Curriculum reviewed but no action yet
    REJECTED,               // Curriculum rejected (terminal action)
    ACCREDITED,             // Final accreditation granted
    REVAMP_REQUESTED        // Revamp requested (minor/major)
}
