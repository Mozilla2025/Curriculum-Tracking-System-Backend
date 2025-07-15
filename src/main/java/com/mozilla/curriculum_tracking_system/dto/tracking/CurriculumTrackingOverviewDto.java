package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingOverviewDto {
    private Long id;
    private String trackingId;

    private Long curriculumId;
    private String curriculumName;
    private String curriculumCode;

    private String displayCurriculumName;
    private String displayCurriculumCode;

    private String proposedCurriculumName;
    private String proposedCurriculumCode;

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
    private String currentAssigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime expectedCompletionDate;
    private Boolean isActive;
    private Boolean isIdeationStage;
}