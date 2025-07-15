package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for curriculum tracking overview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingPageResponse {
    private List<CurriculumTrackingOverviewDto> trackings;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
}
