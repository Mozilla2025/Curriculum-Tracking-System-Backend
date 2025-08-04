package com.mozilla.curriculum_tracking_system.documentation;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingDocumentDto;
import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Documentation interface for TrackingDocumentController
 * This interface contains all Swagger annotations for the document management endpoints
 * to keep the main controller clean and focused on business logic.
 */
@Tag(
        name = "Tracking Documents",
        description = "APIs for managing documents in curriculum tracking process - upload, download, versioning, and metadata management"
)
public interface TrackingDocumentControllerDocs {

    @Operation(
            summary = "Upload single document",
            description = """
            Uploads a single document to a tracking step. Supported document types:
            - **CURRICULUM_PROPOSAL**: Main curriculum proposal documents
            - **SUPPORTING_DOCUMENTS**: Additional supporting materials
            - **REVISION_DOCUMENTS**: Documents for revisions and updates
            - **APPROVAL_CERTIFICATE**: Official approval certificates
            - **AUDIT_REPORT**: Audit and review reports
            - **OTHER**: Miscellaneous documents
            
            **File Restrictions**:
            - Max size: 50MB (100MB for proposals, 200MB for audit reports)
            - Allowed formats: PDF, DOC, DOCX, TXT, RTF, JPG, PNG, XLS, XLSX
            
            **Required Permissions**: QA, DEAN, or HOD roles
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Document uploaded successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "Upload Success",
                            value = """
                {
                  "message": "Document uploaded successfully",
                  "data": {
                    "id": 1,
                    "documentName": "curriculum_proposal_2025",
                    "originalFilename": "Advanced_CS_Curriculum_Proposal.pdf",
                    "documentType": "CURRICULUM_PROPOSAL",
                    "fileSize": 2548736,
                    "formattedFileSize": "2.4 MB",
                    "contentType": "application/pdf",
                    "uploadedByName": "John Doe",
                    "versionNumber": 1,
                    "uploadedAt": "2025-08-04T10:30:00",
                    "isActive": true
                  }
                }
                """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file or request parameters",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "File Validation Error",
                            value = """
                {
                  "message": "File size 52428800 bytes exceeds maximum allowed size 50485760 bytes",
                  "data": null
                }
                """
                    )
            )
    )
    ResponseEntity<ApiResponse> uploadDocument(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("trackingId") @NotNull Long trackingId,
            @RequestParam("stepId") @NotNull Long stepId,
            @RequestParam(value = "documentType", defaultValue = "OTHER") DocumentType documentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Upload multiple documents",
            description = """
            Uploads multiple documents to a tracking step in a single batch operation.
            All files must meet the same validation criteria as single uploads.
            If any file fails, the entire batch is rolled back.
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "All documents uploaded successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiResponse.class)
            )
    )
    ResponseEntity<ApiResponse> uploadDocuments(
            @RequestParam("files") @NotNull List<MultipartFile> files,
            @RequestParam("trackingId") @NotNull Long trackingId,
            @RequestParam("stepId") @NotNull Long stepId,
            @RequestParam(value = "documentType", defaultValue = "OTHER") DocumentType documentType,
            @RequestParam(value = "descriptions", required = false) List<String> descriptions,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Download document",
            description = "Downloads a document by its ID. Returns the file content as a stream with appropriate headers for browser download."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Document downloaded successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Document not found"
    )
    ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long documentId);

    @Operation(
            summary = "Get document download URL",
            description = """
            Generates a pre-signed URL for downloading a document. 
            The URL expires after the specified time and provides secure, temporary access to the file.
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Download URL generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "Download URL Response",
                            value = """
                {
                  "message": "Download URL generated successfully",
                  "data": {
                    "downloadUrl": "https://s3.amazonaws.com/bucket/file.pdf?X-Amz-Expires=3600...",
                    "expiresInMinutes": 60
                  }
                }
                """
                    )
            )
    )
    ResponseEntity<ApiResponse> getDocumentDownloadUrl(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "60") int expirationMinutes
    );

    @Operation(
            summary = "Get document metadata",
            description = "Retrieves document information without downloading the actual file content"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Document metadata retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TrackingDocumentDto.class)
            )
    )
    ResponseEntity<ApiResponse> getDocumentMetadata(@PathVariable Long documentId);

    @Operation(
            summary = "Get documents by tracking",
            description = "Retrieves all documents associated with a specific tracking process"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Documents retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "array", implementation = TrackingDocumentDto.class)
            )
    )
    ResponseEntity<ApiResponse> getDocumentsByTracking(@PathVariable Long trackingId);

    @Operation(
            summary = "Get documents by step",
            description = "Retrieves all documents associated with a specific tracking step"
    )
    ResponseEntity<ApiResponse> getDocumentsByStep(@PathVariable Long stepId);

    ResponseEntity<ApiResponse> getDocumentsByType(
            @PathVariable Long trackingId,
            @PathVariable DocumentType documentType
    );

    @Operation(
            summary = "Search documents",
            description = """
            Search documents by name, filename, or description using full-text search.
            Search is case-insensitive and supports partial matching.
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "Search Results",
                            value = """
                {
                  "message": "Document search completed successfully",
                  "data": [
                    {
                      "id": 1,
                      "documentName": "curriculum_proposal_2025",
                      "originalFilename": "Advanced_CS_Curriculum.pdf",
                      "documentType": "CURRICULUM_PROPOSAL"
                    }
                  ]
                }
                """
                    )
            )
    )
    ResponseEntity<ApiResponse> searchDocuments(
            @RequestParam String searchTerm,
            @RequestParam(required = false) Long trackingId
    );

    @Operation(
            summary = "Update document metadata",
            description = "Updates document description and/or type. The actual file content cannot be modified.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Document metadata updated successfully"
    )
    ResponseEntity<ApiResponse> updateDocumentMetadata(
            @PathVariable Long documentId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) DocumentType documentType
    );

    @Operation(
            summary = "Delete document",
            description = """
            Soft deletes a document (marks as inactive). The file is removed from storage.
            This action cannot be undone.
            
            **Required Permissions**: ADMIN or QA roles
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Document deleted successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """
                {
                  "message": "Document deleted successfully",
                  "data": {
                    "deleted": true
                  }
                }
                """
                    )
            )
    )
    ResponseEntity<ApiResponse> deleteDocument(@PathVariable Long documentId);

    @Operation(
            summary = "Create document version",
            description = """
            Creates a new version of an existing document. The new version will have an incremented version number
            while maintaining the same document name and type.
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Document version created successfully"
    )
    ResponseEntity<ApiResponse> createDocumentVersion(
            @PathVariable Long documentId,
            @RequestParam("file") @NotNull MultipartFile newFile,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Get document versions",
            description = "Retrieves all versions of a document ordered by version number"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Document versions retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "array", implementation = TrackingDocumentDto.class)
            )
    )
    ResponseEntity<ApiResponse> getDocumentVersions(
            @RequestParam String documentName,
            @RequestParam Long trackingId
    );

    @Operation(
            summary = "Get storage statistics",
            description = """
            Retrieves storage usage statistics including:
            - Total number of documents
            - Total storage used
            - Storage breakdown by document type
            - Average file size
            
            **Required Permissions**: ADMIN or QA roles
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Storage statistics retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "Storage Statistics",
                            value = """
                {
                  "message": "Storage statistics retrieved successfully",
                  "data": {
                    "totalDocuments": 156,
                    "totalSize": 524288000,
                    "formattedStorageUsed": "500.0 MB",
                    "documentsByType": {
                      "CURRICULUM_PROPOSAL": 45,
                      "SUPPORTING_DOCUMENTS": 78,
                      "AUDIT_REPORT": 23,
                      "OTHER": 10
                    },
                    "averageFileSize": 3363846.15
                  }
                }
                """
                    )
            )
    )
    ResponseEntity<ApiResponse> getStorageStatistics(
            @RequestParam(required = false) Long trackingId
    );

    @Operation(
            summary = "Copy document to another tracking",
            description = """
            Creates a copy of a document in another tracking process. 
            The copied document will have version number 1 in the target tracking.
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Document copied successfully"
    )
    ResponseEntity<ApiResponse> copyDocument(
            @PathVariable Long documentId,
            @RequestParam Long targetTrackingId,
            @RequestParam Long targetStepId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Get document upload URL",
            description = """
            Generates a pre-signed URL for direct file upload to S3. 
            This enables client-side uploads without going through the server.
            After uploading via the URL, you must call the appropriate API to register the document in the database.
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Upload URL generated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "Upload URL Response",
                            value = """
                {
                  "message": "Upload URL generated successfully",
                  "data": {
                    "uploadUrl": "https://s3.amazonaws.com/bucket/tracking/1/steps/5/1733318400_document.pdf?X-Amz-Expires=3600...",
                    "expiresInMinutes": 60,
                    "fileName": "document.pdf"
                  }
                }
                """
                    )
            )
    )
    ResponseEntity<ApiResponse> getDocumentUploadUrl(
            @RequestParam Long trackingId,
            @RequestParam Long stepId,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam(defaultValue = "60") int expirationMinutes
    );
}