package com.mozilla.curriculum_tracking_system.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCurriculumRequest {
    private String name;
    private String code;
    private Integer durationSemesters;
    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
    private Long departmentId;
    private Long academicLevelId;
}
