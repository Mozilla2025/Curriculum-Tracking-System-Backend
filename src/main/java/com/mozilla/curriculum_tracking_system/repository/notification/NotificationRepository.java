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

    // Find notifications by recipient
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // Find unread notifications
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // Count unread notifications
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Find notifications by type and recipient
    List<Notification> findByRecipientIdAndType(Long recipientId, NotificationType type);

    // Find notifications that need email sending
    @Query("SELECT n FROM Notification n WHERE n.emailSent = false AND n.scheduledFor <= :currentTime")
    List<Notification> findNotificationsToSendEmail(@Param("currentTime") LocalDateTime currentTime);

    // Find notifications by curriculum
    List<Notification> findByCurriculumIdOrderByCreatedAtDesc(Long curriculumId);

    // Find high priority notifications
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.priority = :priority AND n.isRead = false")
    List<Notification> findHighPriorityNotifications(@Param("recipientId") Long recipientId,
                                                     @Param("priority") NotificationPriority priority);

    // Mark notifications as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id IN :notificationIds")
    void markAsRead(@Param("notificationIds") List<Long> notificationIds, @Param("readAt") LocalDateTime readAt);

    // Mark email as sent
    @Modifying
    @Query("UPDATE Notification n SET n.emailSent = true WHERE n.id = :notificationId")
    void markEmailAsSent(@Param("notificationId") Long notificationId);

    // Find notifications by date range
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findByRecipientAndDateRange(@Param("recipientId") Long recipientId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Delete old notifications (cleanup)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.isRead = true")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}