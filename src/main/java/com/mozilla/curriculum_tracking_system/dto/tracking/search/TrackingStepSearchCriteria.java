package com.mozilla.curriculum_tracking_system.dto.tracking.search;


import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering tracking steps
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingStepSearchCriteria {
    private Long trackingId;
    private TrackingStage stage;
    private TrackingAction action;
    private Long performedByUserId;
    private Long assignedToUserId;
    private LocalDateTime performedAfter;
    private LocalDateTime performedBefore;
    private Boolean isMilestone;
    private Boolean isStageTransition;
}