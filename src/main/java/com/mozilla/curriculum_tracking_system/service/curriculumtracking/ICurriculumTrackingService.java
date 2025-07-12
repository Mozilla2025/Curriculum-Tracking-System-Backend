package com.mozilla.curriculum_tracking_system.service.curriculumtracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.*;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing curriculum tracking lifecycle
 */
public interface ICurriculumTrackingService {

    /**
     * initiate curriculum tracking process (QA only)
     */
    CurriculumTrackingDto initiateCurriculumTracking(InitiateCurriculumTrackingRequest request, String authToken);

    /**
     * Perform action on curriculum tracking (submit, approve, send back, etc.)
     */
    CurriculumTrackingDto performTrackingAction(CurriculumTrackingActionRequest request, String authToken);

    /**
     * Get curriculum tracking by ID
     */
    CurriculumTrackingDto getCurriculumTrackingById(Long trackingId);

    /**
     * Get curriculum tracking by curriculum ID
     */
    CurriculumTrackingDto getCurriculumTrackingByCurriculumId(Long curriculumId);

    /**
     * Get all curriculum trackings with pagination
     */
    CurriculumTrackingPageResponse getAllCurriculumTrackings(Pageable pageable);

    /**
     * Search curriculum trackings based on criteria
     */
    CurriculumTrackingPageResponse searchCurriculumTrackings(CurriculumTrackingSearchRequest searchRequest,
                                                             Pageable pageable);

    /**
     * Get curriculum trackings assigned to current user
     */
    CurriculumTrackingPageResponse getMyAssignedTrackings(String authToken, Pageable pageable);

    /**
     * Get curriculum trackings by stage
     */
    CurriculumTrackingPageResponse getCurriculumTrackingsByStage(CurriculumTrackingStage stage, Pageable pageable);

    /**
     * Get curriculum trackings by status
     */
    CurriculumTrackingPageResponse getCurriculumTrackingsByStatus(CurriculumTrackingStatus status, Pageable pageable);

    /**
     * Get overdue curriculum trackings
     */
    List<CurriculumTrackingDto> getOverdueTrackings();

    /**
     * Get curriculum trackings expiring soon
     */
    List<CurriculumTrackingDto> getExpiringSoonTrackings(int days);

    /**
     * Get curriculum tracking statistics
     */
    CurriculumTrackingStatsDto getCurriculumTrackingStats();

    /**
     * Get curriculum tracking timeline
     */
    CurriculumTrackingTimelineDto getCurriculumTrackingTimeline(Long trackingId);

    /**
     * Assign curriculum tracking to user
     */
    CurriculumTrackingDto assignTrackingToUser(Long trackingId, Long userId, String authToken);

    /**
     * Update curriculum tracking notes (QA only)
     */
    CurriculumTrackingDto updateTrackingNotes(Long trackingId, String notes, String authToken);

    /**
     * Deactivate curriculum tracking (QA only)
     */
    void deactivateCurriculumTracking(Long trackingId, String authToken);

    /**
     * Get available actions for current user on specific tracking
     */
    List<TrackingActionType> getAvailableActions(Long trackingId, String authToken);

    /**
     * Get stage information including possible transitions
     */
    CurriculumTrackingStageInfo getStageInfo(CurriculumTrackingStage stage);

    /**
     * Check if user can perform action on tracking
     */
    boolean canPerformAction(Long trackingId, TrackingActionType actionType, String authToken);

    /**
     * Validate curriculum tracking request
     */
    void validateTrackingAction(CurriculumTrackingActionRequest request, String authToken);


}
