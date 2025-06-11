package com.mozilla.curriculum_tracking_system.dto.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {

    private String name;
    private String code;
    private Long headId;
    private Long schoolId;
    private String schoolName;
    private int curriculumCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
