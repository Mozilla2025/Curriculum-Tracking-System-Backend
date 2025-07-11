package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {
    private Long documentId;
    private String firebaseUrl;
    private String firebasePath;
    private String originalFilename;
    private String formattedFileSize;
    private String message;
}
