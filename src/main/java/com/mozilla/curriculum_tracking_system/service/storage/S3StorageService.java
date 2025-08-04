package com.mozilla.curriculum_tracking_system.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements IS3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}:curriculumstorage")
    private String defaultBucket;

    @Value("${aws.region}")
    private String region;


    @Override
    public String uploadFile(MultipartFile file, String bucketName, String key, String contentType, Map<String, String> metadata) {
        validateUploadInputs(file, bucketName, key);

        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType != null ? contentType : file.getContentType())
                    .contentLength(file.getSize());

            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }

            PutObjectRequest request = requestBuilder.build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded file to S3: bucket={}, key={}", bucketName, key);
            return key;

        } catch (IOException e) {
            log.error("Failed to read file for upload: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        } catch (S3Exception e) {
            log.error("S3 upload failed: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String bucketName, List<String> keys, String contentType, Map<String, String> metadata) {
        validateBatchUploadInputs(files, keys);

        List<String> uploadedKeys = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            try {
                String uploadedKey = uploadFile(files.get(i), bucketName, keys.get(i), contentType, metadata);
                uploadedKeys.add(uploadedKey);
            } catch (Exception e) {
                log.error("Failed to upload file {}: {}", keys.get(i), e.getMessage());
                // Cleanup already uploaded files on failure
                cleanupFailedBatchUpload(bucketName, uploadedKeys);
                throw new RuntimeException("Batch upload failed at file: " + keys.get(i), e);
            }
        }

        return uploadedKeys;
    }

    @Override
    public InputStream downloadFile(String bucketName, String key) {
        validateDownloadInputs(bucketName, key);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(request);

        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: bucket={}, key={}", bucketName, key);
            throw new RuntimeException("File not found: " + key, e);
        } catch (S3Exception e) {
            log.error("S3 download failed: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDownloadUrl(String bucketName, String key, int expirationMinutes) {
        validateUrlInputs(bucketName, key, expirationMinutes);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (S3Exception e) {
            log.error("Failed to generate download URL: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            throw new RuntimeException("Failed to generate download URL: " + e.getMessage(), e);
        }
    }


    @Override
    public String getUploadUrl(String bucketName, String key, String contentType, int expirationMinutes) {
        validateUrlInputs(bucketName, key, expirationMinutes);

        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);

            if (contentType != null) {
                requestBuilder.contentType(contentType);
            }

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .putObjectRequest(requestBuilder.build())
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();

        } catch (S3Exception e) {
            log.error("Failed to generate upload URL: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            throw new RuntimeException("Failed to generate upload URL: " + e.getMessage(), e);
        }
    }



    @Override
    public boolean deleteFile(String bucketName, String key) {
        validateDeleteInputs(bucketName, key);

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Successfully deleted file from S3: bucket={}, key={}", bucketName, key);
            return true;

        } catch (S3Exception e) {
            log.error("Failed to delete file: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Boolean> deleteFiles(String bucketName, List<String> keys) {
        validateBatchDeleteInputs(bucketName, keys);

        List<ObjectIdentifier> objectsToDelete = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .collect(Collectors.toList());

        Delete delete = Delete.builder()
                .objects(objectsToDelete)
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(delete)
                .build();

        Map<String, Boolean> results = new HashMap<>();

        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(request);

            // Mark successful deletions
            response.deleted().forEach(deleted ->
                    results.put(deleted.key(), true));

            // Mark failed deletions
            response.errors().forEach(error -> {
                results.put(error.key(), false);
                log.error("Failed to delete key {}: {}", error.key(), error.message());
            });

            // Mark remaining keys as successful if not in errors
            keys.forEach(key -> results.putIfAbsent(key, true));

        } catch (S3Exception e) {
            log.error("Batch delete failed: bucket={}, error={}", bucketName, e.getMessage());
            keys.forEach(key -> results.put(key, false));
        }

        return results;
    }

    @Override
    public boolean fileExists(String bucketName, String key) {
        validateExistsInputs(bucketName, key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking file existence: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listFiles(String bucketName, String prefix, int maxKeys) {
        validateListInputs(bucketName, maxKeys);

        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .maxKeys(maxKeys);

            if (prefix != null && !prefix.trim().isEmpty()) {
                requestBuilder.prefix(prefix.trim());
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("Failed to list files: bucket={}, prefix={}, error={}", bucketName, prefix, e.getMessage());
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }

    @Override
    public S3FileMetadata getFileMetadata(String bucketName, String key) {
        validateMetadataInputs(bucketName, key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);

            return new S3FileMetadata(
                    key,
                    response.contentLength(),
                    response.contentType(),
                    response.eTag(),
                    response.lastModified(),
                    response.metadata()
            );

        } catch (NoSuchKeyException e) {
            log.error("File not found for metadata: bucket={}, key={}", bucketName, key);
            throw new RuntimeException("File not found: " + key, e);
        } catch (S3Exception e) {
            log.error("Failed to get file metadata: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            throw new RuntimeException("Failed to get file metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean copyFile(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        validateCopyInputs(sourceBucket, sourceKey, destBucket, destKey);

        try {
            CopyObjectRequest request = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(destBucket)
                    .destinationKey(destKey)
                    .build();

            s3Client.copyObject(request);
            log.info("Successfully copied file: {}/{} -> {}/{}", sourceBucket, sourceKey, destBucket, destKey);
            return true;

        } catch (S3Exception e) {
            log.error("Failed to copy file: {}/{} -> {}/{}, error={}",
                    sourceBucket, sourceKey, destBucket, destKey, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean moveFile(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        if (copyFile(sourceBucket, sourceKey, destBucket, destKey)) {
            return deleteFile(sourceBucket, sourceKey);
        }
        return false;
    }

    @Override
    public Map<String, Object> getStorageStatistics(String bucketName, String prefix) {
        List<String> files = listFiles(bucketName, prefix, 1000);

        Map<String, Object> stats = new HashMap<>();
        stats.put("fileCount", files.size());
        stats.put("totalSize", calculateTotalSize(bucketName, files));
        stats.put("lastUpdated", Instant.now());

        return stats;
    }

    @Override
    public String generateUniqueKey(String prefix, String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String cleanFilename = sanitizeFilename(originalFilename);

        if (prefix != null && !prefix.trim().isEmpty()) {
            return prefix.trim() + "/" + timestamp + "_" + cleanFilename;
        }

        return timestamp + "_" + cleanFilename;
    }

    @Override
    public S3ValidationResult validateFile(MultipartFile file, long maxSizeBytes, List<String> allowedTypes, List<String> allowedExtensions) {
        List<String> warnings = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            return new S3ValidationResult(false, "File is required", warnings);
        }

        if (file.getSize() > maxSizeBytes) {
            return new S3ValidationResult(false,
                    String.format("File size %d bytes exceeds maximum allowed size %d bytes",
                            file.getSize(), maxSizeBytes), warnings);
        }

        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType)) {
                return new S3ValidationResult(false,
                        "File type " + contentType + " is not allowed", warnings);
            }
        }

        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            String extension = getFileExtension(file.getOriginalFilename());
            if (!allowedExtensions.contains(extension.toLowerCase())) {
                return new S3ValidationResult(false,
                        "File extension " + extension + " is not allowed", warnings);
            }
        }

        return new S3ValidationResult(true, null, warnings);
    }

    @Override
    public List<String> cleanupOldFiles(String bucketName, String prefix, int daysOld, boolean dryRun) {
        Instant cutOffDate = Instant.now().minus(daysOld, ChronoUnit.DAYS);
        List<String> filesToCleanup = new ArrayList<>();

        try {
            List<String> allFiles = listFiles(bucketName, prefix, 1000);

            for (String key : allFiles) {
                S3FileMetadata metadata = getFileMetadata(bucketName, key);
                if (metadata.lastModified().isBefore(cutOffDate)) {
                    filesToCleanup.add(key);

                    if (!dryRun) {
                        deleteFile(bucketName, key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old files: {}", e.getMessage());
        }
        return filesToCleanup;
    }

    @Override
    public boolean setFileMetadata(String bucketName, String key, Map<String, String> metadata) {
        try {
            // Get current object
            S3FileMetadata currentMetadata = getFileMetadata(bucketName, key);

            // Copy the object with new metadata
            CopyObjectRequest request = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(key)
                    .destinationBucket(bucketName)
                    .destinationKey(key)
                    .metadata(metadata)
                    .metadataDirective(MetadataDirective.REPLACE)
                    .contentType(currentMetadata.contentType())
                    .build();

            s3Client.copyObject(request);
            return true;
        } catch (S3Exception e) {
            log.error("Failed to set metadata: bucket={}, key={}, error={}", bucketName, key, e.getMessage());
            return false;
        }
    }

    @Override
    public long getFileSize(String bucketName, String key) {
        try {
            S3FileMetadata metadata = getFileMetadata(bucketName, key);
            return metadata.size();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(request);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking bucket existence: bucket={}, error={}", bucketName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createBucket(String bucketName, String region) {
        try {
            CreateBucketRequest.Builder requestBuilder = CreateBucketRequest.builder()
                    .bucket(bucketName);

            if (!region.equals("us-east-1")) {
                requestBuilder.createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .locationConstraint(BucketLocationConstraint.fromValue(region))
                                .build()
                );
            }

            s3Client.createBucket(requestBuilder.build());
            log.info("Successfully created bucket: {}", bucketName);
            return true;
        } catch (S3Exception e) {
            log.error("Failed to create bucket: bucket={}, error={}", bucketName, e.getMessage());
            return false;
        }
    }

    private void validateUploadInputs(MultipartFile file, String bucketName, String key) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }

    private void validateBatchUploadInputs(List<MultipartFile> files, List<String> keys) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Files list cannot be null or empty");
        }
        if (keys == null || keys.size() != files.size()) {
            throw new IllegalArgumentException("Keys list must match files list size");
        }
    }

    private void validateDownloadInputs(String bucketName, String key) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }

    private void validateUrlInputs(String bucketName, String key, int expirationMinutes) {
        validateDownloadInputs(bucketName, key);
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("Expiration minutes must be positive");
        }
    }

    private void validateDeleteInputs(String bucketName, String key) {
        validateDownloadInputs(bucketName, key);
    }

    private void validateBatchDeleteInputs(String bucketName, List<String> keys) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("Keys list cannot be null or empty");
        }
    }

    private void validateExistsInputs(String bucketName, String key) {
        validateDownloadInputs(bucketName, key);
    }

    private void validateListInputs(String bucketName, int maxKeys) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (maxKeys <= 0) {
            throw new IllegalArgumentException("Max keys must be positive");
        }
    }

    private void validateMetadataInputs(String bucketName, String key) {
        validateDownloadInputs(bucketName, key);
    }

    private void validateCopyInputs(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        validateDownloadInputs(sourceBucket, sourceKey);
        validateDownloadInputs(destBucket, destKey);
    }

    private void cleanupFailedBatchUpload(String bucketName, List<String> uploadKeys) {
        if (!uploadKeys.isEmpty()) {
            log.info("Cleaning up {} files due to batch upload failure", uploadKeys.size());
            deleteFiles(bucketName, uploadKeys);
        }
    }

    private long calculateTotalSize(String bucketName, List<String> files) {
        return files.stream()
                .mapToLong(key -> getFileSize(bucketName, key))
                .filter(size -> size > 0)
                .sum();
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed_file";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
