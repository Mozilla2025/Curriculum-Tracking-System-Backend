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
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

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
            @Valid @ModelAttribute InitiateTrackingRequest request,
            @RequestHeader("Authorization") String authorizationHeader
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
            @Valid @ModelAttribute TrackingActionRequest request,
            @RequestHeader("Authorization") String authorizationHeader
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
            @PathVariable Long trackingId
    );

    @Operation(
            summary = "Get tracking by tracking ID",
            description = "Retrieves tracking information using the human-readable tracking ID (e.g., CURR-CS-2025-001)"
    )
    ResponseEntity<ApiResponse> getTrackingByTrackingId(
            @PathVariable String trackingId
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
            @Valid @org.springframework.web.bind.annotation.RequestBody TrackingSearchCriteria criteria,
            Pageable pageable
    );

    @Operation(
            summary = "Get trackings by status",
            description = "Retrieves trackings filtered by status (INITIATED, IN_PROGRESS, APPROVED, REJECTED, etc.)"
    )
    ResponseEntity<ApiResponse> getTrackingsByStatus(
            @PathVariable TrackingStatus status,
            Pageable pageable
    );

    @Operation(
            summary = "Get trackings by stage",
            description = "Retrieves trackings filtered by current stage (IDEATION, REVIEW_APPROVAL, SCHOOL_BOARD, etc.)"
    )
    ResponseEntity<ApiResponse> getTrackingsByStage(
            @PathVariable TrackingStage stage,
            Pageable pageable
    );

    @Operation(
            summary = "Get my assigned trackings",
            description = "Retrieves trackings assigned to the current authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> getMyAssignedTrackings(
            Pageable pageable,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Get my initiated trackings",
            description = "Retrieves trackings initiated by the current authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> getMyInitiatedTrackings(
            Pageable pageable,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Update tracking",
            description = "Updates tracking information. Only certain fields can be updated depending on the current stage.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> updateTracking(
            @PathVariable Long trackingId,
            @Valid @ModelAttribute InitiateTrackingRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Assign tracking to user",
            description = "Assigns a tracking to a different user. Requires ADMIN or QA permissions.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> assignTracking(
            @PathVariable Long trackingId,
            @PathVariable Long assigneeId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Check tracking permissions",
            description = "Checks if the current user has permission to perform actions on a tracking",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> hasTrackingPermission(
            @PathVariable Long trackingId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @Operation(
            summary = "Validate stage transition",
            description = "Validates if a stage transition is allowed for the current tracking state",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    ResponseEntity<ApiResponse> validateStageTransition(
            @PathVariable Long trackingId,
            @PathVariable TrackingStage targetStage,
            @RequestHeader("Authorization") String authorizationHeader
    );
}