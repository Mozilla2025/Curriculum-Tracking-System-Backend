package com.mozilla.curriculum_tracking_system.controller.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.DocumentUploadResponse;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.curriculumdocuments.ICurriculumTrackingDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/tracking/documents")
@RequiredArgsConstructor
@Slf4j
public class CurriculumTrackingDocumentController {

    private final ICurriculumTrackingDocumentService documentService;

    /**
     * Upload single document to tracking history
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadDocument(
            @RequestParam("trackingHistoryId") Long trackingHistoryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /tracking/documents/upload - trackingHistoryId: {}, filename: {}",
                trackingHistoryId, file.getOriginalFilename());

        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .trackingHistoryId(trackingHistoryId)
                .file(file)
                .documentName(documentName)
                .description(description)
                .build();

        String token = extractToken(authorizationHeader);
        DocumentUploadResponse response = documentService.uploadDocument(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Document uploaded successfully",
                response
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Upload multiple documents to tracking history
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadMultipleDocuments(
            @RequestParam("trackingHistoryId") Long trackingHistoryId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /tracking/documents/upload-multiple - trackingHistoryId: {}, fileCount: {}",
                trackingHistoryId, files.size());

        String token = extractToken(authorizationHeader);
        List<DocumentUploadResponse> responses = documentService.uploadMultipleDocuments(
                trackingHistoryId, files, description, token);

        ApiResponse apiResponse = new ApiResponse(
                "Documents upload completed",
                responses
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get documents by tracking history ID
     */
    @GetMapping("/tracking-history/{trackingHistoryId}")
    public ResponseEntity<ApiResponse> getDocumentsByTrackingHistory(@PathVariable Long trackingHistoryId) {

        List<CurriculumTrackingDocumentDto> documents = documentService
                .getDocumentsByTrackingHistoryId(trackingHistoryId);

        ApiResponse apiResponse = new ApiResponse(
                "Documents retrieved successfully",
                documents
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all documents for a curriculum tracking
     */
    @GetMapping("/curriculum-tracking/{curriculumTrackingId}")
    public ResponseEntity<ApiResponse> getDocumentsByCurriculumTracking(@PathVariable Long curriculumTrackingId) {

        List<CurriculumTrackingDocumentDto> documents = documentService
                .getDocumentsByCurriculumTrackingId(curriculumTrackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Documents retrieved successfully",
                documents
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse> getDocumentById(@PathVariable Long documentId) {
        log.debug("GET /tracking/documents/{}", documentId);

        CurriculumTrackingDocumentDto document = documentService.getDocumentById(documentId);

        ApiResponse apiResponse = new ApiResponse(
                "Document retrieved successfully",
                document
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get document download URL
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<ApiResponse> getDocumentDownloadUrl(
            @PathVariable Long documentId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("GET /tracking/documents/{}/download", documentId);

        String token = extractToken(authorizationHeader);
        String downloadUrl = documentService.getDocumentDownloadUrl(documentId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Download URL generated successfully",
                downloadUrl
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update document description
     */
    @PutMapping("/{documentId}/description")
    public ResponseEntity<ApiResponse> updateDocumentDescription(
            @PathVariable Long documentId,
            @RequestBody String description,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /tracking/documents/{}/description", documentId);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDocumentDto updatedDocument = documentService
                .updateDocumentDescription(documentId, description, token);

        ApiResponse apiResponse = new ApiResponse(
                "Document description updated successfully",
                updatedDocument
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse> deleteDocument(
            @PathVariable Long documentId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("DELETE /tracking/documents/{}", documentId);

        String token = extractToken(authorizationHeader);
        documentService.deleteDocument(documentId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Document deleted successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get document versions by name and tracking history
     */
    @GetMapping("/versions")
    public ResponseEntity<ApiResponse> getDocumentVersions(
            @RequestParam String documentName,
            @RequestParam Long trackingHistoryId) {

        log.debug("GET /tracking/documents/versions - name: {}, trackingHistoryId: {}",
                documentName, trackingHistoryId);

        List<CurriculumTrackingDocumentDto> versions = documentService
                .getDocumentVersions(documentName, trackingHistoryId);

        ApiResponse apiResponse = new ApiResponse(
                "Document versions retrieved successfully",
                versions
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get latest document version
     */
    @GetMapping("/latest-version")
    public ResponseEntity<ApiResponse> getLatestDocumentVersion(
            @RequestParam String documentName,
            @RequestParam Long trackingHistoryId) {

        log.debug("GET /tracking/documents/latest-version - name: {}, trackingHistoryId: {}",
                documentName, trackingHistoryId);

        CurriculumTrackingDocumentDto latestVersion = documentService
                .getLatestDocumentVersion(documentName, trackingHistoryId);

        ApiResponse apiResponse = new ApiResponse(
                "Latest document version retrieved successfully",
                latestVersion
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get storage statistics
     */
    @GetMapping("/storage/stats")
    public ResponseEntity<ApiResponse> getStorageStatistics() {
        log.debug("GET /tracking/documents/storage/stats");

        Object stats = documentService.getStorageStatistics();

        ApiResponse apiResponse = new ApiResponse(
                "Storage statistics retrieved successfully",
                stats
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{documentId}/refresh-url")
    public ResponseEntity<ApiResponse> refreshDocumentUrl(
            @PathVariable Long documentId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /tracking/documents/{}/refresh-url", documentId);

        String token = extractToken(authorizationHeader);
        String refreshedUrl = documentService.refreshDocumentUrl(documentId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Document URL refreshed successfully",
                Map.of("documentId", documentId, "refreshedUrl", refreshedUrl)
        );

        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/refresh-urls")
    public ResponseEntity<ApiResponse> refreshMultipleDocumentUrls(
            @RequestBody List<Long> documentIds,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /tracking/documents/refresh-urls - documentIds: {}", documentIds);

        String token = extractToken(authorizationHeader);
        List<String> refreshedUrls = documentService.refreshMultipleDocumentUrls(documentIds, token);

        Map<String, Object> response = new HashMap<>();
        for (int i = 0; i < documentIds.size(); i++) {
            response.put(documentIds.get(i).toString(), refreshedUrls.get(i));
        }

        ApiResponse apiResponse = new ApiResponse(
                "Document URLs refreshed successfully",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}