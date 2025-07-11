package com.mozilla.curriculum_tracking_system.dto.tracking;

import jakarta.validation.constraints.NotNull;
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
public class InitiateCurriculumTrackingRequest {
    @NotNull(message = "Curriculum ID is required")
    private Long curriculumId;

    private String notes;

    private LocalDateTime estimatedCompletionDate;

    private List<MultipartFile> initialDocuments;

    private String initialComments;
}
