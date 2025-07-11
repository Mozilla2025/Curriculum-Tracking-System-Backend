package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingHistoryDto;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing curriculum tracking history
 */
public interface ICurriculumTrackingHistoryService {

    /**
     * Get tracking history for a curriculum tracking
     */
    List<CurriculumTrackingHistoryDto> getTrackingHistory(Long curriculumTrackingId);

    /**
     * Get tracking history with pagination
     */
    List<CurriculumTrackingHistoryDto> getTrackingHistory(Long curriculumTrackingId, Pageable pageable);

    /**
     * Get recent tracking history entries
     */
    List<CurriculumTrackingHistoryDto> getRecentTrackingHistory(Long curriculumTrackingId, int limit);

    /**
     * Get tracking history by ID
     */
    CurriculumTrackingHistoryDto getTrackingHistoryById(Long historyId);

    /**
     * Get milestone history entries
     */
    List<CurriculumTrackingHistoryDto> getMilestoneHistory(Long curriculumTrackingId);

    /**
     * Get stage transitions for a tracking
     */
    List<CurriculumTrackingHistoryDto> getStageTransitions(Long curriculumTrackingId);

    /**
     * Get history by performer
     */
    List<CurriculumTrackingHistoryDto> getHistoryByPerformer(Long userId, Pageable pageable);

    /**
     * Get history by assignee
     */
    List<CurriculumTrackingHistoryDto> getHistoryByAssignee(Long userId, Pageable pageable);

    /**
     * Search tracking history
     */
    List<CurriculumTrackingHistoryDto> searchTrackingHistory(String searchTerm);

    /**
     * Add history entry (internal use)
     */
    CurriculumTrackingHistoryDto addHistoryEntry(CurriculumTrackingHistory historyEntry);

    /**
     * Get overdue history items
     */
    List<CurriculumTrackingHistoryDto> getOverdueHistoryItems();
}
