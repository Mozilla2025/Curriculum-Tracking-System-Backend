package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDetailDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.InitiateTrackingRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingActionRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing curriculum tracking operations
 * Handles the complete lifecycle of curriculum idea tracking from initiation to completion
 */
public interface ICurriculumTrackingService {

    /**
     * Initiate a new curriculum tracking process
     *
     * @param request   The tracking initiation request
     * @param authToken Authentication token for user validation
     * @return The created tracking detail DTO
     */
    CurriculumTrackingDetailDto initiateTracking(InitiateTrackingRequest request, String authToken);

    /**
     * Perform an action on a tracking record (approve, reject, return, etc.)
     *
     * @param request   The tracking action request
     * @param authToken Authentication token for user validation
     * @return The updated tracking detail DTO
     */
    CurriculumTrackingDetailDto performTrackingAction(TrackingActionRequest request, String authToken);

    /**
     * Get tracking details by ID
     *
     * @param trackingId The tracking ID
     * @return The tracking detail DTO
     */
    CurriculumTrackingDetailDto getTrackingById(Long trackingId);

    /**
     * Get tracking details by tracking ID string
     *
     * @param trackingId The tracking ID string (e.g., CURR-CS-2025-001)
     * @return The tracking detail DTO
     */
    CurriculumTrackingDetailDto getTrackingByTrackingId(String trackingId);

    /**
     * Get all trackings with pagination
     *
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getAllTrackings(Pageable pageable);

    /**
     * Search trackings based on criteria
     *
     * @param criteria Search criteria
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse searchTrackings(TrackingSearchCriteria criteria, Pageable pageable);

    /**
     * Get trackings by status
     *
     * @param status   The tracking status
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getTrackingsByStatus(TrackingStatus status, Pageable pageable);

    /**
     * Get trackings by current stage
     *
     * @param stage    The current tracking stage
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getTrackingsByStage(TrackingStage stage, Pageable pageable);

    /**
     * Get trackings assigned to a specific user
     *
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getTrackingsByAssignee(Long userId, Pageable pageable);

    /**
     * Get trackings initiated by a specific user
     *
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getTrackingsByInitiator(Long userId, Pageable pageable);

    /**
     * Get trackings for a specific school
     *
     * @param schoolId The school ID
     * @param pageable Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getTrackingsBySchool(Long schoolId, Pageable pageable);

    /**
     * Get trackings for a specific department
     *
     * @param departmentId The department ID
     * @param pageable     Pagination parameters
     * @return Paginated tracking response
     */
    CurriculumTrackingPageResponse getTrackingsByDepartment(Long departmentId, Pageable pageable);

    /**
     * Update tracking information
     *
     * @param trackingId The tracking ID
     * @param request    The update request
     * @param authToken  Authentication token for user validation
     * @return The updated tracking detail DTO
     */
    CurriculumTrackingDetailDto updateTracking(Long trackingId, InitiateTrackingRequest request, String authToken);

    /**
     * Deactivate a tracking record
     *
     * @param trackingId The tracking ID
     * @param authToken  Authentication token for user validation
     */
    void deactivateTracking(Long trackingId, String authToken);

    /**
     * Reactivate a tracking record
     *
     * @param trackingId The tracking ID
     * @param authToken  Authentication token for user validation
     * @return The reactivated tracking detail DTO
     */
    CurriculumTrackingDetailDto reactivateTracking(Long trackingId, String authToken);

    /**
     * Assign tracking to a different user
     *
     * @param trackingId The tracking ID
     * @param assigneeId The new assignee user ID
     * @param authToken  Authentication token for user validation
     * @return The updated tracking detail DTO
     */
    CurriculumTrackingDetailDto assignTracking(Long trackingId, Long assigneeId, String authToken);

    /**
     * Check if user has permission to perform actions on a tracking
     *
     * @param trackingId The tracking ID
     * @param userId     The user ID
     * @return true if user has permission, false otherwise
     */
    boolean hasTrackingPermission(Long trackingId, Long userId);

    /**
     * Validate stage transition
     *
     * @param trackingId  The tracking ID
     * @param targetStage The target stage
     * @param authToken   Authentication token for user validation
     * @return true if transition is valid, false otherwise
     */
    boolean validateStageTransition(Long trackingId, TrackingStage targetStage, String authToken);
}
