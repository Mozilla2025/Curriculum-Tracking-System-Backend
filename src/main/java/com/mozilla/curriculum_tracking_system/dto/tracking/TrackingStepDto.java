package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
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
public class TrackingStepDto {
    private Long id;
    private TrackingStage stage;
    private String stageDisplayName;
    private TrackingAction action;
    private String actionDisplayName;
    private String performedByName;
    private String performedByEmail;
    private String assignedToName;
    private String assignedToEmail;
    private TrackingStage fromStage;
    private TrackingStage toStage;
    private String notes;
    private LocalDateTime performedAt;
    private LocalDateTime dueDate;
    private Boolean isMilestone;
    private Boolean isStageTransition;
    private Boolean isForwardMovement;
    private Boolean isBackwardMovement;
    private List<TrackingDocumentDto> documents;
}
