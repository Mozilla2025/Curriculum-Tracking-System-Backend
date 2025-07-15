package com.mozilla.curriculum_tracking_system.dto.tracking.search;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering curriculum tracking records
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingSearchCriteria {
    private TrackingStatus status;
    private TrackingStage currentStage;
    private Long initiatedByUserId;
    private Long currentAssigneeId;
    private Long schoolId;
    private Long departmentId;
    private Long curriculumId;
    private String searchTerm; // For curriculum name, code, or tracking ID
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime expectedCompletionBefore;
    private Boolean isActive;
    private Boolean isOverdue;
}

