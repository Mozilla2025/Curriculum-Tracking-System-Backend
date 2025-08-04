package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingStepRepository extends JpaRepository<TrackingStep, Long>, JpaSpecificationExecutor<TrackingStep> {

    /**
     * Find all steps for a specific tracking with details
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            LEFT JOIN FETCH ts.assignedTo at
            WHERE ts.tracking.id = :trackingId
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByTrackingIdWithDetails(@Param("trackingId") Long trackingId, Pageable pageable);

    /**
     * Find steps by tracking ID ordered by performed date
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            WHERE ts.tracking.id = :trackingId
            ORDER BY ts.performedAt DESC
            """)
    List<TrackingStep> findByTrackingIdOrderByPerformedAtDesc(@Param("trackingId") Long trackingId);


    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            LEFT JOIN FETCH ts.assignedTo at
            WHERE ts.tracking.id = :trackingId
            ORDER BY ts.performedAt DESC
            LIMIT :limit
            """)
    List<TrackingStep> findRecentStepsByTrackingId(@Param("trackingId") Long trackingId, @Param("limit") int limit);

    /**
     * Find the latest step for a tracking
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            LEFT JOIN FETCH ts.assignedTo at
            WHERE ts.tracking.id = :trackingId
            ORDER BY ts.performedAt DESC
            LIMIT 1
            """)
    Optional<TrackingStep> findLatestStepByTrackingId(@Param("trackingId") Long trackingId);

    /**
     * Find steps by tracking and stage
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id = :trackingId AND ts.stage = :stage
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByTrackingIdAndStage(@Param("trackingId") Long trackingId,
                                                @Param("stage") TrackingStage stage,
                                                Pageable pageable);

    /**
     * Find steps by tracking and action
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id = :trackingId AND ts.action = :action
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByTrackingIdAndAction(@Param("trackingId") Long trackingId,
                                                 @Param("action") TrackingAction action,
                                                 Pageable pageable);

    /**
     * Find steps performed by a specific user
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH t.school s
            JOIN FETCH t.department d
            WHERE ts.performedBy.id = :userId
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByPerformedByIdOrderByPerformedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find steps assigned to a specific user
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH t.school s
            JOIN FETCH t.department d
            WHERE ts.assignedTo.id = :userId
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByAssignedToIdOrderByPerformedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find milestone steps for a tracking
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id = :trackingId AND ts.isMilestone = true
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findMilestoneStepsByTrackingId(@Param("trackingId") Long trackingId, Pageable pageable);

    /**
     * Find stage transition steps for a tracking
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id = :trackingId 
            AND ts.fromStage IS NOT NULL 
            AND ts.toStage IS NOT NULL 
            AND ts.fromStage != ts.toStage
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findStageTransitionStepsByTrackingId(@Param("trackingId") Long trackingId, Pageable pageable);

    /**
     * Find steps within a date range for a tracking
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id = :trackingId 
            AND ts.performedAt BETWEEN :startDate AND :endDate
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByTrackingIdAndPerformedAtBetween(@Param("trackingId") Long trackingId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate,
                                                             Pageable pageable);

    /**
     * Find steps by stage across all trackings
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.performedBy pb
            WHERE ts.stage = :stage
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByStageOrderByPerformedAtDesc(@Param("stage") TrackingStage stage, Pageable pageable);

    /**
     * Find steps by action across all trackings
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.performedBy pb
            WHERE ts.action = :action
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByActionOrderByPerformedAtDesc(@Param("action") TrackingAction action, Pageable pageable);

    /**
     * Count steps by tracking ID
     */
    long countByTrackingId(Long trackingId);

    /**
     * Count milestone steps by tracking ID
     */
    long countByTrackingIdAndIsMilestoneTrue(Long trackingId);

    /**
     * Count steps by user
     */
    long countByPerformedById(Long userId);

    /**
     * Find steps with documents
     */
    @Query("""
            SELECT DISTINCT ts FROM TrackingStep ts
            JOIN FETCH ts.documents d
            WHERE ts.tracking.id = :trackingId AND d.isActive = true
            ORDER BY ts.performedAt DESC
            """)
    List<TrackingStep> findStepsWithDocumentsByTrackingId(@Param("trackingId") Long trackingId);

    /**
     * Check if step exists for tracking
     */
    boolean existsByTrackingIdAndId(Long trackingId, Long stepId);

    /**
     * Find pending approval steps (where due date is set and not yet acted upon)
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.assignedTo at
            WHERE ts.dueDate IS NOT NULL 
            AND ts.dueDate < :currentDate
            AND NOT EXISTS (
                SELECT ts2 FROM TrackingStep ts2 
                WHERE ts2.tracking.id = ts.tracking.id 
                AND ts2.performedAt > ts.performedAt
            )
            ORDER BY ts.dueDate ASC
            """)
    List<TrackingStep> findOverdueSteps(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find steps due soon
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.assignedTo at
            WHERE ts.dueDate BETWEEN :startDate AND :endDate
            AND NOT EXISTS (
                SELECT ts2 FROM TrackingStep ts2 
                WHERE ts2.tracking.id = ts.tracking.id 
                AND ts2.performedAt > ts.performedAt
            )
            ORDER BY ts.dueDate ASC
            """)
    List<TrackingStep> findStepsDueSoon(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find first step for each tracking (initiation steps)
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.performedBy pb
            WHERE ts.action = 'INITIATE'
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findInitiationSteps(Pageable pageable);

    /**
     * Find completion steps (final steps in tracking workflow)
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.performedBy pb
            WHERE ts.action = 'COMPLETE'
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findCompletionSteps(Pageable pageable);

    /**
     * Find steps that resulted in approval
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.performedBy pb
            WHERE ts.action = 'APPROVE'
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findApprovalSteps(Pageable pageable);

    /**
     * Find steps that resulted in rejection
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.performedBy pb
            WHERE ts.action = 'REJECT'
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findRejectionSteps(Pageable pageable);

    /**
     * Find steps by tracking and user (either performed by or assigned to)
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            LEFT JOIN FETCH ts.assignedTo at
            WHERE ts.tracking.id = :trackingId 
            AND (ts.performedBy.id = :userId OR ts.assignedTo.id = :userId)
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByTrackingIdAndUserId(@Param("trackingId") Long trackingId,
                                                 @Param("userId") Long userId,
                                                 Pageable pageable);

    /**
     * Get step statistics by tracking
     */
    @Query("""
            SELECT 
                COUNT(ts) as totalSteps,
                COUNT(CASE WHEN ts.isMilestone = true THEN 1 END) as milestoneSteps,
                COUNT(CASE WHEN ts.action = 'APPROVE' THEN 1 END) as approvalSteps,
                COUNT(CASE WHEN ts.action = 'REJECT' THEN 1 END) as rejectionSteps,
                COUNT(CASE WHEN ts.action = 'RETURN' THEN 1 END) as returnSteps
            FROM TrackingStep ts
            WHERE ts.tracking.id = :trackingId
            """)
    Object[] getStepStatisticsByTrackingId(@Param("trackingId") Long trackingId);

    /**
     * Find steps with specific notes pattern
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id = :trackingId 
            AND ts.notes IS NOT NULL 
            AND LOWER(ts.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY ts.performedAt DESC
            """)
    List<TrackingStep> findByTrackingIdAndNotesContaining(@Param("trackingId") Long trackingId,
                                                          @Param("searchTerm") String searchTerm);

    /**
     * Delete steps by tracking ID (for cleanup)
     */
    void deleteByTrackingId(Long trackingId);

    /**
     * Find steps that need attention (assigned but no follow-up action within timeframe)
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.tracking t
            JOIN FETCH ts.assignedTo at
            WHERE ts.assignedTo IS NOT NULL
            AND ts.performedAt < :cutoffDate
            AND NOT EXISTS (
                SELECT ts2 FROM TrackingStep ts2 
                WHERE ts2.tracking.id = ts.tracking.id 
                AND ts2.performedAt > ts.performedAt
                AND ts2.performedBy.id = ts.assignedTo.id
            )
            ORDER BY ts.performedAt ASC
            """)
    List<TrackingStep> findStepsNeedingAttention(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find all unique stages in the system
     */
    @Query("SELECT DISTINCT ts.stage FROM TrackingStep ts ORDER BY ts.stage")
    List<TrackingStage> findDistinctStages();

    /**
     * Find all unique actions in the system
     */
    @Query("SELECT DISTINCT ts.action FROM TrackingStep ts ORDER BY ts.action")
    List<TrackingAction> findDistinctActions();

    /**
     * Count steps by stage
     */
    long countByStage(TrackingStage stage);

    /**
     * Count steps by action
     */
    long countByAction(TrackingAction action);

    /**
     * Find steps by multiple trackings
     */
    @Query("""
            SELECT ts FROM TrackingStep ts
            JOIN FETCH ts.performedBy pb
            WHERE ts.tracking.id IN :trackingIds
            ORDER BY ts.performedAt DESC
            """)
    Page<TrackingStep> findByTrackingIdIn(@Param("trackingIds") List<Long> trackingIds, Pageable pageable);
}