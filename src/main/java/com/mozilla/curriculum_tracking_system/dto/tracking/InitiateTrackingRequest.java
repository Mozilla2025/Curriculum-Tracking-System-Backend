package com.mozilla.curriculum_tracking_system.dto.tracking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateTrackingRequest {

    private Long curriculumId;

    @NotNull(message = "School ID is required")
    private Long schoolId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Academic level ID is required")
    private Long academicLevelId;

    @NotBlank(message = "Proposed curriculum name is required")
    @Size(max = 200, message = "Curriculum name must not exceed 200 characters")
    private String proposedCurriculumName;

    @Size(max = 20, message = "Curriculum code must not exceed 20 characters")
    private String proposedCurriculumCode;

    @Positive(message = "Duration semesters must be positive")
    private Integer proposedDurationSemesters;

    @Size(max = 2000, message = "Curriculum description must not exceed 2000 characters")
    private String curriculumDescription;

    private LocalDateTime proposedEffectiveDate;
    private LocalDateTime proposedExpiryDate;

    @Size(max = 1000, message = "Initial notes must not exceed 1000 characters")
    private String initialNotes;

    private LocalDateTime expectedCompletionDate;

    private List<MultipartFile> documents;
}