package com.mozilla.curriculum_tracking_system.dto.swagger;

import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced DTOs with Swagger schema annotations for better API documentation
 */
public class SwaggerSchemas {

    @Schema(
            name = "InitiateTrackingRequest",
            description = "Request payload for initiating new curriculum tracking"
    )
    @Data
    public static class InitiateTrackingRequestSchema {

        @Schema(
                description = "ID of existing curriculum (optional - only for tracking changes to existing curriculum)",
                example = "null",
                nullable = true
        )
        private Long curriculumId;

        @Schema(
                description = "ID of the school where curriculum belongs",
                example = "1",
                required = true
        )
        private Long schoolId;

        @Schema(
                description = "ID of the department within the school",
                example = "2",
                required = true
        )
        private Long departmentId;

        @Schema(
                description = "ID of the academic level (Bachelor, Master, PhD, etc.)",
                example = "3",
                required = true
        )
        private Long academicLevelId;

        @Schema(
                description = "Name of the proposed curriculum",
                example = "Advanced Computer Science",
                required = true,
                maxLength = 200
        )
        private String proposedCurriculumName;

        @Schema(
                description = "Code for the proposed curriculum",
                example = "ADVCS",
                maxLength = 20
        )
        private String proposedCurriculumCode;

        @Schema(
                description = "Duration of the curriculum in semesters",
                example = "8",
                minimum = "1"
        )
        private Integer proposedDurationSemesters;

        @Schema(
                description = "Detailed description of the curriculum",
                example = "Advanced curriculum covering AI, ML, data science, and emerging technologies",
                maxLength = 2000
        )
        private String curriculumDescription;

        @Schema(
                description = "Proposed effective date when curriculum should start",
                example = "2025-09-01T00:00:00"
        )
        private LocalDateTime proposedEffectiveDate;

        @Schema(
                description = "Proposed expiry date when curriculum should be reviewed/renewed",
                example = "2030-08-31T23:59:59"
        )
        private LocalDateTime proposedExpiryDate;

        @Schema(
                description = "Initial notes from the person initiating the tracking",
                example = "Initial proposal based on industry feedback and market requirements",
                maxLength = 1000
        )
        private String initialNotes;

        @Schema(
                description = "Expected completion date for the entire tracking process",
                example = "2025-12-31T23:59:59"
        )
        private LocalDateTime expectedCompletionDate;

        @Schema(
                description = "Supporting documents (PDFs, Word docs, etc.)",
                type = "array",
                format = "binary"
        )
        private List<MultipartFile> documents;
    }

    @Schema(
            name = "TrackingActionRequest",
            description = "Request payload for performing actions on tracking"
    )
    @Data
    public static class TrackingActionRequestSchema {

        @Schema(
                description = "ID of the tracking to perform action on",
                example = "1",
                required = true
        )
        private Long trackingId;

        @Schema(
                description = "Action to perform",
                implementation = TrackingAction.class,
                required = true,
                allowableValues = {"INITIATE", "APPROVE", "REJECT", "RETURN", "SUBMIT", "REVIEW", "COMPLETE"}
        )
        private TrackingAction action;

        @Schema(
                description = "Stage to return to (required only for RETURN action)",
                implementation = TrackingStage.class,
                nullable = true
        )
        private TrackingStage returnToStage;

        @Schema(
                description = "User ID to assign tracking to (optional)",
                example = "5",
                nullable = true
        )
        private Long assignToUserId;

        @Schema(
                description = "Notes/comments about the action being performed",
                example = "Curriculum proposal meets all requirements for next stage review"
        )
        private String notes;

        @Schema(
                description = "Due date for the action (if applicable)",
                example = "2025-08-15T17:00:00"
        )
        private LocalDateTime dueDate;

        @Schema(
                description = "Whether this action represents a milestone",
                example = "true",
                defaultValue = "false"
        )
        private Boolean isMilestone;

        @Schema(
                description = "Supporting documents for the action",
                type = "array",
                format = "binary"
        )
        private List<MultipartFile> documents;
    }

    @Schema(
            name = "TrackingSearchCriteria",
            description = "Search criteria for filtering curriculum trackings"
    )
    @Data
    public static class TrackingSearchCriteriaSchema {

        @Schema(
                description = "Filter by tracking status",
                implementation = TrackingStatus.class,
                allowableValues = {"INITIATED", "IN_PROGRESS", "APPROVED", "REJECTED", "RETURNED_FOR_REVISION", "COMPLETED"}
        )
        private TrackingStatus status;

        @Schema(
                description = "Filter by current tracking stage",
                implementation = TrackingStage.class
        )
        private TrackingStage currentStage;

        @Schema(
                description = "Filter by user who initiated the tracking",
                example = "3"
        )
        private Long initiatedByUserId;

        @Schema(
                description = "Filter by currently assigned user",
                example = "5"
        )
        private Long currentAssigneeId;

        @Schema(
                description = "Filter by school ID",
                example = "1"
        )
        private Long schoolId;

        @Schema(
                description = "Filter by department ID",
                example = "2"
        )
        private Long departmentId;

        @Schema(
                description = "Filter by academic level ID",
                example = "3"
        )
        private Long academicLevelId;

        @Schema(
                description = "Filter by linked curriculum ID",
                example = "4"
        )
        private Long curriculumId;

        @Schema(
                description = "Full-text search term (searches in curriculum name, code, tracking ID)",
                example = "computer science"
        )
        private String searchTerm;

        @Schema(
                description = "Filter trackings created after this date",
                example = "2025-01-01T00:00:00"
        )
        private LocalDateTime createdAfter;

        @Schema(
                description = "Filter trackings created before this date",
                example = "2025-12-31T23:59:59"
        )
        private LocalDateTime createdBefore;

        @Schema(
                description = "Filter trackings with expected completion before this date",
                example = "2025-06-30T23:59:59"
        )
        private LocalDateTime expectedCompletionBefore;

        @Schema(
                description = "Filter by active status",
                example = "true"
        )
        private Boolean isActive;

        @Schema(
                description = "Filter overdue trackings (past expected completion date)",
                example = "false"
        )
        private Boolean isOverdue;

        @Schema(
                description = "Filter ideation stage trackings (no linked curriculum)",
                example = "true"
        )
        private Boolean isIdeationStage;

        @Schema(
                description = "Filter trackings with or without linked curriculum",
                example = "false"
        )
        private Boolean hasLinkedCurriculum;
    }

    @Schema(
            name = "DocumentUploadRequest",
            description = "Request for uploading documents to tracking"
    )
    @Data
    public static class DocumentUploadRequestSchema {

        @Schema(
                description = "File to upload",
                type = "string",
                format = "binary",
                required = true
        )
        private MultipartFile file;

        @Schema(
                description = "Tracking ID to associate document with",
                example = "1",
                required = true
        )
        private Long trackingId;

        @Schema(
                description = "Step ID within the tracking",
                example = "5",
                required = true
        )
        private Long stepId;

        @Schema(
                description = "Type of document",
                implementation = DocumentType.class,
                defaultValue = "OTHER",
                allowableValues = {
                        "CURRICULUM_PROPOSAL",
                        "SUPPORTING_DOCUMENTS",
                        "REVISION_DOCUMENTS",
                        "APPROVAL_CERTIFICATE",
                        "AUDIT_REPORT",
                        "OTHER"
                }
        )
        private DocumentType documentType;

        @Schema(
                description = "Description of the document",
                example = "Initial curriculum proposal with detailed course structure"
        )
        private String description;
    }

    @Schema(
            name = "CurriculumTrackingDetail",
            description = "Detailed information about a curriculum tracking"
    )
    @Data
    public static class CurriculumTrackingDetailSchema {

        @Schema(description = "Unique database ID", example = "1")
        private Long id;

        @Schema(description = "Human-readable tracking ID", example = "CURR-CS-2025-001")
        private String trackingId;

        @Schema(description = "Linked curriculum ID (null for ideation)", example = "4", nullable = true)
        private Long curriculumId;

        @Schema(description = "Proposed curriculum name", example = "Advanced Computer Science")
        private String proposedCurriculumName;

        @Schema(description = "Proposed curriculum code", example = "ADVCS")
        private String proposedCurriculumCode;

        @Schema(description = "Current tracking stage", implementation = TrackingStage.class)
        private TrackingStage currentStage;

        @Schema(description = "Current tracking status", implementation = TrackingStatus.class)
        private TrackingStatus status;

        @Schema(description = "School information")
        private String schoolName;

        @Schema(description = "Department information")
        private String departmentName;

        @Schema(description = "Academic level information")
        private String academicLevelName;

        @Schema(description = "User who initiated the tracking")
        private String initiatedByName;

        @Schema(description = "Currently assigned user")
        private String currentAssigneeName;

        @Schema(description = "Creation timestamp", example = "2025-08-04T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "Last update timestamp", example = "2025-08-04T15:45:00")
        private LocalDateTime updatedAt;

        @Schema(description = "Expected completion date", example = "2025-12-31T23:59:59")
        private LocalDateTime expectedCompletionDate;

        @Schema(description = "Whether tracking is active", example = "true")
        private Boolean isActive;

        @Schema(description = "Whether this is an ideation stage tracking", example = "true")
        private Boolean isIdeationStage;
    }

    @Schema(
            name = "TrackingDocument",
            description = "Information about a document in the tracking system"
    )
    @Data
    public static class TrackingDocumentSchema {

        @Schema(description = "Unique document ID", example = "1")
        private Long id;

        @Schema(description = "System-generated document name", example = "curriculum_proposal_2025")
        private String documentName;

        @Schema(description = "Original filename as uploaded", example = "Advanced_CS_Curriculum.pdf")
        private String originalFilename;

        @Schema(description = "Document type", implementation = DocumentType.class)
        private DocumentType documentType;

        @Schema(description = "File size in bytes", example = "2548736")
        private Long fileSize;

        @Schema(description = "Human-readable file size", example = "2.4 MB")
        private String formattedFileSize;

        @Schema(description = "MIME content type", example = "application/pdf")
        private String contentType;

        @Schema(description = "File extension", example = "pdf")
        private String fileExtension;

        @Schema(description = "Document description")
        private String description;

        @Schema(description = "Name of user who uploaded", example = "John Doe")
        private String uploadedByName;

        @Schema(description = "Version number", example = "1")
        private Integer versionNumber;

        @Schema(description = "Upload timestamp", example = "2025-08-04T10:30:00")
        private LocalDateTime uploadedAt;

        @Schema(description = "Whether document is active", example = "true")
        private Boolean isActive;
    }
}
