package com.mozilla.curriculum_tracking_system.dto.curriculum;

import com.mozilla.curriculum_tracking_system.enums.CurriculumStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumDto {

    private String name;
    private String code;
    private Integer durationSemesters;
    private CurriculumStatus status;
    private Long CreatedBy;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long schoolId;
    private String schoolName;
    private Long departmentId;
    private String departmentName;
    private String academicLevelName;
}
