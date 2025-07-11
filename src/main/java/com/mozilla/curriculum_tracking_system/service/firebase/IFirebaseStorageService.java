package com.mozilla.curriculum_tracking_system.service.firebase;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Firebase storage operations
 */
public interface IFirebaseStorageService {

    /**
     * Upload file to Firebase Storage
     */
    String uploadFile(MultipartFile file, String path) throws Exception;

    /**
     * Delete file from Firebase Storage
     */
    void deleteFile(String path) throws Exception;

    /**
     * Get file download URL (signed URL)
     */
    String getFileDownloadUrl(String path) throws Exception;

    /**
     * Generate a signed URL for a file with default duration
     */
    String generateSignedUrl(String path) throws Exception;

    /**
     * Generate a signed URL for a file with custom duration
     */
    String generateSignedUrl(String path, int durationHours) throws Exception;

    /**
     * Refresh/regenerate signed URL for a document
     */
    String refreshSignedUrl(String path) throws Exception;

    /**
     * Generate signed URLs for multiple files
     */
    List<String> generateSignedUrls(List<String> paths) throws Exception;

    /**
     * Check if file exists
     */
    boolean fileExists(String path) throws Exception;

    /**
     * Get file metadata with signed URL
     */
    Object getFileMetadata(String path) throws Exception;

    /**
     * Generate unique path for curriculum tracking documents
     */
    String generateCurriculumTrackingPath(Long curriculumId, Long trackingHistoryId, String filename);

    /**
     * Validate file size and type
     */
    void validateFile(MultipartFile file);

    /**
     * Get allowed file types for curriculum documents
     */
    List<String> getAllowedFileTypes();

    /**
     * Get maximum file size limit
     */
    long getMaxFileSize();

    /**
     * Get the current signed URL duration in hours
     */
    int getSignedUrlDurationHours();

    /**
     * Update the document firebase URL with a fresh signed URL
     */
    String updateDocumentUrl(String firebasePath) throws Exception;
}