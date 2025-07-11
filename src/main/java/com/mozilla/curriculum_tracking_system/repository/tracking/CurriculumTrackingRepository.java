package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
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
public interface CurriculumTrackingRepository extends JpaRepository<CurriculumTracking, Long>,
        JpaSpecificationExecutor<CurriculumTracking> {

    // Find by curriculum
    Optional<CurriculumTracking> findByCurriculumId(Long curriculumId);

    boolean existsByCurriculumId(Long curriculumId);

    // Find by stage
    Page<CurriculumTracking> findByCurrentStage(CurriculumTrackingStage currentStage, Pageable pageable);

    @Query("SELECT ct FROM CurriculumTracking ct WHERE ct.currentStage = :stage AND ct.isActive = true")
    List<CurriculumTracking> findActiveByCurrentStage(@Param("stage") CurriculumTrackingStage stage);

    // Find by status
    Page<CurriculumTracking> findByStatus(CurriculumTrackingStatus status, Pageable pageable);

    List<CurriculumTracking> findByStatusAndIsActive(CurriculumTrackingStatus status, boolean isActive);

    // Find by assignee
    Page<CurriculumTracking> findByCurrentAssignee(Long currentAssignee, Pageable pageable);

    @Query("SELECT ct FROM CurriculumTracking ct WHERE ct.currentAssignee = :assigneeId AND ct.isActive = true")
    List<CurriculumTracking> findActiveByCurrentAssignee(@Param("assigneeId") Long assigneeId);

    // Find by initiator
    Page<CurriculumTracking> findByInitiatedBy(Long initiatedBy, Pageable pageable);

    // Combined filters
    @Query("SELECT ct FROM CurriculumTracking ct WHERE " +
            "(:status IS NULL OR ct.status = :status) AND " +
            "(:stage IS NULL OR ct.currentStage = :stage) AND " +
            "(:assigneeId IS NULL OR ct.currentAssignee = :assigneeId) AND " +
            "(:isActive IS NULL OR ct.isActive = :isActive)")
    Page<CurriculumTracking> findByFilters(@Param("status") CurriculumTrackingStatus status,
                                           @Param("stage") CurriculumTrackingStage stage,
                                           @Param("assigneeId") Long assigneeId,
                                           @Param("isActive") Boolean isActive,
                                           Pageable pageable);

    // Find with curriculum details
    @Query("SELECT ct FROM CurriculumTracking ct " +
            "JOIN FETCH ct.curriculum c " +
            "JOIN FETCH c.school s " +
            "JOIN FETCH c.department d " +
            "WHERE ct.id = :id")
    Optional<CurriculumTracking> findByIdWithCurriculumDetails(@Param("id") Long id);

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "JOIN FETCH ct.curriculum c " +
            "JOIN FETCH c.school s " +
            "JOIN FETCH c.department d " +
            "WHERE ct.isActive = true")
    List<CurriculumTracking> findAllActiveWithCurriculumDetails();

    // Find overdue items
    @Query("SELECT ct FROM CurriculumTracking ct " +
            "WHERE ct.estimatedCompletionDate < :currentDate " +
            "AND ct.completedAt IS NULL " +
            "AND ct.isActive = true")
    List<CurriculumTracking> findOverdueTrackings(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "WHERE ct.estimatedCompletionDate BETWEEN :currentDate AND :targetDate " +
            "AND ct.completedAt IS NULL " +
            "AND ct.isActive = true")
    List<CurriculumTracking> findExpiringSoon(@Param("currentDate") LocalDateTime currentDate,
                                              @Param("targetDate") LocalDateTime targetDate);

    long countByStatus(CurriculumTrackingStatus status);

    long countByCurrentStage(CurriculumTrackingStage stage);

    @Query("SELECT COUNT(ct) FROM CurriculumTracking ct WHERE ct.completedAt IS NULL AND ct.isActive = true")
    long countActiveIncomplete();

    @Query("SELECT COUNT(ct) FROM CurriculumTracking ct WHERE ct.completedAt IS NOT NULL")
    long countCompleted();

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "JOIN ct.curriculum c " +
            "WHERE (:searchTerm IS NULL OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(ct.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND ct.isActive = true")
    Page<CurriculumTracking> searchTrackings(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "JOIN ct.curriculum c " +
            "WHERE c.school.id = :schoolId AND ct.isActive = true")
    Page<CurriculumTracking> findBySchoolId(@Param("schoolId") Long schoolId, Pageable pageable);

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "JOIN ct.curriculum c " +
            "WHERE c.department.id = :departmentId AND ct.isActive = true")
    Page<CurriculumTracking> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "WHERE ct.initiatedAt BETWEEN :startDate AND :endDate " +
            "AND ct.isActive = true")
    List<CurriculumTracking> findByInitiatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT AVG(EXTRACT(DAY FROM (ct.completedAt - ct.initiatedAt))) " +
            "FROM CurriculumTracking ct " +
            "WHERE ct.completedAt IS NOT NULL", nativeQuery = true)
    Double findAverageCompletionTimeInDays();

    @Query("SELECT ct FROM CurriculumTracking ct " +
            "WHERE ct.lastUpdatedAt >= :since " +
            "AND ct.isActive = true " +
            "ORDER BY ct.lastUpdatedAt DESC")
    List<CurriculumTracking> findRecentlyUpdated(@Param("since") LocalDateTime since);

    @Query("UPDATE CurriculumTracking ct SET ct.isActive = false, ct.lastUpdatedAt = CURRENT_TIMESTAMP " +
            "WHERE ct.id = :id")
    void softDeleteById(@Param("id") Long id);
}

