package com.mozilla.curriculum_tracking_system.dto.notification;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDto {
    private Long id;
    private Long userId;
    private boolean emailNotifications;
    private boolean submissionNotifications;
    private boolean approvalNotifications;
    private boolean assignmentNotifications;
    private boolean overdueReminders;
    private boolean weeklySummary;
    private boolean documentUploadNotifications;
    private int reminderFrequencyHours;
}