package com.mozilla.curriculum_tracking_system.repository.notification;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.enums.NotificationPriority;
import com.mozilla.curriculum_tracking_system.enums.NotificationType;
import com.mozilla.curriculum_tracking_system.model.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // User-based queries
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    // Curriculum-based queries
    List<Notification> findByCurriculumIdOrderByCreatedAtDesc(Long curriculumId);

    // Priority-based queries
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND (n.priority = :priority1 OR n.priority = :priority2) ORDER BY n.createdAt DESC")
    List<Notification> findHighPriorityNotifications(@Param("userId") Long userId,
                                                     @Param("priority1") NotificationPriority priority1,
                                                     @Param("priority2") NotificationPriority priority2);

    // Scheduled notifications
    @Query("SELECT n FROM Notification n WHERE n.scheduledFor <= :currentTime AND n.isEmailSent = false")
    List<Notification> findNotificationsToSendEmail(@Param("currentTime") LocalDateTime currentTime);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Additional useful queries
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.type = :type")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt >= :startDate ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}