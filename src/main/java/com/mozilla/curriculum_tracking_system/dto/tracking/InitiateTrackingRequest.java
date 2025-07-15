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
public class InitiateTrackingRequest {
    @NotNull(message = "Curriculum ID is required")
    private Long curriculumId;

    private String initialNotes;
    private LocalDateTime expectedCompletionDate;
    private List<MultipartFile> documents;
}
