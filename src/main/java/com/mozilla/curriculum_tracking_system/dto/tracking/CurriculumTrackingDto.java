package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
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
public class CurriculumTrackingDto {
    private Long id;
    private Long curriculumId;
    private String curriculumName;
    private String curriculumCode;
    private String schoolName;
    private String departmentName;
    private CurriculumTrackingStage currentStage;
    private String currentStageDisplayName;
    private CurriculumTrackingStatus status;
    private Long initiatedBy;
    private String initiatedByEmail;
    private Long currentAssignee;
    private String currentAssigneeEmail;
    private LocalDateTime initiatedAt;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime estimatedCompletionDate;
    private String notes;
    private boolean isActive;
    private boolean isCompleted;
    private int totalHistoryEntries;
    private int totalDocuments;
}
