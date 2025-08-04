package com.mozilla.curriculum_tracking_system.documentation;


import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDetailDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.InitiateTrackingRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingActionRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Documentation interface for CurriculumTrackingController
 * This interface contains all Swagger annotations for the tracking endpoints
 * to keep the main controller clean and focused on business logic.
 */
@Tag(
        name = "Curriculum Tracking",
        description = "APIs for managing curriculum tracking lifecycle from ideation to accreditation"
)
public interface CurriculumTrackingControllerDocs {

    @Operation(
            summary = "Initiate new curriculum tracking",
            description = """
            Initiates a new curriculum tracking process. This can be either:
            - **Ideation Tracking**: For new curriculum ideas (curriculumId not provided)
            - **Existing Curriculum Tracking**: For tracking changes to existing curriculum (curriculumId provided)
            
            **Required Permissions**: QA, DEAN, or HOD roles
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Tracking initiated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "Successful Initiation",
                            value = """
                {
                  "message": "Curriculum tracking initiated successfully",
                  "data": {
                    "id": 1,
                    "trackingId": "CURR-CS-2025-001",
                    "proposedCurriculumName": "Advanced Computer Science",
                    "proposedCurriculumCode": "ADVCS",
                    "currentStage": "IDEATION",
                    "status": "INITIATED",
                    "schoolName": "School of Computing",
                    "departmentName": "Computer Science",
                    "isIdeationStage": true
                  }
                }
                """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data or business rule violation",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "Validation Error",
                            value = """
                {
                  "message": "Request validation failed",
                  "data": {
                    "proposedCurriculumName": "Proposed curriculum name is required",
                    "schoolId": "School ID is required"
                  }
                }
                """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    ResponseEntity<ApiResponse> initiateTracking(
            @RequestBody(
                    description = "Tracking initiation request with curriculum details and supporting documents",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = InitiateTrackingRequest.class),
                            examples = @ExampleObject(
                                    name = "Ideation Tracking Request",
                                    description = "Example request for initiating new curriculum idea tracking",
                                    value = """
                    {
                      "schoolId": 1,
                      "departmentId": 2,
                      "academicLevelId": 3,
                      "proposedCurriculumName": "Advanced Computer Science",
                      "proposedCurriculumCode": "ADVCS",
                      "proposedDurationSemesters": 8,
                      "curriculumDescription": "Advanced curriculum covering AI, ML, and emerging technologies",
                      "proposedEffectiveDate": "2025-09-01T00:00:00",
                      "proposedExpiryDate": "2030-08-31T23:59:59",
                      "initialNotes": "Initial proposal based on industry requirements",
                      "expectedCompletionDate": "2025-12-31T23:59:59",
                      "documents": ["curriculum_proposal.pdf", "supporting_docs.docx"]
                    }
                    """
                            )
                    )
            )
            InitiateTrackingRequest request,

            @Parameter(
                    description = "JWT authentication token",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
            )
            String authorizationHeader
    );

    @Operation(
            summary = "Perform tracking action",
            description = """
            Performs an action on a tracking record. Available actions:
            - **APPROVE**: Move to next stage
            - **REJECT**: Reject the curriculum
            - **RETURN**: Return to previous stage for revision
            - **SUBMIT**: Submit for next review
            - **COMPLETE**: Mark as completed
            
            **Required Permissions**: QA or DEAN roles
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Action performed successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiResponse.class)
            )
    )
    ResponseEntity<ApiResponse> performTrackingAction(
            @RequestBody(
                    description = "Action request with tracking ID, action type, and optional supporting documents",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = TrackingActionRequest.class),
                            examples = @ExampleObject(
                                    name = "Approve Action",
                                    value = """
                    {
                      "trackingId": 1,
                      "action": "APPROVE",
                      "notes": "Curriculum meets all requirements for next stage",
                      "isMilestone": true,
                      "documents": ["approval_certificate.pdf"]
                    }
                    """
                            )
                    )
            )
            TrackingActionRequest request,
            String authorizationHeader
    );

    @Operation(
            summary = "Get tracking by ID",
            description = "Retrieves detailed tracking information including recent steps and documents"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Tracking details retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CurriculumTrackingDetailDto.class)
            )
    )
    ResponseEntity<ApiResponse> getTrackingById(
            @Parameter(
                    description = "Tracking database ID",
                    required = true,
                    example = "1"
            )
            Long trackingId
    );

    @Operation(
            summary = "Get tracking by tracking ID",
            description = "Retrieves tracking information using the human-readable tracking ID (e.g., CURR-CS-2025-001)"
    )
    ResponseEntity<ApiResponse> getTrackingByTrackingId(
            @Parameter(
                    description = "Human-readable tracking ID",
                    required = true,
                    example = "CURR-CS-2025-001"
            )
            String trackingId
    );

    @Operation(
            summary = "Get all trackings",
            description = "Retrieves paginated list of all active trackings"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Trackings retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CurriculumTrackingPageResponse.class)
            )
    )
    ResponseEntity<ApiResponse> getAllTrackings(
            @Parameter(
                    description = "Pagination parameters (page, size, sort)",
                    example = "page=0&size=20&sort=updatedAt,desc"
            )
            Pageable pageable
    );

    @Operation(
            summary = "Search trackings",
            description = """
            Advanced search for trackings using multiple criteria:
            - Status filtering
            - Stage filtering  
            - Date range filtering
            - User filtering (initiator/assignee)
            - School/Department filtering
            - Full-text search
            """
    )
    ResponseEntity<ApiResponse> searchTrackings(
            @RequestBody(
                    description = "Search criteria for filtering trackings",
                    content = @Content(
                            schema = @Schema(implementation = TrackingSearchCriteria.class),
                            examples = @ExampleObject(
                                    name = "Search Example",
                                    value = """
                    {
                      "status": "IN_PROGRESS",
                      "currentStage": "REVIEW_APPROVAL",
                      "schoolId": 1,
                      "searchTerm": "computer science",
                      "createdAfter": "2025-01-01T00:00:00",
                      "isActive": true
                    }
                    """
                            )
                    )
            )
            TrackingSearchCriteria criteria,
            Pageable pageable
    );

    @Operation(
            summary = "Get trackings by status",
            description = "Retrieves trackings filtered by status (INITIATED, IN_PROGRESS, APPROVED, REJECTED, etc.)"
    )
    ResponseEntity<ApiResponse> getTrackingsByStatus(
            @Parameter(
                    description = "Tracking status to filter by",
                    required = true,
                    schema = @Schema(implementation = TrackingStatus.class)
            )
            TrackingStatus status,
            Pageable pageable
    );

    @Operation(
            summary = "Get trackings by stage",
            description = "Retrieves trackings filtered by current stage (IDEATION, REVIEW_APPROVAL, SCHOOL_BOARD, etc.)"
    )
    ResponseEntity<ApiResponse> getTrackingsByStage(
            @Parameter(
                    description = "Tracking stage to filter by",
                    required = true,
                    schema = @Schema(implementation = TrackingStage.class)
            )
            TrackingStage stage,
            Pageable pageable
    );

    @Operation(
            summary = "Get my assigned trackings",
            description = "Retrieves trackings assigned to the current authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> getMyAssignedTrackings(
            Pageable pageable,
            String authorizationHeader
    );

    @Operation(
            summary = "Get my initiated trackings",
            description = "Retrieves trackings initiated by the current authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> getMyInitiatedTrackings(
            Pageable pageable,
            String authorizationHeader
    );

    @Operation(
            summary = "Update tracking",
            description = "Updates tracking information. Only certain fields can be updated depending on the current stage.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> updateTracking(
            Long trackingId,
            InitiateTrackingRequest request,
            String authorizationHeader
    );

    @Operation(
            summary = "Assign tracking to user",
            description = "Assigns a tracking to a different user. Requires ADMIN or QA permissions.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> assignTracking(
            Long trackingId,
            Long assigneeId,
            String authorizationHeader
    );

    @Operation(
            summary = "Check tracking permissions",
            description = "Checks if the current user has permission to perform actions on a tracking",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> hasTrackingPermission(
            Long trackingId,
            String authorizationHeader
    );

    @Operation(
            summary = "Validate stage transition",
            description = "Validates if a stage transition is allowed for the current tracking state",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> validateStageTransition(
            Long trackingId,
            TrackingStage targetStage,
            String authorizationHeader
    );
}
