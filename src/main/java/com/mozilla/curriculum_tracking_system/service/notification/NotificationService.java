package com.mozilla.curriculum_tracking_system.service.notification;

import com.mozilla.curriculum_tracking_system.dto.email.EmailRequest;
import com.mozilla.curriculum_tracking_system.service.email.IEmailService;
import com.mozilla.curriculum_tracking_system.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final IEmailService emailService;
    private final INotificationService notificationService; // For in-app notifications

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String supportEmail;

    @Value("${app.email.from-name:Mozilla Foundation}")
    private String companyName;

    /**
     * Send curriculum review due notification to Dean
     */
    public void sendCurriculumReviewDueNotification(String deanEmail, String deanName,
                                                    String curriculumName, String curriculumCode,
                                                    int yearsElapsed, Long curriculumId) {
        try {
            Map<String, Object> variables = createBaseVariables();
            variables.put("curriculumName", curriculumName);
            variables.put("curriculumCode", curriculumCode);
            variables.put("yearsElapsed", yearsElapsed);
            variables.put("deanName", deanName != null ? deanName : "Dean");
            variables.put("loginUrl", frontendUrl + "/curricula/" + curriculumId);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(deanEmail)
                    .subject("📚 Curriculum Review Due: " + curriculumName)
                    .templateName("curriculum-review-due")
                    .variables(variables)
                    .isHtml(true)
                    .build();

            emailService.sendEmail(emailRequest);

            // In-app notification
            String message = String.format("Curriculum '%s' (%s) is due for review after %d years",
                    curriculumName, curriculumCode, yearsElapsed);
            notificationService.createNotification(deanEmail, "CURRICULUM_REVIEW_DUE", message, curriculumId);

            log.info("Review due notification sent for curriculum: {}", curriculumCode);
        } catch (Exception e) {
            log.error("Failed to send curriculum review due notification for: {}", curriculumCode, e);
        }
    }

    /**
     * Send curriculum status update notification
     */
    public void sendCurriculumStatusUpdateNotification(String recipientEmail, String curriculumName,
                                                       String curriculumCode, String currentStage,
                                                       String nextStage, String comments, Long curriculumId) {
        try {
            Map<String, Object> variables = createBaseVariables();
            variables.put("curriculumName", curriculumName);
            variables.put("curriculumCode", curriculumCode);
            variables.put("currentStage", currentStage);
            variables.put("nextStage", nextStage);
            variables.put("comments", comments);
            variables.put("loginUrl", frontendUrl + "/curricula/" + curriculumId);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(recipientEmail)
                    .subject("📋 Curriculum Status Update: " + curriculumName)
                    .templateName("curriculum-status-update")
                    .variables(variables)
                    .isHtml(true)
                    .build();

            emailService.sendEmail(emailRequest);

            // In-app notification
            String message = String.format("Curriculum '%s' has moved to %s stage",
                    curriculumName, nextStage);
            notificationService.createNotification(recipientEmail, "CURRICULUM_STATUS_UPDATE", message, curriculumId);

            log.info("Status update notification sent for curriculum: {}", curriculumCode);
        } catch (Exception e) {
            log.error("Failed to send curriculum status update notification for: {}", curriculumCode, e);
        }
    }

    /**
     * Send action required notification
     */
    public void sendActionRequiredNotification(String recipientEmail, String curriculumName,
                                               String curriculumCode, String actionRequired,
                                               String stage, Long curriculumId) {
        try {
            Map<String, Object> variables = createBaseVariables();
            variables.put("curriculumName", curriculumName);
            variables.put("curriculumCode", curriculumCode);
            variables.put("actionRequired", actionRequired);
            variables.put("stage", stage);
            variables.put("loginUrl", frontendUrl + "/curricula/" + curriculumId);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(recipientEmail)
                    .subject("⚠️ Action Required: " + curriculumName)
                    .templateName("action-required")
                    .variables(variables)
                    .isHtml(true)
                    .build();

            emailService.sendEmail(emailRequest);

            // In-app notification
            String message = String.format("Action required for curriculum '%s' at %s stage",
                    curriculumName, stage);
            notificationService.createNotification(recipientEmail, "ACTION_REQUIRED", message, curriculumId);

            log.info("Action required notification sent for curriculum: {}", curriculumCode);
        } catch (Exception e) {
            log.error("Failed to send action required notification for: {}", curriculumCode, e);
        }
    }

    /**
     * Send reminder notification for delayed curricula
     */
    public void sendDelayReminderNotification(String recipientEmail, String curriculumName,
                                              String curriculumCode, String stage, int daysDelayed,
                                              Long curriculumId) {
        try {
            Map<String, Object> variables = createBaseVariables();
            variables.put("curriculumName", curriculumName);
            variables.put("curriculumCode", curriculumCode);
            variables.put("stage", stage);
            variables.put("daysDelayed", daysDelayed);
            variables.put("loginUrl", frontendUrl + "/curricula/" + curriculumId);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(recipientEmail)
                    .subject("⏰ Reminder: Curriculum Review Delayed - " + curriculumName)
                    .templateName("delay-reminder")
                    .variables(variables)
                    .isHtml(true)
                    .build();

            emailService.sendEmail(emailRequest);

            // In-app notification
            String message = String.format("Curriculum '%s' has been delayed %d days at %s stage",
                    curriculumName, daysDelayed, stage);
            notificationService.createNotification(recipientEmail, "DELAY_REMINDER", message, curriculumId);

            log.info("Delay reminder notification sent for curriculum: {}", curriculumCode);
        } catch (Exception e) {
            log.error("Failed to send delay reminder notification for: {}", curriculumCode, e);
        }
    }

    /**
     * Send CUE reminder notification to Vice Chancellor
     */
    public void sendCUEReminderToVC(String vcEmail, String curriculumName, String curriculumCode,
                                    LocalDate dateSentToCUE, int daysPending, Long curriculumId) {
        try {
            Map<String, Object> variables = createBaseVariables();
            variables.put("curriculumName", curriculumName);
            variables.put("curriculumCode", curriculumCode);
            variables.put("dateSentToCUE", dateSentToCUE.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            variables.put("daysPending", daysPending);
            variables.put("loginUrl", frontendUrl + "/curricula/" + curriculumId);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(vcEmail)
                    .subject("🔔 CUE Review Reminder: " + curriculumName)
                    .templateName("cue-reminder-to-vc")
                    .variables(variables)
                    .isHtml(true)
                    .build();

            emailService.sendEmail(emailRequest);

            // In-app notification
            String message = String.format("CUE review for curriculum '%s' has been pending for %d days",
                    curriculumName, daysPending);
            notificationService.createNotification(vcEmail, "CUE_REMINDER", message, curriculumId);

            log.info("CUE reminder notification sent to VC for curriculum: {}", curriculumCode);
        } catch (Exception e) {
            log.error("Failed to send CUE reminder notification for: {}", curriculumCode, e);
        }
    }

    /**
     * Send bulk notifications to multiple recipients
     */
    public void sendBulkCurriculumNotifications(List<String> recipients, String subject,
                                                String templateName, Map<String, Object> variables) {
        try {
            // Add base variables
            Map<String, Object> allVariables = createBaseVariables();
            if (variables != null) {
                allVariables.putAll(variables);
            }

            // Send to each recipient individually for better tracking
            for (String recipient : recipients) {
                EmailRequest emailRequest = EmailRequest.builder()
                        .to(recipient)
                        .subject(subject)
                        .templateName(templateName)
                        .variables(allVariables)
                        .isHtml(true)
                        .build();

                emailService.sendEmail(emailRequest);

                // Create in-app notification
                String message = allVariables.get("message") != null ?
                        allVariables.get("message").toString() : subject;
                notificationService.createNotification(recipient, "BULK_NOTIFICATION", message, null);
            }

            log.info("Bulk curriculum notifications sent to {} recipients", recipients.size());
        } catch (Exception e) {
            log.error("Failed to send bulk curriculum notifications", e);
        }
    }

    /**
     * Create base variables that are common to all email templates
     */
    private Map<String, Object> createBaseVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("loginUrl", frontendUrl + "/login");
        variables.put("supportEmail", supportEmail);
        variables.put("companyName", companyName);
        variables.put("currentYear", java.time.Year.now().getValue());
        return variables;
    }
}