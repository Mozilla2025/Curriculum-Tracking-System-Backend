package com.mozilla.curriculum_tracking_system.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Actions that can be performed on curriculum tracking")
@Getter
public enum TrackingAction {
    @Schema(description = "Initial action to start tracking process")
    INITIATE("Initiate Tracking"),

    @Schema(description = "Approve current stage and move to next stage")
    APPROVE("Approve"),

    @Schema(description = "Reject the curriculum tracking")
    REJECT("Reject"),

    @Schema(description = "Return to previous stage for revision")
    RETURN("Return for Revision"),

    @Schema(description = "Submit for next level review")
    SUBMIT("Submit"),

    @Schema(description = "Review the current submission")
    REVIEW("Review"),

    @Schema(description = "Mark tracking as completed")
    COMPLETE("Complete");

    private final String displayName;

    TrackingAction(String displayName) {
        this.displayName = displayName;
    }
}
