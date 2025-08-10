package com.mozilla.curriculum_tracking_system.model.notification;

import com.mozilla.curriculum_tracking_system.enums.NotificationPriority;
import com.mozilla.curriculum_tracking_system.enums.NotificationType;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;

    // Fixed the relationship - should be ManyToOne, not OneToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Optional relationship to curriculum - can be null for general notifications
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_email_sent", nullable = false)
    @Builder.Default
    private Boolean isEmailSent = false;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Convenience methods to access user information
    public Long getRecipientId() {
        return user != null ? user.getId() : null;
    }

    public String getRecipientEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getRecipientName() {
        return user != null ? user.getFirstName() + " " + user.getLastName() : null;
    }

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (scheduledFor == null) {
            scheduledFor = LocalDateTime.now();
        }
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markEmailAsSent() {
        this.isEmailSent = true;
    }

    public boolean isScheduledToSend() {
        return scheduledFor != null &&
                (scheduledFor.isBefore(LocalDateTime.now()) || scheduledFor.isEqual(LocalDateTime.now())) &&
                !isEmailSent;
    }
}