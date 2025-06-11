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
    private long pendingCurriculums;
    private long approvedCurriculums;
    private long rejectedCurriculums;
    private long underReviewCurriculums;
}
