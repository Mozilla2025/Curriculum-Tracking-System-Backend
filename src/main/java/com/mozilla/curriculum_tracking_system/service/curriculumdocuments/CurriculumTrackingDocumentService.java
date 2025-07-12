package com.mozilla.curriculum_tracking_system.service.curriculumdocuments;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadResponse;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.exception.UnauthorizedException;
import com.mozilla.curriculum_tracking_system.mapper.curriculum.CurriculumTrackingDocumentMapper;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingDocument;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingDocumentRepository;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingHistoryRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.service.firebase.IFirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CurriculumTrackingDocumentService implements ICurriculumTrackingDocumentService {

    private final CurriculumTrackingDocumentRepository documentRepository;
    private final CurriculumTrackingHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final CurriculumTrackingDocumentMapper documentMapper;
    private final IFirebaseStorageService firebaseStorageService;
    private final IAuthenticationService authenticationService;

    @Override
    public DocumentUploadResponse uploadDocument(DocumentUploadRequest request, String authToken) {
        log.info("Uploading document for tracking history ID: {}", request.getTrackingHistoryId());

        validateUploadRequest(request, authToken);

        CurriculumTrackingHistory trackingHistory = findTrackingHistoryById(request.getTrackingHistoryId());
        Long userId = authenticationService.getUserIdFromToken(authToken);
        User uploaderUser = findUserById(userId);

        try {
            String originalFilename = request.getFile().getOriginalFilename();
            Long curriculumId = trackingHistory.getCurriculumTracking().getCurriculum().getId();
            String firebasePath = firebaseStorageService.generateCurriculumTrackingPath(
                    curriculumId, request.getTrackingHistoryId(), originalFilename);

            String firebaseUrl = firebaseStorageService.uploadFile(request.getFile(), firebasePath);

            int version = getNextDocumentVersion(request.getDocumentName(), request.getTrackingHistoryId());

            // Create document record
            CurriculumTrackingDocument document = CurriculumTrackingDocument.builder()
                    .trackingHistory(trackingHistory)
                    .documentName(StringUtils.hasText(request.getDocumentName()) ?
                            request.getDocumentName() : extractDocumentName(originalFilename))
                    .originalFilename(originalFilename)
                    .firebaseUrl(firebaseUrl)
                    .firebasePath(firebasePath)
                    .fileSize(request.getFile().getSize())
                    .contentType(request.getFile().getContentType())
                    .fileExtension(getFileExtension(originalFilename))
                    .description(request.getDescription())
                    .uploadedBy(userId)
                    .uploadedByEmail(uploaderUser.getEmail())
                    .documentVersion(version)
                    .build();

            CurriculumTrackingDocument savedDocument = documentRepository.save(document);

            log.info("Successfully uploaded document with ID: {} for tracking history: {}",
                    savedDocument.getId(), request.getTrackingHistoryId());

            return documentMapper.buildUploadResponse(savedDocument, "Document uploaded successfully");

        } catch (Exception e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    public List<DocumentUploadResponse> uploadMultipleDocuments(Long trackingHistoryId,
                                                                List<MultipartFile> files,
                                                                String description,
                                                                String authToken) {
        log.info("Uploading {} documents for tracking history ID: {}", files.size(), trackingHistoryId);

        validateMultipleUploadRequest(trackingHistoryId, files, authToken);

        List<DocumentUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                DocumentUploadRequest request = DocumentUploadRequest.builder()
                        .trackingHistoryId(trackingHistoryId)
                        .file(file)
                        .description(description)
                        .build();

                DocumentUploadResponse response = uploadDocument(request, authToken);
                responses.add(response);

            } catch (Exception e) {
                log.warn("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());

                DocumentUploadResponse errorResponse = DocumentUploadResponse.builder()
                        .originalFilename(file.getOriginalFilename())
                        .message("Failed to upload: " + e.getMessage())
                        .build();
                responses.add(errorResponse);
            }
        }

        log.info("Completed uploading {} documents with {} successful uploads",
                files.size(), responses.stream().mapToLong(r -> r.getDocumentId() != null ? 1 : 0).sum());

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingDocumentDto getDocumentById(Long documentId) {
        log.debug("Fetching document by ID: {}", documentId);

        CurriculumTrackingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        return toDocumentDtoWithFreshUrl(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDocumentDto> getDocumentsByTrackingHistoryId(Long trackingHistoryId) {
        log.debug("Fetching documents for tracking history ID: {}", trackingHistoryId);

        List<CurriculumTrackingDocument> documents = documentRepository
                .findByTrackingHistoryIdAndIsActiveOrderByUploadedAtDesc(trackingHistoryId, true);

        return documents.stream()
                .map(this::toDocumentDtoWithFreshUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDocumentDto> getDocumentsByCurriculumTrackingId(Long curriculumTrackingId) {
        log.debug("Fetching documents for curriculum tracking ID: {}", curriculumTrackingId);

        List<CurriculumTrackingDocument> documents = documentRepository
                .findByCurriculumTrackingId(curriculumTrackingId);

        return documents.stream()
                .map(this::toDocumentDtoWithFreshUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentDownloadUrl(Long documentId, String authToken) {
        log.debug("Getting download URL for document ID: {}", documentId);

        validateDocumentAccess(documentId, authToken);

        CurriculumTrackingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        if (!document.isActive()) {
            throw new BadRequestException("Document is no longer active");
        }

        try {
            return firebaseStorageService.refreshSignedUrl(document.getFirebasePath());
        } catch (Exception e) {
            log.error("Failed to get download URL for document {}: {}", documentId, e.getMessage());
            throw new BadRequestException("Failed to generate download URL: " + e.getMessage());
        }
    }

    @Override
    public String refreshDocumentUrl(Long documentId, String authToken) {
        log.debug("Refreshing URL for document ID: {}", documentId);

        validateDocumentAccess(documentId, authToken);

        CurriculumTrackingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        if (!document.isActive()) {
            throw new BadRequestException("Document is no longer active");
        }

        try {
            String freshUrl = firebaseStorageService.refreshSignedUrl(document.getFirebasePath());
            document.setFirebaseUrl(freshUrl);
            documentRepository.save(document);

            log.info("Refreshed URL for document ID: {}", documentId);
            return freshUrl;
        } catch (Exception e) {
            log.error("Failed to refresh URL for document {}: {}", documentId, e.getMessage());
            throw new BadRequestException("Failed to refresh document URL: " + e.getMessage());
        }
    }

    @Override
    public List<String> refreshMultipleDocumentUrls(List<Long> documentIds, String authToken) {
        log.debug("Refreshing URLs for {} documents", documentIds.size());

        List<String> refreshedUrls = new ArrayList<>();

        for (Long documentId : documentIds) {
            try {
                String refreshedUrl = refreshDocumentUrl(documentId, authToken);
                refreshedUrls.add(refreshedUrl);
            } catch (Exception e) {
                log.warn("Failed to refresh URL for document {}: {}", documentId, e.getMessage());
                refreshedUrls.add(null);
            }
        }

        return refreshedUrls;
    }

    @Override
    public void deleteDocument(Long documentId, String authToken) {
        log.info("Deleting document ID: {}", documentId);

        validateDocumentAccess(documentId, authToken);

        CurriculumTrackingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        document.deactivate();
        documentRepository.save(document);

        log.info("Successfully deleted document ID: {}", documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDocumentDto> searchDocuments(String searchTerm, Pageable pageable) {
        log.debug("Searching documents with term: {}", searchTerm);

        Page<CurriculumTrackingDocument> documentsPage = documentRepository
                .searchDocuments(searchTerm, pageable);

        return documentsPage.getContent().stream()
                .map(this::toDocumentDtoWithFreshUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDocumentDto> getDocumentsByContentType(String contentType) {
        log.debug("Fetching documents by content type: {}", contentType);

        List<CurriculumTrackingDocument> documents = documentRepository
                .findByContentTypeAndIsActiveOrderByUploadedAtDesc(contentType, true);

        return documents.stream()
                .map(this::toDocumentDtoWithFreshUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDocumentDto> getDocumentVersions(String documentName, Long trackingHistoryId) {
        log.debug("Fetching document versions for: {} in tracking history: {}", documentName, trackingHistoryId);

        List<CurriculumTrackingDocument> versions = documentRepository
                .findVersionsByDocumentName(documentName, trackingHistoryId);

        return versions.stream()
                .map(this::toDocumentDtoWithFreshUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingDocumentDto getLatestDocumentVersion(String documentName, Long trackingHistoryId) {
        log.debug("Fetching latest version of document: {} in tracking history: {}", documentName, trackingHistoryId);

        CurriculumTrackingDocument latestVersion = documentRepository
                .findLatestVersionByDocumentName(documentName, trackingHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No document found with name: " + documentName + " in tracking history: " + trackingHistoryId));

        return toDocumentDtoWithFreshUrl(latestVersion);
    }

    @Override
    public CurriculumTrackingDocumentDto updateDocumentDescription(Long documentId, String description, String authToken) {
        log.info("Updating description for document ID: {}", documentId);

        validateDocumentAccess(documentId, authToken);

        CurriculumTrackingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        document.setDescription(description);
        CurriculumTrackingDocument updatedDocument = documentRepository.save(document);

        log.info("Successfully updated description for document ID: {}", documentId);
        return toDocumentDtoWithFreshUrl(updatedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getStorageStatistics() {
        log.debug("Calculating storage statistics");

        Long totalStorageUsed = documentRepository.getTotalStorageUsed();
        List<Object[]> statsByContentType = documentRepository.getStorageStatsByContentType();

        return Map.of(
                "totalStorageUsed", totalStorageUsed != null ? totalStorageUsed : 0L,
                "totalStorageUsedFormatted", formatFileSize(totalStorageUsed != null ? totalStorageUsed : 0L),
                "totalDocuments", documentRepository.count(),
                "activeDocuments", documentRepository.findAll().stream()
                        .mapToLong(doc -> doc.isActive() ? 1 : 0).sum(),
                "statsByContentType", statsByContentType.stream()
                        .collect(Collectors.toMap(
                                row -> (String) row[0],
                                row -> Map.of(
                                        "count", row[1],
                                        "totalSize", row[2],
                                        "totalSizeFormatted", formatFileSize((Long) row[2])
                                )
                        )),
                "signedUrlDurationHours", firebaseStorageService.getSignedUrlDurationHours()
        );
    }

    @Override
    public void validateFileUpload(MultipartFile file) {
        firebaseStorageService.validateFile(file);
    }

    @Override
    public String generateUniqueFilename(String originalFilename, Long trackingHistoryId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        return String.format("%d_%s_%s.%s", trackingHistoryId, timestamp, uuid, extension);
    }

    /**
     * Convert document to DTO with fresh signed URL
     */
    private CurriculumTrackingDocumentDto toDocumentDtoWithFreshUrl(CurriculumTrackingDocument document) {
        CurriculumTrackingDocumentDto dto = documentMapper.toDto(document);

        if (dto != null && document.isActive()) {
            try {
                String freshUrl = firebaseStorageService.refreshSignedUrl(document.getFirebasePath());
                dto.setFirebaseUrl(freshUrl);
            } catch (Exception e) {
                log.warn("Failed to generate fresh URL for document {}: {}", document.getId(), e.getMessage());
            }
        }

        return dto;
    }

    private void validateUploadRequest(DocumentUploadRequest request, String authToken) {
        if (request == null) {
            throw new BadRequestException("Upload request cannot be null");
        }

        if (request.getTrackingHistoryId() == null) {
            throw new BadRequestException("Tracking history ID is required");
        }

        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BadRequestException("File is required and cannot be empty");
        }

        validateUserAccess(authToken);
        firebaseStorageService.validateFile(request.getFile());
    }

    private void validateMultipleUploadRequest(Long trackingHistoryId, List<MultipartFile> files, String authToken) {
        if (trackingHistoryId == null) {
            throw new BadRequestException("Tracking history ID is required");
        }

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one file is required");
        }

        if (files.size() > 10) {
            throw new BadRequestException("Cannot upload more than 10 files at once");
        }

        validateUserAccess(authToken);

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                firebaseStorageService.validateFile(file);
            }
        }
    }

    private void validateUserAccess(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            throw new UnauthorizedException("Authorization token is required");
        }

        if (!authenticationService.validateToken(authToken)) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    private void validateDocumentAccess(Long documentId, String authToken) {
        validateUserAccess(authToken);

        Long userId = authenticationService.getUserIdFromToken(authToken);
        List<String> userRoles = authenticationService.getRolesFromToken(authToken);

        if (!userRoles.contains("QA")) {
            CurriculumTrackingDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

            Long currentAssignee = document.getTrackingHistory().getCurriculumTracking().getCurrentAssignee();

            if (!userId.equals(document.getUploadedBy()) && !userId.equals(currentAssignee)) {
                throw new UnauthorizedException("Access denied to this document");
            }
        }
    }

    private CurriculumTrackingHistory findTrackingHistoryById(Long trackingHistoryId) {
        return historyRepository.findById(trackingHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking history not found with ID: " + trackingHistoryId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private int getNextDocumentVersion(String documentName, Long trackingHistoryId) {
        if (!StringUtils.hasText(documentName)) {
            return 1;
        }

        List<CurriculumTrackingDocument> existingVersions = documentRepository
                .findVersionsByDocumentName(documentName, trackingHistoryId);

        return existingVersions.stream()
                .mapToInt(CurriculumTrackingDocument::getDocumentVersion)
                .max()
                .orElse(0) + 1;
    }

    private String extractDocumentName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "Document_" + System.currentTimeMillis();
        }

        int lastDot = originalFilename.lastIndexOf('.');
        return lastDot > 0 ? originalFilename.substring(0, lastDot) : originalFilename;
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}