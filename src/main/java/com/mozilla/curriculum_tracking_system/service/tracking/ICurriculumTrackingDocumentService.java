package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for managing curriculum tracking documents
 */
public interface ICurriculumTrackingDocumentService {

    /**
     * Upload document to tracking history
     */
    DocumentUploadResponse uploadDocument(DocumentUploadRequest request, String authToken);

    /**
     * Upload multiple documents
     */
    List<DocumentUploadResponse> uploadMultipleDocuments(Long trackingHistoryId,
                                                         List<MultipartFile> files,
                                                         String description,
                                                         String authToken);

    /**
     * Get document by ID
     */
    CurriculumTrackingDocumentDto getDocumentById(Long documentId);

    /**
     * Get documents by tracking history ID
     */
    List<CurriculumTrackingDocumentDto> getDocumentsByTrackingHistoryId(Long trackingHistoryId);

    /**
     * Get documents by curriculum tracking ID
     */
    List<CurriculumTrackingDocumentDto> getDocumentsByCurriculumTrackingId(Long curriculumTrackingId);

    /**
     * Download document (returns Firebase URL for frontend)
     */
    String getDocumentDownloadUrl(Long documentId, String authToken);

    /**
     * Delete document (soft delete)
     */
    void deleteDocument(Long documentId, String authToken);

    /**
     * Search documents
     */
    List<CurriculumTrackingDocumentDto> searchDocuments(String searchTerm, Pageable pageable);

    /**
     * Get documents by content type
     */
    List<CurriculumTrackingDocumentDto> getDocumentsByContentType(String contentType);

    /**
     * Get document versions
     */
    List<CurriculumTrackingDocumentDto> getDocumentVersions(String documentName, Long trackingHistoryId);

    /**
     * Get latest version of document
     */
    CurriculumTrackingDocumentDto getLatestDocumentVersion(String documentName, Long trackingHistoryId);

    /**
     * Update document description
     */
    CurriculumTrackingDocumentDto updateDocumentDescription(Long documentId, String description, String authToken);

    /**
     * Get storage statistics
     */
    Object getStorageStatistics();

    /**
     * Validate file upload
     */
    void validateFileUpload(MultipartFile file);

    /**
     * Generate unique filename for Firebase storage
     */
    String generateUniqueFilename(String originalFilename, Long trackingHistoryId);
}
