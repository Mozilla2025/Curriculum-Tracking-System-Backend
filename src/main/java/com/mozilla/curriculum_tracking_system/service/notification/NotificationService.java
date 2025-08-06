package com.mozilla.curriculum_tracking_system.service.notification;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.NotificationPriority;
import com.mozilla.curriculum_tracking_system.enums.NotificationType;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.notification.Notification;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.notification.NotificationRepository;
import com.mozilla.curriculum_tracking_system.service.email.IEmailService;
import com.mozilla.curriculum_tracking_system.service.tracking.ICurriculumTrackingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
 public class NotificationService implements INotificationService, ICurriculumTrackingNotificationService {

    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    @Override
    @Transactional
    public NotificationDto createNotification(NotificationDto notificationDto) {

        User user = User.builder()
                .id(notificationDto.getUserId())
                .email(notificationDto.getEmail())
                .username(notificationDto.getUsername())
                .build();

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

        return convertToDto(notification);
    }

    @Override
    @Transactional
    public NotificationDto sendCurriculumReviewDueNotification(User schoolDean,
                                                         Curriculum curriculum) {

        NotificationDto notificationDto = NotificationDto.builder()
                .title("Curriculum Review Due")
                .message("The curriculum '" + curriculum.getName() +
                        "' is due for review. Please begin the review process.")
                .type(NotificationType.CURRICULUM_DUE_FOR_REVIEW)
                .priority(NotificationPriority.HIGH)
                .curriculumName(curriculum.getName())
                .userId(schoolDean.getId())
                .email(schoolDean.getEmail())
                .username(schoolDean.getUsername())
                .build();

        return createNotification(notificationDto);
    }

    /**
     * Send delay reminder notification for curriculum tracking
     */
    @Override
    @Transactional
    public NotificationDto sendDelayReminderNotification(String responsibleEmail,
                                                         String curriculumName,
                                                         String curriculumCode,
                                                         CurriculumTrackingStage currentStage,
                                                         int daysDelayed) {

        NotificationDto notificationDto = NotificationDto.builder()
                .title("Curriculum Review Delayed - Action Required")
                .message(String.format("The curriculum '%s (%s)' has been delayed for %d days at the %s stage. " +
                                "Please take immediate action to proceed with the review process.",
                        curriculumName,
                        curriculumCode,
                        daysDelayed,
                        currentStage.getDisplayName()))
                .type(NotificationType.CURRICULUM_DELAY_REMINDER)
                .priority(daysDelayed > 14 ? NotificationPriority.URGENT :
                        daysDelayed > 7 ? NotificationPriority.HIGH : NotificationPriority.MEDIUM)
                .email(responsibleEmail)
                .curriculumName(curriculumName)
                .scheduledFor(LocalDateTime.now())
                .build();

        log.info("Sending delay reminder notification for curriculum: {} to {}", curriculumCode, responsibleEmail);
        return createNotification(notificationDto);
    }

    /**
     * Send overdue notification for curriculum tracking
     */
    @Override
    @Transactional
    public NotificationDto sendOverdueReminderNotification(String responsibleEmail,
                                                   String curriculumName,
                                                   String curriculumCode,
                                                   CurriculumTrackingStage currentStage,
                                                   int daysOverdue) {

        NotificationDto notificationDto = NotificationDto.builder()
                .title("OVERDUE: Curriculum Review - Urgent Action Required")
                .message(String.format("URGENT: The curriculum '%s (%s)' is overdue by %d days at the %s stage. " +
                                "This curriculum has exceeded its estimated completion date. " +
                                "Please prioritize this review immediately.",
                        curriculumName,
                        curriculumCode,
                        daysOverdue,
                        currentStage.getDisplayName()))
                .type(NotificationType.CURRICULUM_OVERDUE)
                .priority(NotificationPriority.URGENT)
                .email(responsibleEmail)
                .curriculumName(curriculumName)
                .scheduledFor(LocalDateTime.now())
                .build();

        log.warn("Sending overdue notification for curriculum: {} to {} - {} days overdue",
                curriculumCode, responsibleEmail, daysOverdue);
        return createNotification(notificationDto);
    }

    /**
     * Send bulk curriculum notifications (for weekly summary)
     */
    @Override
    @Transactional
    public void sendBulkCurriculumNotifications(List<String> recipientEmails,
                                                String subject,
                                                String templateName,
                                                Map<String, Object> summaryData) {

        for (String email : recipientEmails) {
            try {
                NotificationDto notificationDto = NotificationDto.builder()
                        .title(subject)
                        .message(buildSummaryMessage(summaryData))
                        .type(NotificationType.WEEKLY_SUMMARY)
                        .priority(NotificationPriority.LOW)
                        .email(email)
                        .scheduledFor(LocalDateTime.now())
                        .build();

                createNotification(notificationDto);
                log.debug("Weekly summary notification sent to: {}", email);

            } catch (Exception e) {
                log.error("Failed to send weekly summary notification to: {}", email, e);
            }
        }

        log.info("Bulk curriculum notifications sent to {} recipients", recipientEmails.size());
    }

    @Override
    @Transactional
    public NotificationDto createStatusUpdateNotification(Curriculum curriculum,
                                                          String recipientEmail, String recipientName,
                                                          String statusUpdate) {
        NotificationDto notificationDto = NotificationDto.builder()
                .title("Curriculum Status Update")
                .message("Status update for '" + curriculum.getName() + "': " + statusUpdate)
                .type(NotificationType.REVIEW_SUBMITTED)
                .priority(NotificationPriority.MEDIUM)
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .build();

        return createNotification(notificationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markMultipleAsRead(List<Long> notificationIds) {
        notificationRepository.markAsRead(notificationIds, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadFalse(null, null)
                .stream()
                .filter(n -> n.getRecipientId().equals(userId))
                .toList();

        List<Long> notificationIds = unreadNotifications.stream()
                .map(Notification::getId)
                .toList();

        if (!notificationIds.isEmpty()) {
            markMultipleAsRead(notificationIds);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void processScheduledNotifications() {
        List<Notification> scheduledNotifications = notificationRepository
                .findNotificationsToSendEmail(LocalDateTime.now());

        for (Notification notification : scheduledNotifications) {
            try {
                sendNotificationEmail(notification);
                notification.markEmailAsSent();
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.error("Failed to send scheduled notification email for notification ID: {}",
                        notification.getId(), e);
            }
        }
    }

    @Override
    @Transactional
    public void cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldReadNotifications(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByCurriculum(Long curriculumId) {
        List<Notification> notifications = notificationRepository.findByCurriculumIdOrderByCreatedAtDesc(curriculumId);
        return notifications.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getHighPriorityNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
                .findHighPriorityNotifications(userId, NotificationPriority.HIGH);
        return notifications.stream().map(this::convertToDto).toList();
    }

    private void sendNotificationEmail(Notification notification) {
        try {
            NotificationDto notificationDto = convertToDto(notification);
            emailService.sendNotificationEmail(notificationDto);
            log.info("Notification email sent successfully for notification ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification email for notification ID: {}",
                    notification.getId(), e);
        }
    }

    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .priority(notification.getPriority())
                .recipientEmail(notification.getRecipientEmail())
                .recipientName(notification.getRecipientName())
                .isRead(notification.getIsRead())
                .isEmailSent(notification.getIsEmailSent())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .scheduledFor(notification.getScheduledFor())
                .build();
    }


    @Override
    public NotificationDto createStatusUpdateNotification(CurriculumReview curriculumReview, Long recipientId, String recipientEmail, String recipientName, String statusUpdate) {
        return null;
    }

    @Override
    public void sendSubmissionNotification(Long trackingId, Long assigneeId) {

    }

    @Override
    public void sendApprovalNotification(Long trackingId) {

    }

    @Override
    public void sendSentBackNotification(Long trackingId, String comments) {

    }

    @Override
    public void sendAssignmentNotification(Long trackingId, Long assigneeId) {

    }

    @Override
    public void sendOverdueReminders() {

    }

    @Override
    public void sendDocumentUploadNotification(Long trackingId, String documentName) {

    }

    @Override
    public void sendAccreditationNotification(Long trackingId) {

    }

    @Override
    public Object getUserNotificationPreferences(Long userId) {
        return null;
    }

    @Override
    public void updateUserNotificationPreferences(Long userId, Object preferences) {

    }
}