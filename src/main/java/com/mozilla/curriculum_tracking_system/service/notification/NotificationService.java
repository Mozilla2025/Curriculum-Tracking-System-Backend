package com.mozilla.curriculum_tracking_system.service.notification;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.enums.NotificationPriority;
import com.mozilla.curriculum_tracking_system.enums.NotificationType;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.notification.Notification;
import com.mozilla.curriculum_tracking_system.repository.NotificationRepository;
import com.mozilla.curriculum_tracking_system.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    @Override
    @Transactional
    public NotificationDto createNotification(NotificationDto notificationDto) {

        Notification notification = Notification.builder()
                .title(notificationDto.getTitle())
                .message(notificationDto.getMessage())
                .type(notificationDto.getType())
                .priority(notificationDto.getPriority())
                .recipientEmail(notificationDto.getRecipientEmail())
                .recipientName(notificationDto.getRecipientName())
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
    public NotificationDto createCurriculumDueNotification(Curriculum curriculum,
                                                           Long recipientId, String recipientEmail, String recipientName) {

        NotificationDto notificationDto = NotificationDto.builder()
                .title("Curriculum Review Due")
                .message("The curriculum '" + curriculum.getName() +
                        "' is due for review. Please begin the review process.")
                .type(NotificationType.CURRICULUM_DUE_FOR_REVIEW)
                .priority(NotificationPriority.HIGH)
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .build();

        return createNotification(notificationDto);
    }

    @Override
    @Transactional
    public NotificationDto createReminderNotification(Curriculum curriculum, String recipientEmail, String recipientName) {
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(),
                curriculum.getExpiryDate().toLocalDate()
        );

        NotificationDto notificationDto = NotificationDto.builder()
                .title("Curriculum Review Reminder")
                .message("Reminder: The curriculum '" + curriculum.getName() +
                        "' is due for review in " + daysUntilDue + " days.")
                .type(NotificationType.REMINDER)
                .priority(daysUntilDue <= 7 ? NotificationPriority.HIGH :
                        NotificationPriority.MEDIUM)
                .curriculumId(curriculum.getId())
                .curriculumName(curriculum.getName())
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .build();

        return createNotification(notificationDto);
    }

    @Override
    @Transactional
    public NotificationDto createOverdueNotification(Curriculum curriculum,
                                                     String recipientEmail, String recipientName) {
        NotificationDto notificationDto = NotificationDto.builder()
                .title("OVERDUE: Curriculum Review")
                .message("The curriculum '" + curriculum.getName() +
                        "' is overdue for review by " + curriculum.getDaysOverdue() + " days.")
                .type(NotificationType.REVIEW_OVERDUE)
                .priority(NotificationPriority.URGENT)
                .curriculumId(curriculum.getId())
                .curriculumName(curriculum.getName())
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .build();

        return createNotification(notificationDto);
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
}