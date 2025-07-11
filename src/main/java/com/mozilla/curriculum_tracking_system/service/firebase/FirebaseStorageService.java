package com.mozilla.curriculum_tracking_system.service.firebase;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseStorageService implements IFirebaseStorageService{

    private final Storage storage;

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${app.firebase.max-file-size:52428800}") // 50MB
    private long maxFileSize;


    @Getter
    @Value("${app.firebase.signed-url-duration-hours:24}")
    private int signedUrlDurationHours;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    @Override
    public String uploadFile(MultipartFile file, String path) throws Exception {
        log.info("Uploading file to Firebase Storage: {}", path);

        validateFile(file);

        try {
            BlobId blobId = BlobId.of(bucketName, path);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setMetadata(buildMetadata(file))
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());

            log.info("Successfully uploaded file to Firebase Storage: {}", path);

            return generateSignedUrl(path);

        } catch (Exception e) {
            log.error("Failed to upload file to Firebase Storage: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String path) throws Exception {
        log.info("Deleting file from Firebase Storage: {}", path);

        try {
            BlobId blobId = BlobId.of(bucketName, path);
            boolean deleted = storage.delete(blobId);

            if (!deleted) {
                log.warn("File not found or already deleted: {}", path);
            } else {
                log.info("Successfully deleted file from Firebase Storage: {}", path);
            }

        } catch (Exception e) {
            log.error("Failed to delete file from Firebase Storage: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String getFileDownloadUrl(String path) throws Exception {
        log.debug("Getting download URL for file: {}", path);

        try {
            return generateSignedUrl(path);
        } catch (Exception e) {
            log.error("Failed to get download URL for file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to get download URL: " + e.getMessage());
        }
    }


    public String generateSignedUrl(String path) throws Exception {
        log.debug("Generating signed URL for file: {}", path);

        try {
            BlobId blobId = BlobId.of(bucketName, path);
            Blob blob = storage.get(blobId);

            if (blob == null || !blob.exists()) {
                throw new BadRequestException("File not found: " + path);
            }

            return blob.signUrl(signedUrlDurationHours, TimeUnit.HOURS).toString();

        } catch (Exception e) {
            log.error("Failed to generate signed URL for file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate signed URL: " + e.getMessage());
        }
    }


    public String generateSignedUrl(String path, int durationHours) throws Exception {
        log.debug("Generating signed URL for file: {} with duration: {} hours", path, durationHours);

        try {
            BlobId blobId = BlobId.of(bucketName, path);
            Blob blob = storage.get(blobId);

            if (blob == null || !blob.exists()) {
                throw new BadRequestException("File not found: " + path);
            }

            return blob.signUrl(durationHours, TimeUnit.HOURS).toString();

        } catch (Exception e) {
            log.error("Failed to generate signed URL for file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate signed URL: " + e.getMessage());
        }
    }


    public String refreshSignedUrl(String path) throws Exception {
        log.debug("Refreshing signed URL for file: {}", path);
        return generateSignedUrl(path);
    }


    public List<String> generateSignedUrls(List<String> paths) throws Exception {
        log.debug("Generating signed URLs for {} files", paths.size());

        return paths.stream()
                .map(path -> {
                    try {
                        return generateSignedUrl(path);
                    } catch (Exception e) {
                        log.warn("Failed to generate signed URL for path: {}", path, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean fileExists(String path) throws Exception {
        log.debug("Checking if file exists: {}", path);

        try {
            BlobId blobId = BlobId.of(bucketName, path);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();

        } catch (Exception e) {
            log.error("Failed to check file existence: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Object getFileMetadata(String path) throws Exception {
        log.debug("Getting file metadata: {}", path);

        try {
            BlobId blobId = BlobId.of(bucketName, path);
            Blob blob = storage.get(blobId);

            if (blob == null || !blob.exists()) {
                throw new BadRequestException("File not found: " + path);
            }

            return FileMetadata.builder()
                    .name(blob.getName())
                    .size(blob.getSize())
                    .contentType(blob.getContentType())
                    .createdTime(blob.getCreateTime())
                    .updatedTime(blob.getUpdateTime())
                    .md5Hash(blob.getMd5())
                    .metadata(blob.getMetadata())
                    .signedUrl(generateSignedUrl(path))
                    .build();

        } catch (Exception e) {
            log.error("Failed to get file metadata: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to get file metadata: " + e.getMessage());
        }
    }

    @Override
    public String generateCurriculumTrackingPath(Long curriculumId, Long trackingHistoryId, String filename) {
        log.debug("Generating path for curriculum {} tracking history {} file: {}",
                curriculumId, trackingHistoryId, filename);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFilename = sanitizeFilename(filename);

        return String.format("curriculum-tracking/%d/%s/%d/%s_%s",
                curriculumId, timestamp, trackingHistoryId, uniqueId, sanitizedFilename);
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required and cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException(String.format(
                    "File size exceeds maximum allowed size of %d MB",
                    maxFileSize / (1024 * 1024)));
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "File type not allowed. Supported types: " + String.join(", ", getAllowedFileExtensions()));
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BadRequestException("File must have a valid filename");
        }

        validateFileExtension(originalFilename, contentType);
    }

    @Override
    public List<String> getAllowedFileTypes() {
        return List.copyOf(ALLOWED_FILE_TYPES);
    }

    @Override
    public long getMaxFileSize() {
        return maxFileSize;
    }


    public String updateDocumentUrl(String firebasePath) throws Exception {
        return generateSignedUrl(firebasePath);
    }


    private java.util.Map<String, String> buildMetadata(MultipartFile file) {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        metadata.put("originalName", file.getOriginalFilename());
        metadata.put("uploadedAt", LocalDateTime.now().toString());
        metadata.put("size", String.valueOf(file.getSize()));
        metadata.put("signedUrlGenerated", LocalDateTime.now().toString());
        return metadata;
    }

    private String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "unknown";
        }

        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .toLowerCase();
    }

    private void validateFileExtension(String filename, String contentType) {
        String extension = getFileExtension(filename).toLowerCase();

        switch (contentType) {
            case "application/pdf":
                if (!"pdf".equals(extension)) {
                    throw new BadRequestException("File extension does not match content type");
                }
                break;
            case "application/msword":
                if (!"doc".equals(extension)) {
                    throw new BadRequestException("File extension does not match content type");
                }
                break;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                if (!"docx".equals(extension)) {
                    throw new BadRequestException("File extension does not match content type");
                }
                break;
        }
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private List<String> getAllowedFileExtensions() {
        return Arrays.asList(
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                "txt", "jpg", "jpeg", "png", "gif"
        );
    }

    @Builder
    @Data
    public static class FileMetadata {
        private String name;
        private Long size;
        private String contentType;
        private Long createdTime;
        private Long updatedTime;
        private String md5Hash;
        private java.util.Map<String, String> metadata;
        private String signedUrl;
    }
}