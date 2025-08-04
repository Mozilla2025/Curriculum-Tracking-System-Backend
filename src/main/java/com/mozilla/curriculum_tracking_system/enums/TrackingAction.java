package com.mozilla.curriculum_tracking_system.enums;

import lombok.Getter;

@Getter
public enum TrackingAction {
    INITIATE("Initiate Tracking"),
    APPROVE("Approve"),
    REJECT("Reject"),
    RETURN("Return for Revision"),
    SUBMIT("Submit"),
    REVIEW("Review"),
    COMPLETE("Complete");

    private final String displayName;

    TrackingAction(String displayName) {
        this.displayName = displayName;
    }

}
