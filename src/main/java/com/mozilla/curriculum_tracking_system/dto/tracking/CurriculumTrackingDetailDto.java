package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
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
public class CurriculumTrackingDetailDto {
    private Long id;
    private String trackingId;

    private Long curriculumId;
    private String curriculumName;
    private String curriculumCode;

    private String displayCurriculumName;
    private String displayCurriculumCode;

    private String proposedCurriculumName;
    private String proposedCurriculumCode;
    private Integer proposedDurationSemesters;
    private String curriculumDescription;
    private LocalDateTime proposedEffectiveDate;
    private LocalDateTime proposedExpiryDate;

    private Long schoolId;
    private String schoolName;
    private Long departmentId;
    private String departmentName;
    private Long academicLevelId;
    private String academicLevelName;

    private TrackingStage currentStage;
    private String currentStageDisplayName;
    private TrackingStatus status;
    private String statusDisplayName;
    private String initiatedByName;
    private String initiatedByEmail;
    private String currentAssigneeName;
    private String currentAssigneeEmail;
    private String initialNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expectedCompletionDate;
    private LocalDateTime actualCompletionDate;
    private Boolean isActive;
    private Boolean isCompleted;
    private Boolean isIdeationStage;
    private List<TrackingStepDto> recentSteps;
}