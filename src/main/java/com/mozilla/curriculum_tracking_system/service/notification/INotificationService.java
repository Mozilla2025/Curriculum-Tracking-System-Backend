package com.mozilla.curriculum_tracking_system.service.notification;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface INotificationService {

    // Core notification operations
    NotificationDto createNotification(NotificationDto notificationDto);

    void sendDelayReminderNotification(String responsibleEmail,
                                                  String curriculumName,
                                                  String curriculumCode,
                                                  CurriculumTrackingStage currentStage,
                                                  int daysDelayed);

    void sendBulkCurriculumNotifications(List<String> recipientEmails,
                                         String subject,
                                         String templateName,
                                         Map<String, Object> summaryData);

    void markAsRead(Long notificationId);
    void markMultipleAsRead(List<Long> notificationIds);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId);


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