package com.mozilla.curriculum_tracking_system.service.notification;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.entity.CurriculumReview;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface INotificationService {

    // Core notification operations
    NotificationDto createNotification(NotificationDto notificationDto);
    void markAsRead(Long notificationId);
    void markMultipleAsRead(List<Long> notificationIds);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId);

    // Curriculum-specific notifications
    NotificationDto createCurriculumDueNotification(Curriculum curriculum, Long recipientId,
                                                    String recipientEmail, String recipientName);

    NotificationDto createReminderNotification(CurriculumReview curriculumReview,
                                               Long recipientId, String recipientEmail, String recipientName);
    NotificationDto createOverdueNotification(CurriculumReview curriculumReview,
                                              Long recipientId, String recipientEmail, String recipientName);
    NotificationDto createStatusUpdateNotification(CurriculumReview curriculumReview,
                                                   Long recipientId, String recipientEmail, String recipientName,
                                                   String statusUpdate);

    // Retrieval operations
    Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable);
    Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable);
    long getUnreadNotificationCount(Long userId);
    List<NotificationDto> getNotificationsByCurriculum(Long curriculumId);
    List<NotificationDto> getHighPriorityNotifications(Long userId);

    // Scheduled operations
    void processScheduledNotifications();
    void cleanupOldNotifications(int daysOld);
}