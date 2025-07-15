package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurriculumTrackingRepository extends JpaRepository<CurriculumTracking, Long> {

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        JOIN FETCH d.school s
        JOIN FETCH ct.initiatedBy ib
        LEFT JOIN FETCH ct.currentAssignee ca
        WHERE ct.id = :id
        """)
    Optional<CurriculumTracking> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        WHERE ct.trackingId = :trackingId
        """)
    Optional<CurriculumTracking> findByTrackingId(@Param("trackingId") String trackingId);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        WHERE c.id = :curriculumId
        """)
    Optional<CurriculumTracking> findByCurriculumId(@Param("curriculumId") Long curriculumId);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        JOIN FETCH d.school s
        WHERE ct.status = :status AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByStatusAndActiveTrue(@Param("status") TrackingStatus status, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        JOIN FETCH d.school s
        WHERE ct.currentStage = :stage AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByCurrentStageAndActiveTrue(@Param("stage") TrackingStage stage, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        JOIN FETCH d.school s
        WHERE ct.currentAssignee.id = :userId AND ct.isActive = true
        ORDER BY ct.updatedAt DESC
        """)
    Page<CurriculumTracking> findByCurrentAssigneeIdAndActiveTrue(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        JOIN FETCH d.school s
        WHERE ct.initiatedBy.id = :userId AND ct.isActive = true
        ORDER BY ct.createdAt DESC
        """)
    Page<CurriculumTracking> findByInitiatedByIdAndActiveTrue(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        JOIN FETCH d.school s
        WHERE s.id = :schoolId AND ct.isActive = true
        """)
    Page<CurriculumTracking> findBySchoolIdAndActiveTrue(@Param("schoolId") Long schoolId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        JOIN FETCH c.department d
        WHERE d.id = :departmentId AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByDepartmentIdAndActiveTrue(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        WHERE ct.expectedCompletionDate < :date 
        AND ct.actualCompletionDate IS NULL 
        AND ct.isActive = true
        """)
    List<CurriculumTracking> findOverdueTrackings(@Param("date") LocalDateTime date);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.curriculum c
        WHERE ct.expectedCompletionDate BETWEEN :startDate AND :endDate
        AND ct.actualCompletionDate IS NULL 
        AND ct.isActive = true
        """)
    List<CurriculumTracking> findTrackingsExpiringSoon(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    // Statistics queries
    long countByStatusAndActiveTrue(TrackingStatus status);
    long countByCurrentStageAndActiveTrue(TrackingStage stage);

    @Query("SELECT COUNT(ct) FROM CurriculumTracking ct WHERE ct.actualCompletionDate IS NOT NULL")
    long countCompleted();

    @Query("""
        SELECT AVG(FUNCTION('DATEDIFF', ct.actualCompletionDate, ct.createdAt))
        FROM CurriculumTracking ct 
        WHERE ct.actualCompletionDate IS NOT NULL
        """)
    Double getAverageCompletionDays();

    // Existence checks
    boolean existsByCurriculumId(Long curriculumId);
    boolean existsByTrackingId(String trackingId);

}
