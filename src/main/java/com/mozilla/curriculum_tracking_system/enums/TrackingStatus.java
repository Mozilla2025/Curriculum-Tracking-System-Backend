package com.mozilla.curriculum_tracking_system.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Status of curriculum tracking process")
public enum TrackingStatus {
    @Schema(description = "Tracking has been initiated")
    INITIATED("Initiated"),

    @Schema(description = "Tracking is currently in progress")
    IN_PROGRESS("In Progress"),

    @Schema(description = "Curriculum has been approved")
    APPROVED("Approved"),

    @Schema(description = "Curriculum has been rejected")
    REJECTED("Rejected"),

    @Schema(description = "Returned for revision and corrections")
    RETURNED_FOR_REVISION("Returned for Revision"),

    @Schema(description = "Tracking process completed successfully")
    COMPLETED("Completed");

    private final String displayName;

    TrackingStatus(String displayName) {
        this.displayName = displayName;
    }

}
