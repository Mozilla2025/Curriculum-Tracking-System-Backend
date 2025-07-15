package com.mozilla.curriculum_tracking_system.service.storage;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service interface for tracking document storage operations
 * Extends the general S3 storage service with tracking-specific functionality
 */
public interface ITrackingDocumentStorageService extends IS3StorageService {

    /**
     * Upload a single document to S3 for tracking
     *
     * @param file         The file to upload
     * @param trackingId   The tracking ID
     * @param stepId       The tracking step ID
     * @param documentType The type of document
     * @param description  Optional description
     * @param uploadedBy   User ID who uploaded the file
     * @return The uploaded document DTO
     */
    TrackingDocumentDto uploadDocument(MultipartFile file,
                                       Long trackingId,
                                       Long stepId,
                                       DocumentType documentType,
                                       String description,
                                       Long uploadedBy);

    /**
     * Upload multiple documents to S3 for tracking
     *
     * @param files        List of files to upload
     * @param trackingId   The tracking ID
     * @param stepId       The tracking step ID
     * @param documentType The type of documents
     * @param descriptions Optional descriptions (must match files count or be null)
     * @param uploadedBy   User ID who uploaded the files
     * @return List of uploaded document DTOs
     */
    List<TrackingDocumentDto> uploadDocuments(List<MultipartFile> files,
                                              Long trackingId,
                                              Long stepId,
                                              DocumentType documentType,
                                              List<String> descriptions,
                                              Long uploadedBy);

    /**
     * Download a document from S3 using document ID
     *
     * @param documentId The document ID
     * @return InputStream of the file content
     */
    InputStream downloadDocument(Long documentId);

    /**
     * Get a pre-signed URL for document download using document ID
     *
     * @param documentId        The document ID
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed download URL
     */
    String getDocumentDownloadUrl(Long documentId, int expirationMinutes);

    /**
     * Get a pre-signed URL for document upload for tracking
     *
     * @param trackingId        The tracking ID
     * @param stepId            The step ID
     * @param fileName          The file name
     * @param contentType       The content type
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed upload URL
     */
    String getDocumentUploadUrl(Long trackingId, Long stepId, String fileName, String contentType, int expirationMinutes);

    /**
     * Delete a document from S3 using document ID
     *
     * @param documentId The document ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteDocument(Long documentId);

    /**
     * Delete multiple documents from S3 using document IDs
     *
     * @param documentIds List of document IDs
     * @return Map of document ID to deletion status
     */
    Map<Long, Boolean> deleteDocuments(List<Long> documentIds);

    /**
     * Create a new version of an existing document
     *
     * @param originalDocumentId The original document ID
     * @param newFile            The new file version
     * @param description        Optional description for the new version
     * @param uploadedBy         User ID who uploaded the new version
     * @return The new document version DTO
     */
    TrackingDocumentDto createDocumentVersion(Long originalDocumentId,
                                              MultipartFile newFile,
                                              String description,
                                              Long uploadedBy);

    /**
     * Get all versions of a document
     *
     * @param documentName The document name
     * @param trackingId   The tracking ID
     * @return List of document versions ordered by version number
     */
    List<TrackingDocumentDto> getDocumentVersions(String documentName, Long trackingId);

    /**
     * Get the latest version of a document
     *
     * @param documentName The document name
     * @param trackingId   The tracking ID
     * @return The latest document version DTO
     */
    TrackingDocumentDto getLatestDocumentVersion(String documentName, Long trackingId);

    /**
     * Archive old document versions (keep only latest N versions)
     *
     * @param documentName   The document name
     * @param trackingId     The tracking ID
     * @param versionsToKeep Number of latest versions to keep
     * @return Number of versions archived
     */
    int archiveOldVersions(String documentName, Long trackingId, int versionsToKeep);

    /**
     * Get documents by tracking ID
     *
     * @param trackingId The tracking ID
     * @return List of documents for the tracking
     */
    List<TrackingDocumentDto> getDocumentsByTracking(Long trackingId);

    /**
     * Get documents by tracking step ID
     *
     * @param stepId The tracking step ID
     * @return List of documents for the step
     */
    List<TrackingDocumentDto> getDocumentsByStep(Long stepId);

    /**
     * Get documents by type
     *
     * @param documentType The document type
     * @param trackingId   Optional tracking ID filter
     * @return List of documents of the specified type
     */
    List<TrackingDocumentDto> getDocumentsByType(DocumentType documentType, Long trackingId);

    /**
     * Search documents by name or description
     *
     * @param searchTerm The search term
     * @param trackingId Optional tracking ID filter
     * @return List of matching documents
     */
    List<TrackingDocumentDto> searchDocuments(String searchTerm, Long trackingId);

    /**
     * Get document metadata without downloading the file
     *
     * @param documentId The document ID
     * @return Document metadata DTO
     */
    TrackingDocumentDto getDocumentMetadata(Long documentId);

    /**
     * Update document metadata
     *
     * @param documentId   The document ID
     * @param description  New description
     * @param documentType New document type
     * @return Updated document DTO
     */
    TrackingDocumentDto updateDocumentMetadata(Long documentId, String description, DocumentType documentType);

    /**
     * Get document storage statistics for tracking
     *
     * @param trackingId Optional tracking ID filter
     * @return Map with storage statistics
     */
    Map<String, Object> getTrackingStorageStatistics(Long trackingId);

    /**
     * Get storage usage by document type for tracking
     *
     * @param trackingId Optional tracking ID filter
     * @return Map of document type to storage usage
     */
    Map<DocumentType, Long> getStorageUsageByType(Long trackingId);

    /**
     * Validate file before upload for tracking documents
     *
     * @param file         The file to validate
     * @param documentType The document type
     * @return Validation result with any error messages
     */
    TrackingValidationResult validateTrackingFile(MultipartFile file, DocumentType documentType);

    /**
     * Generate unique file path for tracking document S3 storage
     *
     * @param trackingId       The tracking ID
     * @param stepId           The step ID
     * @param originalFilename The original filename
     * @return Unique S3 file path for tracking documents
     */
    String generateTrackingFilePath(Long trackingId, Long stepId, String originalFilename);

    /**
     * Clean up orphaned tracking files (files not referenced in database)
     *
     * @param dryRun If true, only report what would be cleaned, don't actually delete
     * @return List of orphaned file paths
     */
    List<String> cleanupOrphanedTrackingFiles(boolean dryRun);

    /**
     * Copy document to another tracking
     *
     * @param documentId       The source document ID
     * @param targetTrackingId The target tracking ID
     * @param targetStepId     The target step ID
     * @param copiedBy         User ID who performed the copy
     * @return The copied document DTO
     */
    TrackingDocumentDto copyDocument(Long documentId, Long targetTrackingId, Long targetStepId, Long copiedBy);

    /**
     * Move document to another tracking
     *
     * @param documentId       The document ID to move
     * @param targetTrackingId The target tracking ID
     * @param targetStepId     The target step ID
     * @param movedBy          User ID who performed the move
     * @return The moved document DTO
     */
    TrackingDocumentDto moveDocument(Long documentId, Long targetTrackingId, Long targetStepId, Long movedBy);

    /**
     * Get tracking document bucket name
     *
     * @return The S3 bucket name used for tracking documents
     */
    String getTrackingDocumentBucketName();

    /**
     * Get maximum file size allowed for tracking documents
     *
     * @param documentType The document type
     * @return Maximum file size in bytes
     */
    long getMaxFileSize(DocumentType documentType);

    /**
     * Get allowed file types for tracking documents
     *
     * @param documentType The document type
     * @return List of allowed content types
     */
    List<String> getAllowedFileTypes(DocumentType documentType);

    /**
     * Get allowed file extensions for tracking documents
     *
     * @param documentType The document type
     * @return List of allowed file extensions
     */
    List<String> getAllowedFileExtensions(DocumentType documentType);

    /**
     * Result class for tracking file validation
     */
    record TrackingValidationResult(
            boolean valid,
            String errorMessage,
            List<String> warnings
    ) {}
}