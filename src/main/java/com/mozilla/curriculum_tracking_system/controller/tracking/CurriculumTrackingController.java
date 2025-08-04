package com.mozilla.curriculum_tracking_system.controller.tracking;

import com.mozilla.curriculum_tracking_system.documentation.CurriculumTrackingControllerDocs;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDetailDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.InitiateTrackingRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingActionRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.service.tracking.ICurriculumTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing curriculum tracking operations
 * Handles the complete lifecycle of curriculum tracking from initiation to completion
 */
@RestController
@RequestMapping("${api.prefix}/tracking")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CurriculumTrackingController implements CurriculumTrackingControllerDocs {

    private final ICurriculumTrackingService curriculumTrackingService;
    private final IAuthenticationService authenticationService;

    /**
     * Initiate a new curriculum tracking process
     */
    @PostMapping(value = "/initiate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('QA', 'DEAN', 'HOD')")
    public ResponseEntity<ApiResponse> initiateTracking(
            @Valid @ModelAttribute InitiateTrackingRequest request,
            @RequestHeader("Authorization") String authorizationHeader
            ) {
        log.info("Initiating curriculum tracking for department: {}, school: {}",
                request.getDepartmentId(), request.getSchoolId());

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDetailDto tracking = curriculumTrackingService.initiateTracking(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking initiated successfully",
                tracking
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Perform an action on a tracking record (approve, reject, return, etc.)
     */
    @PostMapping(value = "/action", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('QA', 'DEAN')")
    public ResponseEntity<ApiResponse> performTrackingAction(
            @Valid @ModelAttribute TrackingActionRequest request,
            @RequestHeader("Authorization") String authorizationHeader
            ) {
        log.info("Performing tracking action: {} on tracking: {}",
                request.getAction(), request.getTrackingId());
        String token = extractToken(authorizationHeader);
        CurriculumTrackingDetailDto tracking = curriculumTrackingService.performTrackingAction(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "tracking action performed successfully",
                tracking
        );
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get tracking details by ID
     */
    @GetMapping("/{trackingId}")
    public ResponseEntity<ApiResponse> getTrackingById(@PathVariable Long trackingId) {
        log.debug("Fetching tracking by ID: {}", trackingId);

        CurriculumTrackingDetailDto tracking = curriculumTrackingService.getTrackingById(trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking details retrieved successfully",
                tracking
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get tracking details by tracking ID string
     */
    @GetMapping("/by-tracking-id/{trackingId}")
    public ResponseEntity<ApiResponse> getTrackingByTrackingId(@PathVariable String trackingId) {
        log.debug("Fetching tracking by tracking ID: {}", trackingId);

        CurriculumTrackingDetailDto tracking = curriculumTrackingService.getTrackingByTrackingId(trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking details retrieved successfully",
                tracking
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all trackings with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllTrackings(
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Fetching all trackings with pagination: {}", pageable);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getAllTrackings(pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Search trackings based on criteria
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse> searchTrackings(
            @Valid @RequestBody TrackingSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Searching trackings with criteria: {}", criteria);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.searchTrackings(criteria, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking search completed successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getTrackingsByStatus(
            @PathVariable TrackingStatus status,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Fetching trackings by status: {}", status);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByStatus(status, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Trackings by status retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings by current stage
     */
    @GetMapping("/stage/{stage}")
    public ResponseEntity<ApiResponse> getTrackingsByStage(
            @PathVariable TrackingStage stage,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Fetching trackings by stage: {}", stage);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByStage(stage, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Trackings by stage retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings assigned to current user
     */
    @GetMapping("/my-assignments")
    public ResponseEntity<ApiResponse> getMyAssignedTrackings(
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        // Extract user ID from token
        Long userId = getUserIdFromToken(token);

        log.debug("Fetching trackings assigned to user: {}", userId);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByAssignee(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Assigned trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings initiated by current user
     */
    @GetMapping("/my-trackings")
    public ResponseEntity<ApiResponse> getMyInitiatedTrackings(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        Long userId = getUserIdFromToken(token);

        log.debug("Fetching trackings initiated by user: {}", userId);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByInitiator(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Initiated trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings by assignee (admin function)
     */
    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> getTrackingsByAssignee(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Fetching trackings assigned to user: {}", userId);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByAssignee(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Assigned trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings by initiator (admin function)
     */
    @GetMapping("/initiator/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> getTrackingsByInitiator(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("Fetching trackings initiated by user: {}", userId);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByInitiator(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Initiated trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings for a specific school
     */
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse> getTrackingsBySchool(
            @PathVariable Long schoolId,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Fetching trackings for school: {}", schoolId);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsBySchool(schoolId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "School trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings for a specific department
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse> getTrackingsByDepartment(
            @PathVariable Long departmentId,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {

        log.debug("Fetching trackings for department: {}", departmentId);

        CurriculumTrackingPageResponse trackings = curriculumTrackingService.getTrackingsByDepartment(departmentId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Department trackings retrieved successfully",
                trackings
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update tracking information
     */
    @PutMapping(value = "/{trackingId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('QA', 'DEAN')")
    public ResponseEntity<ApiResponse> updateTracking(
            @PathVariable Long trackingId,
            @Valid @ModelAttribute InitiateTrackingRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Updating tracking: {}", trackingId);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDetailDto tracking = curriculumTrackingService.updateTracking(trackingId, request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking updated successfully",
                tracking
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Deactivate a tracking record
     */
    @PostMapping("/{trackingId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> deactivateTracking(
            @PathVariable Long trackingId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Deactivating tracking: {}", trackingId);

        String token = extractToken(authorizationHeader);
        curriculumTrackingService.deactivateTracking(trackingId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking deactivated successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Reactivate a tracking record
     */
    @PostMapping("/{trackingId}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> reactivateTracking(
            @PathVariable Long trackingId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Reactivating tracking: {}", trackingId);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDetailDto tracking = curriculumTrackingService.reactivateTracking(trackingId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking reactivated successfully",
                tracking
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Assign tracking to a different user
     */
    @PostMapping("/{trackingId}/assign/{assigneeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QA')")
    public ResponseEntity<ApiResponse> assignTracking(
            @PathVariable Long trackingId,
            @PathVariable Long assigneeId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("Assigning tracking: {} to user: {}", trackingId, assigneeId);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDetailDto tracking = curriculumTrackingService.assignTracking(trackingId, assigneeId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking assigned successfully",
                tracking
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Check if current user has permission to perform actions on a tracking
     */
    @GetMapping("/{trackingId}/has-permission")
    public ResponseEntity<ApiResponse> hasTrackingPermission(
            @PathVariable Long trackingId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        Long userId = getUserIdFromToken(token);

        boolean hasPermission = curriculumTrackingService.hasTrackingPermission(trackingId, userId);

        ApiResponse apiResponse = new ApiResponse(
                "Permission check completed",
                hasPermission
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Validate stage transition
     */
    @PostMapping("/{trackingId}/validate-transition/{targetStage}")
    public ResponseEntity<ApiResponse> validateStageTransition(
            @PathVariable Long trackingId,
            @PathVariable TrackingStage targetStage,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("Validating stage transition for tracking: {} to stage: {}", trackingId, targetStage);

        String token = extractToken(authorizationHeader);
        boolean isValid = curriculumTrackingService.validateStageTransition(trackingId, targetStage, token);

        ApiResponse apiResponse = new ApiResponse(
                "Stage transition validation completed",
                isValid
        );

        return ResponseEntity.ok(apiResponse);
    }


    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new RuntimeException("No valid token found in request");
    }

    /**
     * Extract user ID from JWT token
     */
    private Long getUserIdFromToken(String token) {
        return authenticationService.getUserIdFromToken(token);
    }
}
