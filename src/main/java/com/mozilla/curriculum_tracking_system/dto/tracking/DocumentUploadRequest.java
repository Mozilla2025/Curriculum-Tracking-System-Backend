package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRequest {
    @NotNull(message = "Tracking history ID is required")
    private Long trackingHistoryId;

    @NotNull(message = "Document file is required")
    private MultipartFile file;

    private String description;

    private String documentName;
}
