package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CurriculumTrackingHistoryRepository extends JpaRepository<CurriculumTrackingHistory, Long> {

    List<CurriculumTrackingHistory> findByCurriculumTrackingIdOrderByActionDateDesc(Long curriculumTrackingId);

    Page<CurriculumTrackingHistory> findByCurriculumTrackingId(Long curriculumTrackingId, Pageable pageable);

    @Query("SELECT cth FROM CurriculumTrackingHistory cth " +
            "WHERE cth.curriculumTracking.id = :trackingId " +
            "ORDER BY cth.actionDate DESC")
    List<CurriculumTrackingHistory> findRecentByCurriculumTrackingId(@Param("trackingId") Long trackingId,
                                                                     Pageable pageable);

    List<CurriculumTrackingHistory> findByStageOrderByActionDateDesc(CurriculumTrackingStage stage);

    Page<CurriculumTrackingHistory> findByStage(CurriculumTrackingStage stage, Pageable pageable);

    List<CurriculumTrackingHistory> findByActionTypeOrderByActionDateDesc(TrackingActionType actionType);

    Page<CurriculumTrackingHistory> findByPerformedByOrderByActionDateDesc(Long performedBy, Pageable pageable);

    Page<CurriculumTrackingHistory> findByAssignedToOrderByActionDateDesc(Long assignedTo, Pageable pageable);

    @Query("SELECT cth FROM CurriculumTrackingHistory cth " +
            "WHERE cth.curriculumTracking.id = :trackingId " +
            "AND cth.isMilestone = true " +
            "ORDER BY cth.actionDate ASC")
    List<CurriculumTrackingHistory> findMilestonesByCurriculumTrackingId(@Param("trackingId") Long trackingId);

    @Query("SELECT cth FROM CurriculumTrackingHistory cth " +
            "WHERE cth.fromStage IS NOT NULL " +
            "AND cth.toStage IS NOT NULL " +
            "AND cth.fromStage != cth.toStage " +
            "ORDER BY cth.actionDate DESC")
    List<CurriculumTrackingHistory> findStageTransitions();

    @Query("SELECT cth FROM CurriculumTrackingHistory cth " +
            "WHERE cth.dueDate < :currentDate " +
            "AND cth.curriculumTracking.completedAt IS NULL " +
            "AND cth.curriculumTracking.isActive = true")
    List<CurriculumTrackingHistory> findOverdueItems(@Param("currentDate") LocalDateTime currentDate);

    List<CurriculumTrackingHistory> findByActionDateBetweenOrderByActionDateDesc(LocalDateTime startDate,
                                                                                 LocalDateTime endDate);

    long countByActionType(TrackingActionType actionType);

    long countByStage(CurriculumTrackingStage stage);

    @Query("SELECT cth FROM CurriculumTrackingHistory cth " +
            "LEFT JOIN FETCH cth.documents " +
            "WHERE cth.id = :id")
    CurriculumTrackingHistory findByIdWithDocuments(@Param("id") Long id);

    @Query("SELECT cth FROM CurriculumTrackingHistory cth " +
            "WHERE LOWER(cth.comments) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY cth.actionDate DESC")
    List<CurriculumTrackingHistory> searchInComments(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(h) FROM CurriculumTrackingHistory h WHERE h.actionDate BETWEEN :startDate AND :endDate")
    long countByActionDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(FUNCTION('DATEDIFF', CURRENT_DATE, ct.stageUpdatedAt)) FROM CurriculumTracking ct WHERE ct.isActive = true")
    Double findAverageDaysInCurrentStage();

}
