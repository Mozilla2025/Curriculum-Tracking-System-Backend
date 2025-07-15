package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for tracking document information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingDocumentDto {
    private Long id;
    private String documentName;
    private String originalFilename;
    private DocumentType documentType;
    private String documentTypeDisplayName;
    private String filePath;
    private Long fileSize;
    private String formattedFileSize;
    private String contentType;
    private String fileExtension;
    private String description;
    private String uploadedByName;
    private Integer versionNumber;
    private LocalDateTime uploadedAt;
    private Boolean isActive;
}
