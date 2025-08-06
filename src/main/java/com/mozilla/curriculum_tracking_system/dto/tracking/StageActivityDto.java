package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageActivityDto {
    private CurriculumTrackingStage stage;
    private String stageDisplayName;
    private Long actionCount;

    // Constructor for JPQL query projection
    public StageActivityDto(CurriculumTrackingStage stage, Long actionCount) {
        this.stage = stage;
        this.actionCount = actionCount;
        this.stageDisplayName = stage.getDisplayName(); // Assuming your enum has this method
    }
}