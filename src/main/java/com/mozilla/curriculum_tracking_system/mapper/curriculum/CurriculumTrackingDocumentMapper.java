package com.mozilla.curriculum_tracking_system.mapper.curriculum;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadResponse;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CurriculumTrackingDocumentMapper {

    /**
     * Convert CurriculumTrackingDocument entity to DTO
     */
    public CurriculumTrackingDocumentDto toDto(CurriculumTrackingDocument document) {
        if (document == null) {
            return null;
        }

        return CurriculumTrackingDocumentDto.builder()
                .id(document.getId())
                .trackingHistoryId(document.getTrackingHistory() != null
                        ? document.getTrackingHistory().getId() : null)
                .documentName(document.getDocumentName())
                .originalFilename(document.getOriginalFilename())
                .firebaseUrl(document.getFirebaseUrl())
                .firebasePath(document.getFirebasePath())
                .fileSize(document.getFileSize())
                .formattedFileSize(document.getFormattedFileSize())
                .contentType(document.getContentType())
                .fileExtension(document.getFileExtension())
                .description(document.getDescription())
                .uploadedBy(document.getUploadedBy())
                .uploadedByEmail(document.getUploadedByEmail())
                .uploadedAt(document.getUploadedAt())
                .isActive(document.isActive())
                .documentVersion(document.getDocumentVersion())
                .isPdf(document.isPdf())
                .isWordDocument(document.isWordDocument())
                .build();
    }

    /**
     * Convert list of CurriculumTrackingDocument entities to DTOs
     */
    public List<CurriculumTrackingDocumentDto> toDtoList(List<CurriculumTrackingDocument> documents) {
        if (documents == null) {
            return null;
        }
        return documents.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create document upload response
     */
    public DocumentUploadResponse buildUploadResponse(CurriculumTrackingDocument document, String message) {
        if (document == null) {
            return DocumentUploadResponse.builder()
                    .message(message != null ? message : "Upload failed")
                    .build();
        }

        return DocumentUploadResponse.builder()
                .documentId(document.getId())
                .firebaseUrl(document.getFirebaseUrl())
                .firebasePath(document.getFirebasePath())
                .originalFilename(document.getOriginalFilename())
                .formattedFileSize(document.getFormattedFileSize())
                .message(message != null ? message : "Upload successful")
                .build();
    }
}