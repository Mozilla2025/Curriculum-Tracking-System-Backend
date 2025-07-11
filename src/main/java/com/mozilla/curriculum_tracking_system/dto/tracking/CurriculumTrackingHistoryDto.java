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
public class CurriculumTrackingHistoryDto {
    private Long id;
    private Long curriculumTrackingId;
    private CurriculumTrackingStage stage;
    private String stageDisplayName;
    private TrackingActionType actionType;
    private Long performedBy;
    private String performedByEmail;
    private Long assignedTo;
    private String assignedToEmail;
    private CurriculumTrackingStage fromStage;
    private String fromStageDisplayName;
    private CurriculumTrackingStage toStage;
    private String toStageDisplayName;
    private String comments;
    private LocalDateTime actionDate;
    private LocalDateTime dueDate;
    private boolean isMilestone;
    private boolean isStageTransition;
    private boolean isForwardMovement;
    private boolean isBackwardMovement;
    private List<CurriculumTrackingDocumentDto> documents;
}
