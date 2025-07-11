package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingSearchRequest {
    private CurriculumTrackingStatus status;
    private CurriculumTrackingStage currentStage;
    private Long assignedToUserId;
    private Long initiatedByUserId;
    private Long curriculumId;
    private Long schoolId;
    private Long departmentId;
    private Boolean isActive;
    private LocalDateTime initiatedAfter;
    private LocalDateTime initiatedBefore;
    private LocalDateTime dueBefore;
    private String searchTerm;
}