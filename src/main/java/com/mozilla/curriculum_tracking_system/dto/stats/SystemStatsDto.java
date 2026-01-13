package com.mozilla.curriculum_tracking_system.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatsDto {
    private long totalSchools;
    private long totalDepartments;
    private long totalCurriculums;
}
