package com.mozilla.curriculum_tracking_system.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumPageResponse {
    private List<CurriculumDto> curriculums;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
