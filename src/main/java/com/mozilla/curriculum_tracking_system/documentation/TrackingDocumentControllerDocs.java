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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            @Parameter(
                    description = "File to upload",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            MultipartFile file,

            @Parameter(
                    description = "Tracking ID to associate the document with",
                    required = true,
                    example = "1"
            )
            Long trackingId,

            @Parameter(
                    description = "Step ID within the tracking process",
                    required = true,
                    example = "5"
            )
            Long stepId,

            @Parameter(
                    description = "Type of document being uploaded",
                    schema = @Schema(implementation = DocumentType.class),
                    example = "CURRICULUM_PROPOSAL"
            )
            DocumentType documentType,

            @Parameter(
                    description = "Optional description of the document",
                    example = "Initial curriculum proposal for Advanced Computer Science program"
            )
            String description,

            String authorizationHeader
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
            @Parameter(
                    description = "List of files to upload",
                    required = true
            )
            List<MultipartFile> files,

            Long trackingId,
            Long stepId,
            DocumentType documentType,

            @Parameter(
                    description = "Optional descriptions for each file (must match file count)"
            )
            List<String> descriptions,

            String authorizationHeader
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
    ResponseEntity<InputStreamResource> downloadDocument(
            @Parameter(
                    description = "Document ID to download",
                    required = true,
                    example = "1"
            )
            Long documentId
    );

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
            Long documentId,

            @Parameter(
                    description = "URL expiration time in minutes",
                    example = "60"
            )
            int expirationMinutes
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
    ResponseEntity<ApiResponse> getDocumentMetadata(
            @Parameter(
                    description = "Document ID",
                    required = true,
                    example = "1"
            )
            Long documentId
    );

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
    ResponseEntity<ApiResponse> getDocumentsByTracking(
            @Parameter(
                    description = "Tracking ID",
                    required = true,
                    example = "1"
            )
            Long trackingId
    );

    @Operation(
            summary = "Get documents by step",
            description = "Retrieves all documents associated with a specific tracking step"
    )
    ResponseEntity<ApiResponse> getDocumentsByStep(
            @Parameter(
                    description = "Step ID",
                    required = true,
                    example = "5"
            )
            Long stepId
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
            @Parameter(
                    description = "Search term to match against document names and descriptions",
                    required = true,
                    example = "curriculum proposal"
            )
            String searchTerm,

            @Parameter(
                    description = "Optional tracking ID to limit search scope",
                    example = "1"
            )
            Long trackingId
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
            Long documentId,

            @Parameter(
                    description = "New description for the document"
            )
            String description,

            @Parameter(
                    description = "New document type",
                    schema = @Schema(implementation = DocumentType.class)
            )
            DocumentType documentType
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
    ResponseEntity<ApiResponse> deleteDocument(
            @Parameter(
                    description = "Document ID to delete",
                    required = true,
                    example = "1"
            )
            Long documentId
    );

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
            @Parameter(
                    description = "Original document ID to create a new version of",
                    required = true,
                    example = "1"
            )
            Long documentId,

            @Parameter(
                    description = "New file version",
                    required = true
            )
            MultipartFile newFile,

            @Parameter(
                    description = "Optional description for the new version"
            )
            String description,

            String authorizationHeader
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
            @Parameter(
                    description = "Document name to get versions for",
                    required = true,
                    example = "curriculum_proposal_2025"
            )
            String documentName,

            @Parameter(
                    description = "Tracking ID",
                    required = true,
                    example = "1"
            )
            Long trackingId
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
            @Parameter(
                    description = "Optional tracking ID to filter statistics",
                    example = "1"
            )
            Long trackingId
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
            @Parameter(
                    description = "Source document ID to copy",
                    required = true,
                    example = "1"
            )
            Long documentId,

            @Parameter(
                    description = "Target tracking ID",
                    required = true,
                    example = "2"
            )
            Long targetTrackingId,

            @Parameter(
                    description = "Target step ID",
                    required = true,
                    example = "8"
            )
            Long targetStepId,

            String authorizationHeader
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
            @Parameter(
                    description = "Tracking ID",
                    required = true,
                    example = "1"
            )
            Long trackingId,

            @Parameter(
                    description = "Step ID",
                    required = true,
                    example = "5"
            )
            Long stepId,

            @Parameter(
                    description = "File name",
                    required = true,
                    example = "curriculum_proposal.pdf"
            )
            String fileName,

            @Parameter(
                    description = "Content type of the file",
                    required = true,
                    example = "application/pdf"
            )
            String contentType,

            @Parameter(
                    description = "URL expiration time in minutes",
                    example = "60"
            )
            int expirationMinutes
    );
}
