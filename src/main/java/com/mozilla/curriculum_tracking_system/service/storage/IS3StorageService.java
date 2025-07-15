package com.mozilla.curriculum_tracking_system.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * General service interface for AWS S3 storage operations
 * Provides core S3 functionality that can be extended for specific use cases
 */
public interface IS3StorageService {

    /**
     * Upload a single file to S3
     *
     * @param file        The file to upload
     * @param bucketName  The S3 bucket name
     * @param key         The S3 object key (file path)
     * @param contentType The content type of the file
     * @param metadata    Optional metadata to attach to the file
     * @return The S3 object key of the uploaded file
     */
    String uploadFile(MultipartFile file, String bucketName, String key, String contentType, Map<String, String> metadata);

    /**
     * Upload multiple files to S3
     *
     * @param files       List of files to upload
     * @param bucketName  The S3 bucket name
     * @param keys        List of S3 object keys (must match files count)
     * @param contentType The content type of the files
     * @param metadata    Optional metadata to attach to the files
     * @return List of S3 object keys of the uploaded files
     */
    List<String> uploadFiles(List<MultipartFile> files, String bucketName, List<String> keys, String contentType, Map<String, String> metadata);

    /**
     * Download a file from S3
     *
     * @param bucketName The S3 bucket name
     * @param key        The S3 object key
     * @return InputStream of the file content
     */
    InputStream downloadFile(String bucketName, String key);

    /**
     * Get a pre-signed URL for file download
     *
     * @param bucketName        The S3 bucket name
     * @param key               The S3 object key
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed download URL
     */
    String getDownloadUrl(String bucketName, String key, int expirationMinutes);

    /**
     * Get a pre-signed URL for file upload
     *
     * @param bucketName        The S3 bucket name
     * @param key               The S3 object key
     * @param contentType       The content type
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed upload URL
     */
    String getUploadUrl(String bucketName, String key, String contentType, int expirationMinutes);

    /**
     * Delete a file from S3
     *
     * @param bucketName The S3 bucket name
     * @param key        The S3 object key
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteFile(String bucketName, String key);

    /**
     * Delete multiple files from S3
     *
     * @param bucketName The S3 bucket name
     * @param keys       List of S3 object keys
     * @return Map of object key to deletion status
     */
    Map<String, Boolean> deleteFiles(String bucketName, List<String> keys);

    /**
     * Check if a file exists in S3
     *
     * @param bucketName The S3 bucket name
     * @param key        The S3 object key
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String bucketName, String key);

    /**
     * List files in a bucket with optional prefix
     *
     * @param bucketName The S3 bucket name
     * @param prefix     Optional prefix to filter files
     * @param maxKeys    Maximum number of keys to return
     * @return List of S3 object keys
     */
    List<String> listFiles(String bucketName, String prefix, int maxKeys);

    /**
     * Get file metadata without downloading the file
     *
     * @param bucketName The S3 bucket name
     * @param key        The S3 object key
     * @return File metadata information
     */
    S3FileMetadata getFileMetadata(String bucketName, String key);

    /**
     * Copy a file within S3
     *
     * @param sourceBucket The source bucket name
     * @param sourceKey    The source object key
     * @param destBucket   The destination bucket name
     * @param destKey      The destination object key
     * @return true if copied successfully, false otherwise
     */
    boolean copyFile(String sourceBucket, String sourceKey, String destBucket, String destKey);

    /**
     * Move a file within S3 (copy then delete)
     *
     * @param sourceBucket The source bucket name
     * @param sourceKey    The source object key
     * @param destBucket   The destination bucket name
     * @param destKey      The destination object key
     * @return true if moved successfully, false otherwise
     */
    boolean moveFile(String sourceBucket, String sourceKey, String destBucket, String destKey);

    /**
     * Get storage statistics for a bucket
     *
     * @param bucketName The S3 bucket name
     * @param prefix     Optional prefix to filter statistics
     * @return Map with storage statistics
     */
    Map<String, Object> getStorageStatistics(String bucketName, String prefix);

    /**
     * Generate a unique file key for S3 storage
     *
     * @param prefix           Optional prefix for the key
     * @param originalFilename The original filename
     * @return Unique S3 object key
     */
    String generateUniqueKey(String prefix, String originalFilename);

    /**
     * Validate file before upload
     *
     * @param file            The file to validate
     * @param maxSizeBytes    Maximum allowed file size in bytes
     * @param allowedTypes    List of allowed content types
     * @param allowedExtensions List of allowed file extensions
     * @return Validation result with any error messages
     */
    S3ValidationResult validateFile(MultipartFile file, long maxSizeBytes, List<String> allowedTypes, List<String> allowedExtensions);

    /**
     * Clean up files older than specified days
     *
     * @param bucketName The S3 bucket name
     * @param prefix     Optional prefix to filter files
     * @param daysOld    Files older than this many days will be cleaned
     * @param dryRun     If true, only report what would be cleaned, don't actually delete
     * @return List of file keys that were or would be cleaned
     */
    List<String> cleanupOldFiles(String bucketName, String prefix, int daysOld, boolean dryRun);

    /**
     * Set file metadata
     *
     * @param bucketName The S3 bucket name
     * @param key        The S3 object key
     * @param metadata   Metadata to set
     * @return true if metadata was set successfully, false otherwise
     */
    boolean setFileMetadata(String bucketName, String key, Map<String, String> metadata);

    /**
     * Get file size in bytes
     *
     * @param bucketName The S3 bucket name
     * @param key        The S3 object key
     * @return File size in bytes, -1 if file doesn't exist
     */
    long getFileSize(String bucketName, String key);

    /**
     * Check if bucket exists
     *
     * @param bucketName The S3 bucket name
     * @return true if bucket exists, false otherwise
     */
    boolean bucketExists(String bucketName);

    /**
     * Create a new bucket
     *
     * @param bucketName The S3 bucket name
     * @param region     AWS region for the bucket
     * @return true if bucket was created successfully, false otherwise
     */
    boolean createBucket(String bucketName, String region);

    /**
     * File metadata record
     */
    record S3FileMetadata(
            String key,
            long size,
            String contentType,
            String etag,
            java.time.Instant lastModified,
            Map<String, String> metadata
    ) {}

    /**
     * Validation result record
     */
    record S3ValidationResult(
            boolean valid,
            String errorMessage,
            List<String> warnings
    ) {}
}