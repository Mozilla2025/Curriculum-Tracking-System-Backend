package com.mozilla.curriculum_tracking_system.enums;

public enum TrackingStatus {

    INITIATED("Initiated"),
    IN_PROGRESS("In Progress"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    RETURNED_FOR_REVISION("Returned for Revision"),
    COMPLETED("Completed");

    private final String displayName;

    TrackingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
