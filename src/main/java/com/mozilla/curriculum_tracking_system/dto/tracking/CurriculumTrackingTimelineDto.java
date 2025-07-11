package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingTimelineDto {
    private Long trackingId;
    private String curriculumName;
    private List<TimelineEntry> timeline;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimelineEntry {
        private LocalDateTime date;
        private CurriculumTrackingStage stage;
        private String stageDisplayName;
        private TrackingActionType actionType;
        private String performedByEmail;
        private String comments;
        private boolean isMilestone;
        private boolean isCurrentStage;
        private int documentsCount;
    }
}
