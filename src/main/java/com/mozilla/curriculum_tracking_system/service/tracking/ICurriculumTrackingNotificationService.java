package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationPreferencesDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.user.User;

/**
 * Service interface for curriculum tracking notifications
 */
public interface ICurriculumTrackingNotificationService {

    /**
     * Send notification when curriculum is submitted to next stage
     */
    void sendSubmissionNotification(Long trackingId, Long assigneeId);

    /**
     * Send notification when curriculum is approved
     */
    void sendApprovalNotification(Long trackingId);

    /**
     * Send notification when curriculum is sent back
     */
    void sendSentBackNotification(Long trackingId, String comments);

    /**
     * Send notification when curriculum is assigned to user
     */
    void sendAssignmentNotification(Long trackingId, Long assigneeId);

    void sendCurriculumReviewDueNotification(User schoolDean,
                                                        Curriculum curriculum);
    /**
     * Send overdue reminder notification
     */
    void sendOverdueReminderNotification(String responsibleEmail,
                                                    String curriculumName,
                                                    String curriculumCode,
                                                    CurriculumTrackingStage currentStage,
                                                    int daysOverdue);

    /**
     * Send bulk notifications for overdue items
     */
    void sendOverdueReminders();

    /**
     * Send notification when document is uploaded
     */
    void sendDocumentUploadNotification(Long trackingId, String documentName);

    /**
     * Send final accreditation notification
     */
    void sendAccreditationNotification(Long trackingId);

    /**
     * Get notification preferences for user
     */
    NotificationPreferencesDto getUserNotificationPreferences(Long userId);

    /**
     * Update notification preferences for user
     */
    void updateUserNotificationPreferences(Long userId, NotificationPreferencesDto preferences);
}
