package com.mozilla.curriculum_tracking_system.controller.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.service.tracking.ITrackingDocumentStorageService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing tracking document operations
 * Handles document upload, download, and metadata management for curriculum tracking
 */
@RestController
@RequestMapping("${api.prefix}/tracking/documents")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TrackingDocumentController {

    private final ITrackingDocumentStorageService documentStorageService;
    private final IAuthenticationService authenticationService;

    /**
     * Upload a single document to a tracking step
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('QA', 'DEAN', 'HOD')")
    public ResponseEntity<ApiResponse> uploadDocument(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("trackingId") @NotNull Long trackingId,
            @RequestParam("stepId") @NotNull Long stepId,
            @RequestParam(value = "documentType", defaultValue = "OTHER") DocumentType documentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Uploading document for tracking: {}, step: {}, type: {}",
                trackingId, stepId, documentType);

        String token = extractToken(authorizationHeader);
        Long uploadedBy = authenticationService.getUserIdFromToken(token);

        TrackingDocumentDto uploadedDocument = documentStorageService.uploadDocument(
                file, trackingId, stepId, documentType, description, uploadedBy);

        ApiResponse apiResponse = new ApiResponse(
                "Document uploaded successfully",
                uploadedDocument
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Upload multiple documents to a tracking step
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('QA', 'DEAN', 'HOD')")
    public ResponseEntity<ApiResponse> uploadDocuments(
            @RequestParam("files") @NotNull List<MultipartFile> files,
            @RequestParam("trackingId") @NotNull Long trackingId,
            @RequestParam("stepId") @NotNull Long stepId,
            @RequestParam(value = "documentType", defaultValue = "OTHER") DocumentType documentType,
            @RequestParam(value = "descriptions", required = false) List<String> descriptions,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Uploading {} documents for tracking: {}, step: {}",
                files.size(), trackingId, stepId);

        String token = extractToken(authorizationHeader);
        Long uploadedBy = authenticationService.getUserIdFromToken(token);

        List<TrackingDocumentDto> uploadedDocuments = documentStorageService.uploadDocuments(
                files, trackingId, stepId, documentType, descriptions, uploadedBy);

        ApiResponse apiResponse = new ApiResponse(
                "Documents uploaded successfully",
                uploadedDocuments
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Download a document by its ID
     */
    @GetMapping("/download/{documentId}")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long documentId) {
        log.debug("Downloading document with ID: {}", documentId);

        // Get document metadata first
        TrackingDocumentDto documentMetadata = documentStorageService.getDocumentMetadata(documentId);

        // Get document stream
        InputStream documentStream = documentStorageService.downloadDocument(documentId);
        InputStreamResource resource = new InputStreamResource(documentStream);

        // Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + documentMetadata.getOriginalFilename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, documentMetadata.getContentType());
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(documentMetadata.getFileSize()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * Get a pre-signed URL for document download
     */
    @GetMapping("/download-url/{documentId}")
    public ResponseEntity<ApiResponse> getDocumentDownloadUrl(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "60") int expirationMinutes) {

        log.debug("Generating download URL for document: {}, expires in: {} minutes",
                documentId, expirationMinutes);

        String downloadUrl = documentStorageService.getDocumentDownloadUrl(documentId, expirationMinutes);

        ApiResponse apiResponse = new ApiResponse(
                "Download URL generated successfully",
                Map.of("downloadUrl", downloadUrl, "expiresInMinutes", expirationMinutes)
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get document metadata by ID
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse> getDocumentMetadata(@PathVariable Long documentId) {
        log.debug("Fetching metadata for document: {}", documentId);

        TrackingDocumentDto documentMetadata = documentStorageService.getDocumentMetadata(documentId);

        ApiResponse apiResponse = new ApiResponse(
                "Document metadata retrieved successfully",
                documentMetadata
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all documents for a specific tracking
     */
    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<ApiResponse> getDocumentsByTracking(@PathVariable Long trackingId) {
        log.debug("Fetching documents for tracking: {}", trackingId);

        List<TrackingDocumentDto> documents = documentStorageService.getDocumentsByTracking(trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking documents retrieved successfully",
                documents
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all documents for a specific tracking step
     */
    @GetMapping("/step/{stepId}")
    public ResponseEntity<ApiResponse> getDocumentsByStep(@PathVariable Long stepId) {
        log.debug("Fetching documents for step: {}", stepId);

        List<TrackingDocumentDto> documents = documentStorageService.getDocumentsByStep(stepId);

        ApiResponse apiResponse = new ApiResponse(
                "Step documents retrieved successfully",
                documents
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get documents by type for a specific tracking
     */
    @GetMapping("/tracking/{trackingId}/type/{documentType}")
    public ResponseEntity<ApiResponse> getDocumentsByType(
            @PathVariable Long trackingId,
            @PathVariable DocumentType documentType) {

        log.debug("Fetching documents of type: {} for tracking: {}", documentType, trackingId);

        List<TrackingDocumentDto> documents = documentStorageService.getDocumentsByType(documentType, trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Documents by type retrieved successfully",
                documents
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Search documents by name or description
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchDocuments(
            @RequestParam String searchTerm,
            @RequestParam(required = false) Long trackingId) {

        log.debug("Searching documents with term: '{}' in tracking: {}", searchTerm, trackingId);

        List<TrackingDocumentDto> documents = documentStorageService.searchDocuments(searchTerm, trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Document search completed successfully",
                documents
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update document metadata (description and type)
     */
    @PutMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('QA', 'DEAN', 'HOD')")
    public ResponseEntity<ApiResponse> updateDocumentMetadata(
            @PathVariable Long documentId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) DocumentType documentType) {

        log.debug("Updating metadata for document: {}", documentId);

        TrackingDocumentDto updatedDocument = documentStorageService.updateDocumentMetadata(
                documentId, description, documentType);

        ApiResponse apiResponse = new ApiResponse(
                "Document metadata updated successfully",
                updatedDocument
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete a document
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> deleteDocument(@PathVariable Long documentId) {
        log.info("Deleting document: {}", documentId);

        boolean deleted = documentStorageService.deleteDocument(documentId);

        ApiResponse apiResponse = new ApiResponse(
                deleted ? "Document deleted successfully" : "Failed to delete document",
                Map.of("deleted", deleted)
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Create a new version of an existing document
     */
    @PostMapping(value = "/{documentId}/version", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('QA', 'DEAN', 'HOD')")
    public ResponseEntity<ApiResponse> createDocumentVersion(
            @PathVariable Long documentId,
            @RequestParam("file") @NotNull MultipartFile newFile,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Creating new version for document: {}", documentId);

        String token = extractToken(authorizationHeader);
        Long uploadedBy = authenticationService.getUserIdFromToken(token);

        TrackingDocumentDto newVersion = documentStorageService.createDocumentVersion(
                documentId, newFile, description, uploadedBy);

        ApiResponse apiResponse = new ApiResponse(
                "Document version created successfully",
                newVersion
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get all versions of a document
     */
    @GetMapping("/versions")
    public ResponseEntity<ApiResponse> getDocumentVersions(
            @RequestParam String documentName,
            @RequestParam Long trackingId) {

        log.debug("Fetching versions for document: {} in tracking: {}", documentName, trackingId);

        List<TrackingDocumentDto> versions = documentStorageService.getDocumentVersions(documentName, trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Document versions retrieved successfully",
                versions
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get document storage statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> getStorageStatistics(
            @RequestParam(required = false) Long trackingId) {

        log.debug("Fetching storage statistics for tracking: {}", trackingId);

        Map<String, Object> statistics = documentStorageService.getTrackingStorageStatistics(trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Storage statistics retrieved successfully",
                statistics
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Copy document to another tracking
     */
    @PostMapping("/{documentId}/copy")
    @PreAuthorize("hasAnyRole('QA', 'DEAN')")
    public ResponseEntity<ApiResponse> copyDocument(
            @PathVariable Long documentId,
            @RequestParam Long targetTrackingId,
            @RequestParam Long targetStepId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Copying document: {} to tracking: {}, step: {}",
                documentId, targetTrackingId, targetStepId);

        String token = extractToken(authorizationHeader);
        Long copiedBy = authenticationService.getUserIdFromToken(token);

        TrackingDocumentDto copiedDocument = documentStorageService.copyDocument(
                documentId, targetTrackingId, targetStepId, copiedBy);

        ApiResponse apiResponse = new ApiResponse(
                "Document copied successfully",
                copiedDocument
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get pre-signed URL for document upload
     */
    @GetMapping("/upload-url")
    @PreAuthorize("hasAnyRole('QA', 'DEAN', 'HOD')")
    public ResponseEntity<ApiResponse> getDocumentUploadUrl(
            @RequestParam Long trackingId,
            @RequestParam Long stepId,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam(defaultValue = "60") int expirationMinutes) {

        log.debug("Generating upload URL for tracking: {}, step: {}, file: {}",
                trackingId, stepId, fileName);

        String uploadUrl = documentStorageService.getDocumentUploadUrl(
                trackingId, stepId, fileName, contentType, expirationMinutes);

        ApiResponse apiResponse = new ApiResponse(
                "Upload URL generated successfully",
                Map.of(
                        "uploadUrl", uploadUrl,
                        "expiresInMinutes", expirationMinutes,
                        "fileName", fileName
                )
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new RuntimeException("No valid token found in request");
    }
}