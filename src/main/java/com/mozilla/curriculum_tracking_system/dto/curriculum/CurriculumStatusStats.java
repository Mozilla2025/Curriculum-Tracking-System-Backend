package com.mozilla.curriculum_tracking_system.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumStatusStats {
    private long totalCurriculums;
    private long underReviewCurriculums;
    private long approvedCurriculums;
    private long accreditedCurriculums;
    private long minorRevampCurriculums;
    private long majorRevampCurriculums;
}
