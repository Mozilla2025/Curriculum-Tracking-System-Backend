package com.mozilla.curriculum_tracking_system.service.tracking;

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
     * Get file download URL
     */
    String getFileDownloadUrl(String path) throws Exception;

    /**
     * Check if file exists
     */
    boolean fileExists(String path) throws Exception;

    /**
     * Get file metadata
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
}

