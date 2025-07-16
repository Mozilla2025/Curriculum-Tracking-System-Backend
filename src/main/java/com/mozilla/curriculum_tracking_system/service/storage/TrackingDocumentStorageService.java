package com.mozilla.curriculum_tracking_system.service.storage;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.tracking.TrackingDocumentMapper;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingDocument;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingRepository;
import com.mozilla.curriculum_tracking_system.repository.tracking.TrackingDocumentRepository;
import com.mozilla.curriculum_tracking_system.repository.tracking.TrackingStepRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TrackingDocumentStorageService extends S3StorageService implements ITrackingDocumentStorageService {

    private final TrackingDocumentRepository documentRepository;
    private final CurriculumTrackingRepository trackingRepository;
    private final TrackingStepRepository stepRepository;
    private final UserRepository userRepository;
    private final TrackingDocumentMapper documentMapper;

    @Value("{aws.s3.bucket}")
    private String trackingDocumentBucket;

    // File size limits (in bytes)
    private static final long DEFAULT_MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long PROPOSAL_MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long AUDIT_MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB

    // Allowed file types
    private static final List<String> DOCUMENT_CONTENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "application/rtf"
    );

    private static final List<String> IMAGE_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp"
    );

    private static final List<String> SPREADSHEET_CONTENT_TYPES = Arrays.asList(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "txt", "rtf"
    );

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp"
    );

    private static final List<String> SPREADSHEET_EXTENSIONS = Arrays.asList(
            "xls", "xlsx"
    );

    public TrackingDocumentStorageService(
            S3Client s3Client,
            TrackingDocumentRepository documentRepository,
            CurriculumTrackingRepository trackingRepository,
            TrackingStepRepository stepRepository,
            UserRepository userRepository,
            TrackingDocumentMapper documentMapper) {
        super(s3Client);
        this.documentRepository = documentRepository;
        this.trackingRepository = trackingRepository;
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
        this.documentMapper = documentMapper;
    }

    @Override
    public TrackingDocumentDto uploadDocument(MultipartFile file, Long trackingId, Long stepId,
                                              DocumentType documentType, String description, Long uploadedBy) {
        validateTrackingDocumentInputs(file, trackingId, stepId, uploadedBy);

        CurriculumTracking tracking = findTrackingById(trackingId);
        TrackingStep step = findStepById(stepId);
        User user = findUserById(uploadedBy);

        // Validate tracking and step relationship
        validateStepBelongsToTracking(step, tracking);

        // Validate file
        TrackingValidationResult validation = validateTrackingFile(file, documentType);
        if (!validation.valid()) {
            throw new BadRequestException(validation.errorMessage());
        }

        // Generate unique file path
        String filePath = generateTrackingFilePath(trackingId, stepId, file.getOriginalFilename());

        // Determine version number
        Integer versionNumber = getNextVersionNumber(file.getOriginalFilename(), stepId);

        try {
            // Upload to S3
            Map<String, String> metadata = createFileMetadata(tracking, step, user, documentType);
            String uploadedKey = uploadFile(file, trackingDocumentBucket, filePath, file.getContentType(), metadata);

            // Create database record
            TrackingDocument document = documentMapper.toEntity(
                    file, step, user, documentType, description, uploadedKey, versionNumber
            );

            TrackingDocument savedDocument = documentRepository.save(document);
            log.info("Successfully uploaded tracking document: trackingId={}, stepId={}, documentId={}, filePath={}",
                    trackingId, stepId, savedDocument.getId(), filePath);

            return documentMapper.toDto(savedDocument);

        } catch (Exception e) {
            log.error("Failed to upload tracking document: trackingId={}, stepId={}, error={}",
                    trackingId, stepId, e.getMessage());
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TrackingDocumentDto> uploadDocuments(List<MultipartFile> files, Long trackingId, Long stepId,
                                                     DocumentType documentType, List<String> descriptions, Long uploadedBy) {
        validateBatchTrackingUpload(files, descriptions);

        List<TrackingDocumentDto> uploadedDocuments = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            String description = descriptions != null && i < descriptions.size() ? descriptions.get(i) : null;

            try {
                TrackingDocumentDto document = uploadDocument(files.get(i), trackingId, stepId,
                        documentType, description, uploadedBy);
                uploadedDocuments.add(document);
            } catch (Exception e) {
                log.error("Failed to upload document {} in batch: {}", i, e.getMessage());
                // Cleanup already uploaded documents
                cleanupBatchUploadFailure(uploadedDocuments);
                throw new RuntimeException("Batch upload failed at document " + i + ": " + e.getMessage(), e);
            }
        }

        return uploadedDocuments;
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream downloadDocument(Long documentId) {
        TrackingDocument document = findDocumentById(documentId);

        if (!document.getIsActive()) {
            throw new BadRequestException("Document is not active and cannot be downloaded");
        }

        return downloadFile(trackingDocumentBucket, document.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentDownloadUrl(Long documentId, int expirationMinutes) {
        TrackingDocument document = findDocumentById(documentId);

        if (!document.getIsActive()) {
            throw new BadRequestException("Document is not active and cannot be downloaded");
        }

        return getDownloadUrl(trackingDocumentBucket, document.getFilePath(), expirationMinutes);
    }

    @Override
    public String getDocumentUploadUrl(Long trackingId, Long stepId, String fileName, String contentType, int expirationMinutes) {
        validateUploadUrlInputs(trackingId, stepId, fileName);

        // Verify tracking and step exist
        findTrackingById(trackingId);
        findStepById(stepId);

        String filePath = generateTrackingFilePath(trackingId, stepId, fileName);
        return getUploadUrl(trackingDocumentBucket, filePath, contentType, expirationMinutes);
    }


    @Override
    public boolean deleteDocument(Long documentId) {
        TrackingDocument document = findDocumentById(documentId);

        try {
            // Soft delete in database
            document.setIsActive(false);
            documentRepository.save(document);

            // Delete from S3
            boolean s3Deleted = deleteFile(trackingDocumentBucket, document.getFilePath());

            log.info("Document deleted: documentId={}, s3Deleted={}", documentId, s3Deleted);
            return s3Deleted;

        } catch (Exception e) {
            log.error("Failed to delete document: documentId={}, error={}", documentId, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<Long, Boolean> deleteDocuments(List<Long> documentIds) {
        Map<Long, Boolean> results = new HashMap<>();

        for (Long documentId : documentIds) {
            try {
                boolean deleted = deleteDocument(documentId);
                results.put(documentId, deleted);
            } catch (Exception e) {
                log.error("Failed to delete document in batch: documentId={}, error={}", documentId, e.getMessage());
                results.put(documentId, false);
            }
        }

        return results;
    }

    @Override
    public TrackingDocumentDto createDocumentVersion(Long originalDocumentId, MultipartFile newFile,
                                                     String description, Long uploadedBy) {
        TrackingDocument originalDocument = findDocumentById(originalDocumentId);
        TrackingStep step = originalDocument.getTrackingStep();
        User user = findUserById(uploadedBy);

        // Generate new file path
        String newFilePath = generateTrackingFilePath(
                step.getTracking().getId(),
                step.getId(),
                newFile.getOriginalFilename()
        );

        try {
            // Upload new version to S3
            Map<String, String> metadata = createFileMetadata(
                    step.getTracking(), step, user, originalDocument.getDocumentType()
            );
            String uploadedKey = uploadFile(newFile, trackingDocumentBucket, newFilePath, newFile.getContentType(), metadata);

            // Create new document version
            TrackingDocument newVersion = documentMapper.createNewVersion(
                    originalDocument, newFile, step, user, uploadedKey, description
            );

            TrackingDocument savedVersion = documentRepository.save(newVersion);
            log.info("Created new document version: originalId={}, newId={}, version={}",
                    originalDocumentId, savedVersion.getId(), savedVersion.getVersionNumber());

            return documentMapper.toDto(savedVersion);

        } catch (Exception e) {
            log.error("Failed to create document version: originalId={}, error={}", originalDocumentId, e.getMessage());
            throw new RuntimeException("Failed to create document version: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingDocumentDto> getDocumentVersions(String documentName, Long trackingId) {
        List<TrackingDocument> versions = documentRepository.findVersionsByDocumentNameAndStepId(documentName, trackingId);
        return documentMapper.toDtoList(versions);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingDocumentDto getLatestDocumentVersion(String documentName, Long trackingId) {
        return documentRepository.findLatestVersionByDocumentNameAndStepId(documentName, trackingId)
                .map(documentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Latest document version not found"));
    }

    @Override
    public int archiveOldVersions(String documentName, Long trackingId, int versionsToKeep) {
        List<TrackingDocument> versions = documentRepository.findVersionsByDocumentNameAndStepId(documentName, trackingId);

        if (versions.size() <= versionsToKeep) {
            return 0;
        }

        // Sort by version number descending and keep the latest versions
        versions.sort((d1, d2) -> d2.getVersionNumber().compareTo(d1.getVersionNumber()));
        List<TrackingDocument> versionsToArchive = versions.subList(versionsToKeep, versions.size());

        int archivedCount = 0;
        for (TrackingDocument document: versionsToArchive) {
            document.setIsActive(false);
            documentRepository.save(document);

            deleteFile(trackingDocumentBucket, document.getFilePath());
            archivedCount++;
        }
        log.info("Archived {} old document versions: documentName={}, trackingId={}",
                archivedCount, documentName, trackingId);
        return archivedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingDocumentDto> getDocumentsByTracking(Long trackingId) {
        List<TrackingDocument> documents = documentRepository.findByTrackingIdAndActiveTrue(trackingId);
        return documentMapper.toDtoList(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingDocumentDto> getDocumentsByStep(Long stepId) {
        List<TrackingDocument> documents = documentRepository.findByTrackingStepIdAndActiveTrue(stepId);
        return documentMapper.toDtoList(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingDocumentDto> getDocumentsByType(DocumentType documentType, Long trackingId) {
        List<TrackingDocument> documents;

        if (trackingId != null) {
            // Filter by tracking ID and document type
            documents = documentRepository.findByTrackingIdAndActiveTrue(trackingId).stream()
                    .filter(doc -> doc.getDocumentType() == documentType)
                    .collect(Collectors.toList());
        } else {
            documents = documentRepository.findByDocumentTypeAndActiveTrue(documentType);
        }

        return documentMapper.toDtoList(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingDocumentDto> searchDocuments(String searchTerm, Long trackingId) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return trackingId != null ? getDocumentsByTracking(trackingId) : Collections.emptyList();
        }

        List<TrackingDocument> allDocuments = trackingId != null
                ? documentRepository.findByTrackingIdAndActiveTrue(trackingId)
                : documentRepository.findAll().stream()
                .filter(TrackingDocument::getIsActive)
                .toList();

        String searchPattern = searchTerm.toLowerCase().trim();

        List<TrackingDocument> matchingDocuments = allDocuments.stream()
                .filter(doc -> matchesSearchTerm(doc, searchPattern))
                .collect(Collectors.toList());

        return documentMapper.toDtoList(matchingDocuments);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingDocumentDto getDocumentMetadata(Long documentId) {
        TrackingDocument document = findDocumentById(documentId);
        return documentMapper.toDto(document);
    }

    @Override
    public TrackingDocumentDto updateDocumentMetadata(Long documentId, String description, DocumentType documentType) {
        TrackingDocument document = findDocumentById(documentId);

        documentMapper.updateDocumentMetadata(document, description, documentType);
        TrackingDocument updatedDocument = documentRepository.save(document);

        return documentMapper.toDto(updatedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTrackingStorageStatistics(Long trackingId) {
        List<TrackingDocument> documents = trackingId != null
                ? documentRepository.findByTrackingIdAndActiveTrue(trackingId)
                : documentRepository.findAll().stream()
                .filter(TrackingDocument::getIsActive)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", documents.size());
        stats.put("totalSize", documents.stream().mapToLong(TrackingDocument::getFileSize).sum());
        stats.put("documentsByType", groupDocumentsByType(documents));
        stats.put("averageFileSize", documents.isEmpty() ? 0 :
                documents.stream().mapToLong(TrackingDocument::getFileSize).average().orElse(0));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<DocumentType, Long> getStorageUsageByType(Long trackingId) {
        List<TrackingDocument> documents = trackingId != null
                ? documentRepository.findByTrackingIdAndActiveTrue(trackingId)
                : documentRepository.findAll().stream()
                .filter(TrackingDocument::getIsActive)
                .toList();

        return documents.stream()
                .collect(Collectors.groupingBy(
                        TrackingDocument::getDocumentType,
                        Collectors.summingLong(TrackingDocument::getFileSize)
                ));
    }

    @Override
    public TrackingValidationResult validateTrackingFile(MultipartFile file, DocumentType documentType) {
        long maxSize = getMaxFileSize(documentType);
        List<String> allowedTypes = getAllowedFileTypes(documentType);
        List<String> allowedExtensions = getAllowedFileExtensions(documentType);

        S3ValidationResult baseValidation = validateFile(file, maxSize, allowedTypes, allowedExtensions);

        return new TrackingValidationResult(
                baseValidation.valid(),
                baseValidation.errorMessage(),
                baseValidation.warnings()
        );
    }

    @Override
    public String generateTrackingFilePath(Long trackingId, Long stepId, String originalFilename) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String cleanFilename = sanitizeFilename(originalFilename);

        return String.format("tracking/%d/steps/%d/%s_%s", trackingId, stepId, timeStamp, cleanFilename);
    }

    @Override
    public List<String> cleanupOrphanedTrackingFiles(boolean dryRun) {
        List<String> orphanedFiles = new ArrayList<>();

        try {
            // Get all files from s3
            List<String> s3Files = listFiles(trackingDocumentBucket, "tracking/", 1000);

            // Get all active file paths from database
            Set<String> dbFilePaths = documentRepository.findAll().stream()
                    .filter(TrackingDocument::getIsActive)
                    .map(TrackingDocument::getFilePath)
                    .collect(Collectors.toSet());

            // Find orphaned files
            for (String s3File : s3Files) {
                if (!dbFilePaths.contains(s3File)) {
                    orphanedFiles.add(s3File);

                    if (!dryRun) {
                        deleteFile(trackingDocumentBucket, s3File);
                    }
                }
            }

            log.info("Found {} orphaned tracking files, dryRun={}", orphanedFiles.size(), dryRun);
        } catch (Exception e) {
            log.error("Failed to cleanup orphaned files: {}", e.getMessage());
        }
        return orphanedFiles;
    }

    @Override
    public TrackingDocumentDto copyDocument(Long documentId, Long targetTrackingId, Long targetStepId, Long copiedBy) {
        TrackingDocument sourceDocument = findDocumentById(documentId);
        CurriculumTracking targetTracking = findTrackingById(targetTrackingId);
        TrackingStep targetStep = findStepById(targetStepId);
        User user = findUserById(copiedBy);

        validateStepBelongsToTracking(targetStep, targetTracking);

        try {
            // Generate new file Path
            String newFilePath = generateTrackingFilePath(targetTrackingId, targetStepId, sourceDocument.getOriginalFilename());

            //Copy file in s3
            boolean copied = copyFile(trackingDocumentBucket, sourceDocument.getFilePath(),
                    trackingDocumentBucket, newFilePath);

            if (!copied) {
                throw new RuntimeException("Failed to copy file in S3");
            }

            TrackingDocument newDocument = TrackingDocument.builder()
                    .trackingStep(targetStep)
                    .documentName(sourceDocument.getDocumentName())
                    .originalFilename(sourceDocument.getOriginalFilename())
                    .documentType(sourceDocument.getDocumentType())
                    .filePath(newFilePath)
                    .fileSize(sourceDocument.getFileSize())
                    .contentType(sourceDocument.getContentType())
                    .description("Copy of: " + sourceDocument.getDescription())
                    .uploadedBy(user)
                    .versionNumber(1)
                    .isActive(true)
                    .build();

            TrackingDocument savedDocument = documentRepository.save(newDocument);
            log.info("Copied document: sourceId={}, targetId={}, targetTrackingId={}",
                    documentId, savedDocument.getId(), targetTrackingId);
            return documentMapper.toDto(savedDocument);
        } catch (Exception e) {
            log.error("Failed to copy document: sourceId={}, targetTrackingId={}, error={}",
                    documentId, targetTrackingId, e.getMessage());
            throw new RuntimeException("Failed to copy document: " + e.getMessage(), e);
        }
    }

    @Override
    public TrackingDocumentDto moveDocument(Long documentId, Long targetTrackingId, Long targetStepId, Long movedBy) {
        TrackingDocumentDto copiedDocument = copyDocument(documentId, targetTrackingId, targetStepId, movedBy);

        // delete original document
        boolean deleted = deleteDocument(documentId);
        if (!deleted) {
            log.warn("failed");
        }
        return copiedDocument;
    }

    @Override
    public String getTrackingDocumentBucketName() {
        return trackingDocumentBucket;
    }

    @Override
    public long getMaxFileSize(DocumentType documentType) {
        return switch (documentType) {
            case CURRICULUM_PROPOSAL -> PROPOSAL_MAX_FILE_SIZE;
            case AUDIT_REPORT -> AUDIT_MAX_FILE_SIZE;
            default -> DEFAULT_MAX_FILE_SIZE;
        };
    }

    @Override
    public List<String> getAllowedFileTypes(DocumentType documentType) {
        List<String> allowedTypes = new ArrayList<>(DOCUMENT_CONTENT_TYPES);

        if (documentType == DocumentType.SUPPORTING_DOCUMENTS || documentType == DocumentType.OTHER) {
            allowedTypes.addAll(IMAGE_CONTENT_TYPES);
            allowedTypes.addAll(SPREADSHEET_CONTENT_TYPES);
        }

        return allowedTypes;
    }

    @Override
    public List<String> getAllowedFileExtensions(DocumentType documentType) {
        List<String> allowedExtensions = new ArrayList<>(DOCUMENT_EXTENSIONS);

        if (documentType == DocumentType.SUPPORTING_DOCUMENTS || documentType == DocumentType.OTHER) {
            allowedExtensions.addAll(IMAGE_EXTENSIONS);
            allowedExtensions.addAll(SPREADSHEET_EXTENSIONS);
        }

        return allowedExtensions;
    }

    private void validateTrackingDocumentInputs(MultipartFile file, Long trackingId, Long stepId, Long uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (trackingId == null) {
            throw new BadRequestException("Tracking ID is required");
        }
        if (stepId == null) {
            throw new BadRequestException("Step ID is required");
        }
        if (uploadedBy == null) {
            throw new BadRequestException("Uploaded by user ID is required");
        }
    }

    private void validateBatchTrackingUpload(List<MultipartFile> files, List<String> descriptions) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Files list cannot be empty");
        }
        if (descriptions != null && descriptions.size() != files.size()) {
            throw new BadRequestException("Descriptions list size must match files list size");
        }
    }

    private void validateUploadUrlInputs(Long trackingId, Long stepId, String fileName) {
        if (trackingId == null) {
            throw new BadRequestException("Tracking ID is required");
        }
        if (stepId == null) {
            throw new BadRequestException("Step ID is required");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BadRequestException("File name is required");
        }
    }

    private void validateStepBelongsToTracking(TrackingStep step, CurriculumTracking tracking) {
        if (!step.getTracking().getId().equals(tracking.getId())) {
            throw new BadRequestException("Step does not belong to the specified tracking");
        }
    }

    private CurriculumTracking findTrackingById(Long trackingId) {
        return trackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found: " + trackingId));
    }

    private TrackingStep findStepById(Long stepId) {
        return stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found: " + stepId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private TrackingDocument findDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
    }

    private Integer getNextVersionNumber(String filename, Long stepId) {
        List<TrackingDocument> existingVersions = documentRepository.findVersionsByDocumentNameAndStepId(filename, stepId);
        return existingVersions.stream()
                .mapToInt(TrackingDocument::getVersionNumber)
                .max()
                .orElse(0) + 1;
    }

    private Map<String, String> createFileMetadata(CurriculumTracking tracking, TrackingStep step, User user, DocumentType documentType) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("trackingId", tracking.getId().toString());
        metadata.put("stepId", step.getId().toString());
        metadata.put("documentType", documentType.name());
        metadata.put("uploadedBy", user.getId().toString());
        metadata.put("uploadedByName", user.getFirstName() + " " + user.getLastName());
        metadata.put("uploadedAt", LocalDateTime.now().toString());
        return metadata;
    }

    private void cleanupBatchUploadFailure(List<TrackingDocumentDto> uploadedDocuments) {
        if (!uploadedDocuments.isEmpty()) {
            log.info("Cleaning up {} documents due to batch upload failure", uploadedDocuments.size());
            List<Long> documentIds = uploadedDocuments.stream()
                    .map(TrackingDocumentDto::getId)
                    .collect(Collectors.toList());
            deleteDocuments(documentIds);
        }
    }

    private boolean matchesSearchTerm(TrackingDocument document, String searchPattern) {
        return (document.getDocumentName() != null && document.getDocumentName().toLowerCase().contains(searchPattern)) ||
                (document.getOriginalFilename() != null && document.getOriginalFilename().toLowerCase().contains(searchPattern)) ||
                (document.getDescription() != null && document.getDescription().toLowerCase().contains(searchPattern));
    }

    private Map<DocumentType, Integer> groupDocumentsByType(List<TrackingDocument> documents) {
        return documents.stream()
                .collect(Collectors.groupingBy(
                        TrackingDocument::getDocumentType,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed_file";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
