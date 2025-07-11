package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumTrackingDocumentDto {
    private Long id;
    private Long trackingHistoryId;
    private String documentName;
    private String originalFilename;
    private String firebaseUrl;
    private String firebasePath;
    private Long fileSize;
    private String formattedFileSize;
    private String contentType;
    private String fileExtension;
    private String description;
    private Long uploadedBy;
    private String uploadedByEmail;
    private LocalDateTime uploadedAt;
    private boolean isActive;
    private Integer documentVersion;
    private boolean isPdf;
    private boolean isWordDocument;
}
