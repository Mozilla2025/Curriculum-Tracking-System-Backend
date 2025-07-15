package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingStepDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingStepPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingStepSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing tracking steps
 * Handles individual steps/actions within the tracking workflow
 */
public interface ITrackingStepService {
    /**
     * Get all steps for a specific tracking
     * @param trackingId The tracking ID
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStepsByTracking(Long trackingId, Pageable pageable);

    /**
     * Get a specific tracking step by ID
     * @param stepId The step ID
     * @return The tracking step DTO
     */
    TrackingStepDto getStepById(Long stepId);

    /**
     * Search tracking steps based on criteria
     * @param criteria Search criteria
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse searchSteps(TrackingStepSearchCriteria criteria, Pageable pageable);

    /**
     * Get steps by tracking stage
     * @param trackingId The tracking ID
     * @param stage The tracking stage
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStepsByStage(Long trackingId, TrackingStage stage, Pageable pageable);

    /**
     * Get steps by action type
     * @param trackingId The tracking ID
     * @param action The tracking action
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStepsByAction(Long trackingId, TrackingAction action, Pageable pageable);

    /**
     * Get steps performed by a specific user
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStepsByUser(Long userId, Pageable pageable);

    /**
     * Get milestone steps for a tracking
     * @param trackingId The tracking ID
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getMilestoneSteps(Long trackingId, Pageable pageable);

    /**
     * Get stage transition steps for a tracking
     * @param trackingId The tracking ID
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStageTransitionSteps(Long trackingId, Pageable pageable);

    /**
     * Get recent steps for a tracking (last N steps)
     * @param trackingId The tracking ID
     * @param limit Maximum number of steps to return
     * @return List of recent tracking steps
     */
    List<TrackingStepDto> getRecentSteps(Long trackingId, int limit);

    /**
     * Get steps within a date range
     * @param trackingId The tracking ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStepsByDateRange(Long trackingId,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate,
                                                 Pageable pageable);
    /**
     * Get the latest step for a tracking
     * @param trackingId The tracking ID
     * @return The latest tracking step DTO
     */
    TrackingStepDto getLatestStep(Long trackingId);
    /**
     * Get steps assigned to a specific user
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return Paginated tracking steps response
     */
    TrackingStepPageResponse getStepsAssignedToUser(Long userId, Pageable pageable);
}
