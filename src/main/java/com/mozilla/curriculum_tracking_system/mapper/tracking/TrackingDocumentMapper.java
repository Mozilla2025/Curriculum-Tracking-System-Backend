package com.mozilla.curriculum_tracking_system.mapper.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingDocument;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for TrackingDocument entities and DTOs
 * Handles conversions between document entities and their corresponding DTOs
 */
@Component
public class TrackingDocumentMapper {

    /**
     * Convert TrackingDocument entity to DTO
     */
    public TrackingDocumentDto toDto(TrackingDocument document) {
        if (document == null) {
            return null;
        }

        return TrackingDocumentDto.builder()
                .id(document.getId())
                .documentName(document.getDocumentName())
                .originalFilename(document.getOriginalFilename())
                .documentType(document.getDocumentType())
                .documentTypeDisplayName(document.getDocumentType().getDisplayName())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .formattedFileSize(document.getFormattedFileSize())
                .contentType(document.getContentType())
                .fileExtension(document.getFileExtension())
                .description(document.getDescription())
                .uploadedByName(getFullName(document.getUploadedBy()))
                .versionNumber(document.getVersionNumber())
                .uploadedAt(document.getUploadedAt())
                .isActive(document.getIsActive())
                .build();
    }

    /**
     * Convert MultipartFile and metadata to TrackingDocument entity
     */
    public TrackingDocument toEntity(MultipartFile file,
                                     TrackingStep trackingStep,
                                     User uploadedBy,
                                     DocumentType documentType,
                                     String description,
                                     String filePath,
                                     Integer versionNumber) {
        if (file == null || trackingStep == null || uploadedBy == null) {
            return null;
        }

        String documentName = generateDocumentName(file.getOriginalFilename());

        return TrackingDocument.builder()
                .trackingStep(trackingStep)
                .documentName(documentName)
                .originalFilename(file.getOriginalFilename())
                .documentType(documentType != null ? documentType : DocumentType.OTHER)
                .filePath(filePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .description(description)
                .uploadedBy(uploadedBy)
                .versionNumber(versionNumber != null ? versionNumber : 1)
                .isActive(true)
                .build();
    }

    /**
     * Convert list of TrackingDocument entities to DTOs
     */
    public List<TrackingDocumentDto> toDtoList(List<TrackingDocument> documents) {
        if (documents == null) {
            return null;
        }

        return documents.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new version of an existing document
     */
    public TrackingDocument createNewVersion(TrackingDocument originalDocument,
                                             MultipartFile newFile,
                                             TrackingStep newTrackingStep,
                                             User uploadedBy,
                                             String newFilePath,
                                             String description) {
        if (originalDocument == null || newFile == null) {
            return null;
        }

        return TrackingDocument.builder()
                .trackingStep(newTrackingStep)
                .documentName(originalDocument.getDocumentName())
                .originalFilename(newFile.getOriginalFilename())
                .documentType(originalDocument.getDocumentType())
                .filePath(newFilePath)
                .fileSize(newFile.getSize())
                .contentType(newFile.getContentType())
                .description(description != null ? description : originalDocument.getDescription())
                .uploadedBy(uploadedBy)
                .versionNumber(originalDocument.getVersionNumber() + 1)
                .isActive(true)
                .build();
    }

    /**
     * Update document metadata
     */
    public void updateDocumentMetadata(TrackingDocument document,
                                       String description,
                                       DocumentType documentType) {
        if (document == null) {
            return;
        }

        if (description != null) {
            document.setDescription(description);
        }
        if (documentType != null) {
            document.setDocumentType(documentType);
        }
    }

    /**
     * Helper method to get full name from User entity
     */
    private String getFullName(User user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";

        return (firstName + " " + lastName).trim();
    }

    /**
     * Generate a standardized document name from filename
     */
    private String generateDocumentName(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "unnamed_document_" + System.currentTimeMillis();
        }

        String nameWithoutExtension = originalFilename.contains(".")
                ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
                : originalFilename;

        String cleanName = nameWithoutExtension
                .replaceAll("[^a-zA-Z0-9\\s\\-_]", "_")
                .replaceAll("\\s+", "_")
                .toLowerCase();

        if (cleanName.length() > 100) {
            cleanName = cleanName.substring(0, 100);
        }

        return cleanName;
    }

    /**
     * Check if document is an updated version of another document
     */
    public boolean isUpdatedVersion(TrackingDocument document1, TrackingDocument document2) {
        if (document1 == null || document2 == null) {
            return false;
        }

        return document1.getDocumentName().equals(document2.getDocumentName()) &&
                document1.getDocumentType().equals(document2.getDocumentType()) &&
                !document1.getId().equals(document2.getId());
    }

    /**
     * Create a document for stage transition
     */
    public TrackingDocument createStageDocument(TrackingStep trackingStep,
                                                User uploadedBy,
                                                String notes,
                                                DocumentType documentType) {
        if (trackingStep == null || uploadedBy == null) {
            return null;
        }

        String documentName = "stage_transition_" +
                trackingStep.getStage().name().toLowerCase() + "_" +
                System.currentTimeMillis();

        return TrackingDocument.builder()
                .trackingStep(trackingStep)
                .documentName(documentName)
                .originalFilename(documentName + ".txt")
                .documentType(documentType != null ? documentType : DocumentType.OTHER)
                .filePath(null)
                .fileSize(notes != null ? notes.getBytes().length : 0L)
                .contentType("text/plain")
                .description("Stage transition notes")
                .uploadedBy(uploadedBy)
                .versionNumber(1)
                .isActive(true)
                .build();
    }
}
