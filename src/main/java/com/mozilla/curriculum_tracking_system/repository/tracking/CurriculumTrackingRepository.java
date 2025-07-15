package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
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
public interface CurriculumTrackingRepository extends JpaRepository<CurriculumTracking, Long>, JpaSpecificationExecutor<CurriculumTracking> {

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        JOIN FETCH ct.initiatedBy ib
        LEFT JOIN FETCH ct.currentAssignee ca
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.id = :id
        """)
    Optional<CurriculumTracking> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.trackingId = :trackingId
        """)
    Optional<CurriculumTracking> findByTrackingId(@Param("trackingId") String trackingId);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE c.id = :curriculumId
        """)
    Optional<CurriculumTracking> findByCurriculumId(@Param("curriculumId") Long curriculumId);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.status = :status AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByStatusAndActiveTrue(@Param("status") TrackingStatus status, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.currentStage = :stage AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByCurrentStageAndActiveTrue(@Param("stage") TrackingStage stage, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.currentAssignee.id = :userId AND ct.isActive = true
        ORDER BY ct.updatedAt DESC
        """)
    Page<CurriculumTracking> findByCurrentAssigneeIdAndActiveTrue(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.initiatedBy.id = :userId AND ct.isActive = true
        ORDER BY ct.createdAt DESC
        """)
    Page<CurriculumTracking> findByInitiatedByIdAndActiveTrue(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE s.id = :schoolId AND ct.isActive = true
        """)
    Page<CurriculumTracking> findBySchoolIdAndActiveTrue(@Param("schoolId") Long schoolId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE d.id = :departmentId AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByDepartmentIdAndActiveTrue(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE al.id = :academicLevelId AND ct.isActive = true
        """)
    Page<CurriculumTracking> findByAcademicLevelIdAndActiveTrue(@Param("academicLevelId") Long academicLevelId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.expectedCompletionDate < :date 
        AND ct.actualCompletionDate IS NULL 
        AND ct.isActive = true
        """)
    List<CurriculumTracking> findOverdueTrackings(@Param("date") LocalDateTime date);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.expectedCompletionDate BETWEEN :startDate AND :endDate
        AND ct.actualCompletionDate IS NULL 
        AND ct.isActive = true
        """)
    List<CurriculumTracking> findTrackingsExpiringSoon(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        WHERE ct.curriculum IS NULL AND ct.isActive = true
        ORDER BY ct.createdAt DESC
        """)
    Page<CurriculumTracking> findIdeationTrackings(Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        WHERE ct.curriculum IS NOT NULL AND ct.isActive = true
        ORDER BY ct.updatedAt DESC
        """)
    Page<CurriculumTracking> findTrackingsWithLinkedCurriculum(Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE ct.isActive = true
        AND (
            LOWER(ct.proposedCurriculumName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ct.proposedCurriculumCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ct.trackingId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR (c.name IS NOT NULL AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            OR (c.code IS NOT NULL AND LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        )
        ORDER BY ct.updatedAt DESC
        """)
    Page<CurriculumTracking> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        JOIN FETCH ct.school s
        JOIN FETCH ct.department d
        JOIN FETCH ct.academicLevel al
        LEFT JOIN FETCH ct.curriculum c
        WHERE d.id = :departmentId 
        AND ct.curriculum IS NULL 
        AND ct.isActive = true
        ORDER BY ct.createdAt DESC
        """)
    Page<CurriculumTracking> findIdeationTrackingsByDepartment(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("""
        SELECT ct FROM CurriculumTracking ct
        WHERE ct.proposedCurriculumName = :name 
        AND ct.department.id = :departmentId 
        AND ct.academicLevel.id = :academicLevelId
        AND ct.curriculum IS NULL
        AND ct.isActive = true
        """)
    Optional<CurriculumTracking> findIdeationByNameAndDepartmentAndLevel(
            @Param("name") String name,
            @Param("departmentId") Long departmentId,
            @Param("academicLevelId") Long academicLevelId);

    long countByStatusAndIsActiveTrue(TrackingStatus status);
    long countByCurrentStageAndIsActiveTrue(TrackingStage stage);

    @Query("SELECT COUNT(ct) FROM CurriculumTracking ct WHERE ct.actualCompletionDate IS NOT NULL")
    long countCompleted();

    @Query("SELECT COUNT(ct) FROM CurriculumTracking ct WHERE ct.curriculum IS NULL AND ct.isActive = true")
    long countIdeationTrackings();

    @Query("SELECT COUNT(ct) FROM CurriculumTracking ct WHERE ct.curriculum IS NOT NULL AND ct.isActive = true")
    long countTrackingsWithLinkedCurriculum();

    @Query("""
        SELECT AVG(EXTRACT(EPOCH FROM (ct.actualCompletionDate - ct.createdAt)) / 86400)
        FROM CurriculumTracking ct 
        WHERE ct.actualCompletionDate IS NOT NULL
        """)
    Double getAverageCompletionDays();

    boolean existsByCurriculumId(Long curriculumId);
    boolean existsByTrackingId(String trackingId);

    boolean existsByProposedCurriculumNameAndDepartmentIdAndAcademicLevelIdAndCurriculumIsNullAndIsActiveTrue(
            String proposedCurriculumName, Long departmentId, Long academicLevelId);
}