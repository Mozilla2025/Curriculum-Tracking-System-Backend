package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingStageInfo {
    private CurriculumTrackingStage stage;
    private String displayName;
    private String requiredRole;
    private CurriculumTrackingStage nextStage;
    private CurriculumTrackingStage previousStage;
    private boolean canMoveForward;
    private boolean canSendBack;
    private List<CurriculumTrackingStage> possibleBackStages;
}
