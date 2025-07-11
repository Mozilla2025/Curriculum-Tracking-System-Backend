package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingStatsDto {
    private long totalTracked;
    private long underReview;
    private long accredited;
    private long approvedByCue;
    private long minorRevamp;
    private long majorRevamp;
    private long atSchoolBoard;
    private long atDeanCommittee;
    private long atSenate;
    private long atQaInternalReview;
    private long atViceChancellorReview;
    private long atCueExternalReview;
    private long completed;
    private double averageCompletionTimeInDays;
    private long overdueTasks;
}
