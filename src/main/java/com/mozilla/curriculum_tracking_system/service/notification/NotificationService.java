package com.mozilla.curriculum_tracking_system.service.notification;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.NotificationPriority;
import com.mozilla.curriculum_tracking_system.enums.NotificationType;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.NotificationMapper;
import com.mozilla.curriculum_tracking_system.model.notification.Notification;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.notification.NotificationRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final IEmailService emailService;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationDto createNotification(NotificationDto notificationDto) {
        log.debug("Creating notification: {}", notificationDto.getTitle());

        // Find user by email or ID
        User user = null;
        if (notificationDto.getUserId() != null) {
            user = userRepository.findById(notificationDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + notificationDto.getUserId()));
        } else if (StringUtils.hasText(notificationDto.getEmail())) {
            user = userRepository.findByEmail(notificationDto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + notificationDto.getEmail()));
        } else {
            throw new IllegalArgumentException("Either userId or email must be provided");
        }

        Notification notification = Notification.builder()
                .title(notificationDto.getTitle())
                .message(notificationDto.getMessage())
                .type(notificationDto.getType())
                .priority(notificationDto.getPriority())
                .user(user)
                .scheduledFor(notificationDto.getScheduledFor() != null ?
                        notificationDto.getScheduledFor() : LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        // Send email if scheduled for now or past
        if (notification.getScheduledFor().isBefore(LocalDateTime.now()) ||
                notification.getScheduledFor().isEqual(LocalDateTime.now())) {
            sendNotificationEmail(notification);
        }

        log.info("Notification created successfully with ID: {}", notification.getId());
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional
    public void sendDelayReminderNotification(String responsibleEmail,
                                              String curriculumName,
                                              String curriculumCode,
                                              CurriculumTrackingStage currentStage,
                                              int daysDelayed) {
        log.info("Sending delay reminder notification for curriculum: {} to {}", curriculumCode, responsibleEmail);

        NotificationDto notificationDto = NotificationDto.builder()
                .title("Curriculum Review Delayed - Action Required")
                .message(String.format("The curriculum '%s (%s)' has been delayed for %d days at the %s stage. " +
                                "Please take immediate action to proceed with the review process.",
                        curriculumName,
                        curriculumCode,
                        daysDelayed,
                        currentStage.getDisplayName()))
                .type(NotificationType.CURRICULUM_DELAY_REMINDER)
                .priority(determinePriorityByDelay(daysDelayed))
                .email(responsibleEmail)
                .scheduledFor(LocalDateTime.now())
                .build();

        createNotification(notificationDto);
    }

    @Override
    @Transactional
    public void sendBulkCurriculumNotifications(List<String> recipientEmails,
                                                String subject,
                                                String templateName,
                                                Map<String, Object> summaryData) {
        log.info("Sending bulk curriculum notifications to {} recipients", recipientEmails.size());

        int successCount = 0;
        int failureCount = 0;

        for (String email : recipientEmails) {
            try {
                NotificationDto notificationDto = NotificationDto.builder()
                        .title(subject)
                        .message(buildSummaryMessage(summaryData, templateName))
                        .type(NotificationType.WEEKLY_SUMMARY)
                        .priority(NotificationPriority.LOW)
                        .email(email)
                        .scheduledFor(LocalDateTime.now())
                        .build();

                createNotification(notificationDto);
                successCount++;
                log.debug("Weekly summary notification sent to: {}", email);

            } catch (Exception e) {
                failureCount++;
                log.error("Failed to send weekly summary notification to: {}", email, e);
            }
        }

        log.info("Bulk curriculum notifications completed. Success: {}, Failures: {}", successCount, failureCount);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        notification.markAsRead();
        notificationRepository.save(notification);

        log.debug("Notification {} marked as read", notificationId);
    }

    @Override
    @Transactional
    public void markMultipleAsRead(List<Long> notificationIds) {
        log.debug("Marking multiple notifications as read: {}", notificationIds.size());

        if (notificationIds.isEmpty()) {
            return;
        }

        List<Notification> notifications = notificationRepository.findAllById(notificationIds);

        notifications.forEach(notification -> {
            if (!notification.getIsRead()) {
                notification.markAsRead();
            }
        });

        notificationRepository.saveAll(notifications);
        log.info("Marked {} notifications as read", notifications.size());
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.debug("Marking all notifications as read for user: {}", userId);

        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalse(userId);

        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(Notification::markAsRead);
            notificationRepository.saveAll(unreadNotifications);
            log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.debug("Deleting notification: {}", notificationId);

        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with ID: " + notificationId);
        }

        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted successfully: {}", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", userId);

        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching unread notifications for user: {}", userId);

        Page<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        log.debug("Counting unread notifications for user: {}", userId);

        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByCurriculum(Long curriculumId) {
        log.debug("Fetching notifications for curriculum: {}", curriculumId);

        List<Notification> notifications = notificationRepository
                .findByCurriculumIdOrderByCreatedAtDesc(curriculumId);

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getHighPriorityNotifications(Long userId) {
        log.debug("Fetching high priority notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository
                .findHighPriorityNotifications(userId, NotificationPriority.HIGH, NotificationPriority.URGENT);

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications");

        LocalDateTime now = LocalDateTime.now();
        List<Notification> scheduledNotifications = notificationRepository
                .findNotificationsToSendEmail(now);

        int processedCount = 0;
        int failureCount = 0;

        for (Notification notification : scheduledNotifications) {
            try {
                sendNotificationEmail(notification);
                notification.markEmailAsSent();
                notificationRepository.save(notification);
                processedCount++;

                log.debug("Processed scheduled notification: {}", notification.getId());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to send scheduled notification email for notification ID: {}",
                        notification.getId(), e);
            }
        }

        if (processedCount > 0 || failureCount > 0) {
            log.info("Processed scheduled notifications. Success: {}, Failures: {}",
                    processedCount, failureCount);
        }
    }

    @Override
    @Transactional
    public void cleanupOldNotifications(int daysOld) {
        log.info("Cleaning up notifications older than {} days", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate);

        log.info("Cleaned up {} old notifications", deletedCount);
    }

    // Helper methods

    private NotificationPriority determinePriorityByDelay(int daysDelayed) {
        if (daysDelayed > 14) {
            return NotificationPriority.URGENT;
        } else if (daysDelayed > 7) {
            return NotificationPriority.HIGH;
        } else if (daysDelayed > 3) {
            return NotificationPriority.MEDIUM;
        } else {
            return NotificationPriority.LOW;
        }
    }

    private String buildSummaryMessage(Map<String, Object> summaryData, String templateName) {
        StringBuilder message = new StringBuilder();

        // Handle different template types
        switch (templateName.toLowerCase()) {
            case "weekly-summary":
                message.append(buildWeeklySummaryMessage(summaryData));
                break;
            case "delay-reminder":
                message.append(buildDelayReminderMessage(summaryData));
                break;
            default:
                message.append(buildGenericSummaryMessage(summaryData));
        }

        return message.toString();
    }

    private String buildWeeklySummaryMessage(Map<String, Object> summaryData) {
        StringBuilder message = new StringBuilder();
        message.append("📊 Weekly Curriculum Tracking Summary Report\n");
        message.append("=" .repeat(50)).append("\n\n");

        // Key metrics
        if (summaryData.containsKey("totalTracked")) {
            message.append("📈 Total Curricula Being Tracked: ").append(summaryData.get("totalTracked")).append("\n");
        }
        if (summaryData.containsKey("completedThisWeek")) {
            message.append("✅ Completed This Week: ").append(summaryData.get("completedThisWeek")).append("\n");
        }
        if (summaryData.containsKey("newTrackingsThisWeek")) {
            message.append("🆕 New Trackings This Week: ").append(summaryData.get("newTrackingsThisWeek")).append("\n");
        }

        // Status breakdown
        if (summaryData.containsKey("statusBreakdown")) {
            message.append("\n📋 Status Breakdown:\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> statusMap = (Map<String, Object>) summaryData.get("statusBreakdown");
            statusMap.forEach((status, count) -> {
                String formattedStatus =
                        formatCamelCase(status);
                message.append("  • ").append(formattedStatus).append(": ").append(count).append("\n");
            });
        }

        // Overdue items
        if (summaryData.containsKey("overdueItems") && (Long) summaryData.get("overdueItems") > 0) {
            message.append("\n⚠️  Overdue Items: ").append(summaryData.get("overdueItems")).append("\n");
        }

        message.append("\n📍 Please review the detailed report in the curriculum tracking system for complete information.\n");
        message.append("\nGenerated on: ").append(LocalDateTime.now().toString());

        return message.toString();
    }

    private String buildDelayReminderMessage(Map<String, Object> summaryData) {
        return "This curriculum requires immediate attention due to delays in the review process.";
    }

    private String buildGenericSummaryMessage(Map<String, Object> summaryData) {
        StringBuilder message = new StringBuilder("Curriculum Tracking Summary:\n\n");

        summaryData.forEach((key, value) -> {
            String formattedKey = formatCamelCase(key);
            message.append(formattedKey).append(": ").append(value).append("\n");
        });

        return message.toString();
    }

    private String formatCamelCase(String input) {
        return input.replaceAll("([A-Z])", " $1")
                .replaceAll("^.", String.valueOf(input.charAt(0)).toUpperCase())
                .trim();
    }

    private void sendNotificationEmail(Notification notification) {
        try {
            NotificationDto notificationDto = notificationMapper.toDto(notification);
            emailService.sendNotificationEmail(notificationDto);
            log.debug("Notification email sent successfully for notification ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification email for notification ID: {}",
                    notification.getId(), e);
            throw e; // Re-throw to handle in calling method
        }
    }

}