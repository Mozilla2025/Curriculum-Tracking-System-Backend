package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for tracking steps
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingStepPageResponse {
    private List<TrackingStepDto> steps;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
