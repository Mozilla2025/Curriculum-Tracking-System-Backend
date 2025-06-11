package com.mozilla.curriculum_tracking_system.dto.curriculum;

import com.mozilla.curriculum_tracking_system.enums.CurriculumStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumSearchCriteria {
    private String name;
    private String code;
    private CurriculumStatus status;
    private Long schoolId;
    private Long departmentId;
    private Long academicLevelId;
    private Long createdBy;
    private Boolean isActive;
}