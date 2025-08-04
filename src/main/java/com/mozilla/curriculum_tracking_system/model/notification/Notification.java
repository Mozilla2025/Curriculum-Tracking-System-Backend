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

    @Column(name = "recipient_id")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "curriculum_id")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", unique = true)
    private Curriculum curriculum;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_email_sent", nullable = false)
    private Boolean isEmailSent = false;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Helper methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markEmailAsSent() {
        this.isEmailSent = true;
    }
}

/*
Remove curriculum id and name
 */