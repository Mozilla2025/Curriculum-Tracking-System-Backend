package com.mozilla.curriculum_tracking_system.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCurriculumRequest {
    @NotBlank(message = "Curriculum name is required")
    private String name;

    private String code;

    @Positive(message = "Duration semesters must be positive")
    private Integer durationSemesters;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;

    @NotNull(message = "School ID is required")
    private Long schoolId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Academic level ID is required")
    private Long academicLevelId;
}
