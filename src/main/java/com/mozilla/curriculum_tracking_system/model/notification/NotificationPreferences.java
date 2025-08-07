package com.mozilla.curriculum_tracking_system.model.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "email_notifications")
    private boolean emailNotifications = true;

    @Column(name = "submission_notifications")
    private boolean submissionNotifications = true;

    @Column(name = "approval_notifications")
    private boolean approvalNotifications = true;

    @Column(name = "assignment_notifications")
    private boolean assignmentNotifications = true;

    @Column(name = "overdue_reminders")
    private boolean overdueReminders = true;

    @Column(name = "weekly_summary")
    private boolean weeklySummary = true;

    @Column(name = "document_upload_notifications")
    private boolean documentUploadNotifications = false;

    @Column(name = "reminder_frequency_hours")
    private int reminderFrequencyHours = 24;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}