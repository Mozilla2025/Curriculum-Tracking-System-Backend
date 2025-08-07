package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.dto.notification.NotificationPreferencesDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.NotificationPriority;
import com.mozilla.curriculum_tracking_system.enums.NotificationType;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.model.notification.NotificationPreferences;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.notification.NotificationPreferencesRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumTrackingNotificationService implements ICurriculumTrackingNotificationService{

    private final ICurriculumTrackingService curriculumTrackingService;
    private final UserRepository userRepository;
    private final INotificationService notificationService;
    private final NotificationPreferencesRepository notificationPreferencesRepository;

    @Override
    @Transactional
    public void sendSubmissionNotification(Long trackingId, Long assigneeId) {
        log.info("Sending submission notification for tracking ID: {} to assignee: {}", trackingId, assigneeId);

        try {
            CurriculumTrackingDto tracking = curriculumTrackingService.getCurriculumTrackingById(trackingId);
            User assignee = findUserById(assigneeId);

            // Check preferences
            NotificationPreferences preferences = getOrCreatePreferences(assigneeId);
            if (!preferences.isSubmissionNotifications()) {
                log.debug("Submission notifications disabled for user: {}", assigneeId);
                return;
            }

            NotificationDto notificationDto = NotificationDto.builder()
                    .title("New Curriculum Assignment - Action Required")
                    .message(String.format("You have been assigned to review the curriculum '%s' at the %s stage. " +
                                    "Please review the curriculum materials and take appropriate action.",
                            tracking.getCurriculumName(),
                            tracking.getCurrentStage().getDisplayName()))
                    .type(NotificationType.CURRICULUM_SUBMITTED)
                    .priority(NotificationPriority.HIGH)
                    .userId(assigneeId)
                    .email(assignee.getEmail())
                    .username(assignee.getUsername())
                    .curriculumName(tracking.getCurriculumName())
                    .scheduledFor(LocalDateTime.now())
                    .build();

            notificationService.createNotification(notificationDto);
            log.info("Submission notification sent successfully for tracking: {}", trackingId);

        } catch (Exception e) {
            log.error("Failed to send submission notification for tracking: {}", trackingId, e);
        }
    }

    @Override
    @Transactional
    public void sendApprovalNotification(Long trackingId) {
        log.info("Sending approval notification for tracking ID: {}", trackingId);

        try {
            CurriculumTrackingDto tracking = curriculumTrackingService.getCurriculumTrackingById(trackingId);

            NotificationPreferences initiatorPreferences = getOrCreatePreferences(tracking.getInitiatedBy());
            if (!initiatorPreferences.isApprovalNotifications()) {
                log.debug("Approval notifications disabled for user: {}", tracking.getInitiatedBy());
                return;
            }

            // Notify the initiator
            if (tracking.getInitiatedByEmail() != null) {
                NotificationDto initiatorNotification = NotificationDto.builder()
                        .title("Curriculum Approved - Moving Forward")
                        .message(String.format("Good news! The curriculum '%s' has been approved at the %s stage " +
                                        "and is moving forward in the accreditation process.",
                                tracking.getCurriculumName(),
                                tracking.getCurrentStage().getDisplayName()))
                        .type(NotificationType.CURRICULUM_APPROVED)
                        .priority(NotificationPriority.MEDIUM)
                        .email(tracking.getInitiatedByEmail())
                        .curriculumName(tracking.getCurriculumName())
                        .scheduledFor(LocalDateTime.now())
                        .build();

                notificationService.createNotification(initiatorNotification);
            }

            NotificationPreferences assigneePreferences = getOrCreatePreferences(tracking.getCurrentAssignee());
            if (!assigneePreferences.isApprovalNotifications()) {
                log.debug("Approval notifications disabled for user: {}", tracking.getCurrentAssignee());
                return;
            }

            // Notify next stage assignee if available
            if (tracking.getCurrentAssigneeEmail() != null) {
                NotificationDto assigneeNotification = NotificationDto.builder()
                        .title("New Curriculum Assignment - Ready for Review")
                        .message(String.format("The curriculum '%s' has been approved and assigned to you " +
                                        "for review at the %s stage.",
                                tracking.getCurriculumName(),
                                tracking.getCurrentStage().getDisplayName()))
                        .type(NotificationType.CURRICULUM_SUBMITTED)
                        .priority(NotificationPriority.HIGH)
                        .email(tracking.getCurrentAssigneeEmail())
                        .curriculumName(tracking.getCurriculumName())
                        .scheduledFor(LocalDateTime.now())
                        .build();

                notificationService.createNotification(assigneeNotification);
            }

            log.info("Approval notifications sent successfully for tracking: {}", trackingId);

        } catch (Exception e) {
            log.error("Failed to send approval notification for tracking: {}", trackingId, e);
        }
    }

    @Override
    @Transactional
    public void sendSentBackNotification(Long trackingId, String comments) {
        log.info("Sending sent back notification for tracking ID: {}", trackingId);

        try {
            CurriculumTrackingDto tracking = curriculumTrackingService.getCurriculumTrackingById(trackingId);

            NotificationDto notificationDto = NotificationDto.builder()
                    .title("Curriculum Sent Back - Revision Required")
                    .message(String.format("The curriculum '%s' has been sent back from the %s stage. " +
                                    "Please review the feedback and make necessary revisions before resubmission.\n\n" +
                                    "Comments: %s",
                            tracking.getCurriculumName(),
                            tracking.getCurrentStage().getDisplayName(),
                            comments != null ? comments : "No specific comments provided."))
                    .type(NotificationType.CURRICULUM_SENT_BACK)
                    .priority(NotificationPriority.HIGH)
                    .email(tracking.getInitiatedByEmail())
                    .curriculumName(tracking.getCurriculumName())
                    .scheduledFor(LocalDateTime.now())
                    .build();

            notificationService.createNotification(notificationDto);
            log.info("Sent back notification sent successfully for tracking: {}", trackingId);

        } catch (Exception e) {
            log.error("Failed to send sent back notification for tracking: {}", trackingId, e);
        }
    }

    @Override
    @Transactional
    public void sendAssignmentNotification(Long trackingId, Long assigneeId) {
        log.info("Sending assignment notification for tracking ID: {} to user: {}", trackingId, assigneeId);

        try {
            CurriculumTrackingDto tracking = curriculumTrackingService.getCurriculumTrackingById(trackingId);
            User assignee = findUserById(assigneeId);

            NotificationPreferences preferences = getOrCreatePreferences(assigneeId);
            if (!preferences.isAssignmentNotifications()) {
                log.debug("Assignment notifications disabled for user: {}", assigneeId);
                return;
            }

            NotificationDto notificationDto = NotificationDto.builder()
                    .title("Curriculum Assignment Updated")
                    .message(String.format("You have been assigned to handle the curriculum '%s' " +
                                    "currently at the %s stage. Please review and take appropriate action.",
                            tracking.getCurriculumName(),
                            tracking.getCurrentStage().getDisplayName()))
                    .type(NotificationType.CURRICULUM_ASSIGNMENT)
                    .priority(NotificationPriority.HIGH)
                    .userId(assigneeId)
                    .email(assignee.getEmail())
                    .username(assignee.getUsername())
                    .curriculumName(tracking.getCurriculumName())
                    .scheduledFor(LocalDateTime.now())
                    .build();

            notificationService.createNotification(notificationDto);
            log.info("Assignment notification sent successfully for tracking: {}", trackingId);

        } catch (Exception e) {
            log.error("Failed to send assignment notification for tracking: {}", trackingId, e);
        }
    }

    @Override
    @Transactional
    public void sendOverdueReminders() {
        log.info("Processing overdue curriculum tracking reminders");

        try {
            List<CurriculumTrackingDto> overdueTrackings = curriculumTrackingService.getOverdueTrackings();

            for (CurriculumTrackingDto tracking : overdueTrackings) {
                try {
                    // Calculate days overdue
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime estimatedCompletion = tracking.getEstimatedCompletionDate();

                    if (estimatedCompletion != null) {
                        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(estimatedCompletion, now);

                        // Send to current assignee if available
                        if (tracking.getCurrentAssigneeEmail() != null) {
                            sendOverdueReminderNotification(
                                    tracking.getCurrentAssigneeEmail(),
                                    tracking.getCurriculumName(),
                                    tracking.getCurriculumCode(),
                                    tracking.getCurrentStage(),
                                    (int) daysOverdue
                            );
                        }

                        // Also notify QA admins for severely overdue items (>14 days)
                        if (daysOverdue > 14) {
                            List<User> qaAdmins = userRepository.findByRolesName("QA_ADMIN");
                            for (User qaAdmin : qaAdmins) {
                                sendOverdueReminderNotification(
                                        qaAdmin.getEmail(),
                                        tracking.getCurriculumName(),
                                        tracking.getCurriculumCode(),
                                        tracking.getCurrentStage(),
                                        (int) daysOverdue
                                );
                            }
                        }
                    }

                } catch (Exception e) {
                    log.warn("Failed to send overdue reminder for tracking {}: {}",
                            tracking.getId(), e.getMessage());
                }
            }

            log.info("Processed overdue reminders for {} trackings", overdueTrackings.size());

        } catch (Exception e) {
            log.error("Failed to process overdue reminders", e);
        }
    }

    @Override
    @Transactional
    public void sendDocumentUploadNotification(Long trackingId, String documentName) {
        log.info("Sending document upload notification for tracking ID: {}", trackingId);

        try {
            CurriculumTrackingDto tracking = curriculumTrackingService.getCurriculumTrackingById(trackingId);

            NotificationPreferences assigneePreferences = getOrCreatePreferences(tracking.getCurrentAssignee());
            if (!assigneePreferences.isDocumentUploadNotifications()) {
                log.debug("Document upload notifications disabled for user: {}", tracking.getCurrentAssignee());
                return;
            }

            // Notify current assignee
            if (tracking.getCurrentAssigneeEmail() != null) {
                NotificationDto assigneeNotification = NotificationDto.builder()
                        .title("New Document Uploaded")
                        .message(String.format("A new document '%s' has been uploaded for the curriculum '%s'. " +
                                        "Please review the updated materials.",
                                documentName,
                                tracking.getCurriculumName()))
                        .type(NotificationType.DOCUMENT_UPLOADED)
                        .priority(NotificationPriority.MEDIUM)
                        .email(tracking.getCurrentAssigneeEmail())
                        .curriculumName(tracking.getCurriculumName())
                        .scheduledFor(LocalDateTime.now())
                        .build();

                notificationService.createNotification(assigneeNotification);
            }

            NotificationPreferences initiatorPreferences = getOrCreatePreferences(tracking.getInitiatedBy());
            if (!initiatorPreferences.isDocumentUploadNotifications()) {
                log.debug("Document upload notifications disabled for user: {}", tracking.getInitiatedBy());
                return;
            }

            // Notify initiator
            if (tracking.getInitiatedByEmail() != null &&
                    !tracking.getInitiatedByEmail().equals(tracking.getCurrentAssigneeEmail())) {

                NotificationDto initiatorNotification = NotificationDto.builder()
                        .title("Document Upload Confirmation")
                        .message(String.format("Document '%s' has been successfully uploaded " +
                                        "for curriculum '%s'.",
                                documentName,
                                tracking.getCurriculumName()))
                        .type(NotificationType.DOCUMENT_UPLOADED)
                        .priority(NotificationPriority.LOW)
                        .email(tracking.getInitiatedByEmail())
                        .curriculumName(tracking.getCurriculumName())
                        .scheduledFor(LocalDateTime.now())
                        .build();

                notificationService.createNotification(initiatorNotification);
            }

            log.info("Document upload notifications sent successfully for tracking: {}", trackingId);

        } catch (Exception e) {
            log.error("Failed to send document upload notification for tracking: {}", trackingId, e);
        }
    }

    @Override
    @Transactional
    public void sendAccreditationNotification(Long trackingId) {
        log.info("Sending accreditation notification for tracking ID: {}", trackingId);

        try {
            CurriculumTrackingDto tracking = curriculumTrackingService.getCurriculumTrackingById(trackingId);

            // Notify initiator
            if (tracking.getInitiatedByEmail() != null) {
                NotificationDto initiatorNotification = NotificationDto.builder()
                        .title("🎉 Curriculum Successfully Accredited!")
                        .message(String.format("Congratulations! The curriculum '%s' has been successfully " +
                                        "accredited and completed the full approval process. " +
                                        "The curriculum is now officially approved and ready for implementation.",
                                tracking.getCurriculumName()))
                        .type(NotificationType.CURRICULUM_ACCREDITED)
                        .priority(NotificationPriority.HIGH)
                        .email(tracking.getInitiatedByEmail())
                        .curriculumName(tracking.getCurriculumName())
                        .scheduledFor(LocalDateTime.now())
                        .build();

                notificationService.createNotification(initiatorNotification);
            }

            // Notify all QA admins of successful accreditation
            List<User> qaAdmins = userRepository.findByRolesName("QA_ADMIN");
            for (User qaAdmin : qaAdmins) {
                NotificationDto qaNotification = NotificationDto.builder()
                        .title("Curriculum Accreditation Complete")
                        .message(String.format("The curriculum '%s' has been successfully accredited " +
                                        "and completed the approval process.",
                                tracking.getCurriculumName()))
                        .type(NotificationType.CURRICULUM_ACCREDITED)
                        .priority(NotificationPriority.MEDIUM)
                        .userId(qaAdmin.getId())
                        .email(qaAdmin.getEmail())
                        .username(qaAdmin.getUsername())
                        .curriculumName(tracking.getCurriculumName())
                        .scheduledFor(LocalDateTime.now())
                        .build();

                notificationService.createNotification(qaNotification);
            }

            log.info("Accreditation notifications sent successfully for tracking: {}", trackingId);

        } catch (Exception e) {
            log.error("Failed to send accreditation notification for tracking: {}", trackingId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesDto getUserNotificationPreferences(Long userId) {
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        return NotificationPreferencesDto.builder()
                .id(preferences.getId())
                .userId(preferences.getUserId())
                .emailNotifications(preferences.isEmailNotifications())
                .submissionNotifications(preferences.isSubmissionNotifications())
                .approvalNotifications(preferences.isApprovalNotifications())
                .assignmentNotifications(preferences.isAssignmentNotifications())
                .overdueReminders(preferences.isOverdueReminders())
                .weeklySummary(preferences.isWeeklySummary())
                .documentUploadNotifications(preferences.isDocumentUploadNotifications())
                .reminderFrequencyHours(preferences.getReminderFrequencyHours())
                .build();
    }

    @Override
    @Transactional
    public void updateUserNotificationPreferences(Long userId, NotificationPreferencesDto preferencesDto) {
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        preferences.setEmailNotifications(preferencesDto.isEmailNotifications());
        preferences.setSubmissionNotifications(preferencesDto.isSubmissionNotifications());
        preferences.setApprovalNotifications(preferencesDto.isApprovalNotifications());
        preferences.setAssignmentNotifications(preferencesDto.isAssignmentNotifications());
        preferences.setOverdueReminders(preferencesDto.isOverdueReminders());
        preferences.setWeeklySummary(preferencesDto.isWeeklySummary());
        preferences.setDocumentUploadNotifications(preferencesDto.isDocumentUploadNotifications());
        preferences.setReminderFrequencyHours(preferencesDto.getReminderFrequencyHours());

        notificationPreferencesRepository.save(preferences);
        log.info("Updated notification preferences for user: {}", userId);
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
        return notificationService.createNotification(notificationDto);
    }

    // Helper methods
    private NotificationPreferences getOrCreatePreferences(Long userId) {
        return notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    private NotificationPreferences createDefaultPreferences(Long userId) {
        NotificationPreferences preferences = NotificationPreferences.builder()
                .userId(userId)
                .emailNotifications(true)
                .submissionNotifications(true)
                .approvalNotifications(true)
                .assignmentNotifications(true)
                .overdueReminders(true)
                .weeklySummary(true)
                .documentUploadNotifications(false)
                .reminderFrequencyHours(24)
                .build();

        return notificationPreferencesRepository.save(preferences);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
