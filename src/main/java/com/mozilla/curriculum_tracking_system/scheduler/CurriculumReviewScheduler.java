package com.mozilla.curriculum_tracking_system.scheduler;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDto;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumTrackingMapper;
import com.mozilla.curriculum_tracking_system.mapper.UserMapper;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.curriculum.CurriculumService;
import com.mozilla.curriculum_tracking_system.service.notification.NotificationService;
import com.mozilla.curriculum_tracking_system.service.school.ISchoolService;
import com.mozilla.curriculum_tracking_system.service.tracking.CurriculumTrackingService;
import com.mozilla.curriculum_tracking_system.service.tracking.ICurriculumTrackingNotificationService;
import com.mozilla.curriculum_tracking_system.service.user.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumReviewScheduler {

    private final CurriculumService curriculumService;
    private final CurriculumTrackingService curriculumTrackingService;
    private final CurriculumTrackingRepository trackingRepository;
    private final CurriculumTrackingMapper trackingMapper;
    private final NotificationService notificationService;
    private final ICurriculumTrackingNotificationService trackingNotificationService;
    private final UserManagementService userService;
    private final ISchoolService schoolService;
    private final UserMapper userMapper;
    private final CurriculumTrackingDto curriculumTrackingDto;

    /**
     * Check for curricula due for review - runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkCurriculaForReview() {
        log.info("Starting scheduled check for curricula due for review");

        try {
            List<Curriculum> allCurricula = curriculumService.getAllActiveCurricula();
            LocalDate today = LocalDate.now();

            for (Curriculum curriculum : allCurricula) {
                LocalDateTime lastReviewDate = curriculum.getApprovedAt();
                int reviewCycle = curriculum.getReviewCycleYears(); // 4 or 5 years

                if (lastReviewDate != null) {
                    long yearsElapsed = ChronoUnit.YEARS.between(lastReviewDate, today);

                    // Check if curriculum is due for review
                    if (yearsElapsed >= reviewCycle) {
                        User schoolDean = userMapper.toEntity(schoolService.getSchoolDean(curriculum.getSchool().getId()));
                        String deanEmail = userService.getDeanEmailBySchool(curriculum.getSchool().getId());

                        if (schoolDean != null) {
                            trackingNotificationService.sendCurriculumReviewDueNotification(
                                    schoolDean,
                                    curriculum
                            );

                            // Update curriculum status to indicate review is needed
                            curriculum.putDueForReview();

                            log.info("Review due notification sent for curriculum: {}", curriculum.getCode());
                        } else {
                            log.warn("Dean email not found for curriculum: {}", curriculum.getCode());
                        }
                    }
                }
            }

            log.info("Completed scheduled check for curricula due for review");
        } catch (Exception e) {
            log.error("Error during scheduled curriculum review check", e);
        }
    }

    /**
     * Send daily overdue reminders at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void sendDailyOverdueReminders() {
        log.info("Starting daily overdue reminders job");
        try {
            trackingNotificationService.sendOverdueReminders();
            log.info("Daily overdue reminders completed successfully");
        } catch (Exception e) {
            log.error("Failed to send daily overdue reminders", e);
        }
    }

    /**
     * Weekly summary report - runs every Friday at 5 PM
     */
    @Scheduled(cron = "0 0 17 * * FRI")
    public void sendWeeklySummaryReport() {
        log.info("Starting weekly curriculum summary report");

        try {
            // Get QA admin emails
            List<UserResponse> qaAdmins = userService.getUsersByRole("QA_ADMIN");
            List<String> qaAdminEmails = qaAdmins.stream()
                    .map(UserResponse::getEmail)
                    .collect(Collectors.toList());

            if (!qaAdminEmails.isEmpty()) {
                // Generate summary data
                Map<String, Object> summaryData = curriculumTrackingService. generateWeeklySummary();

                // Send bulk email to QA admins
                notificationService.sendBulkCurriculumNotifications(
                        qaAdminEmails,
                        "Weekly Curriculum Tracking Summary",
                        "weekly-summary",
                        summaryData
                );

                log.info("Weekly summary report sent to {} QA admins", qaAdminEmails.size());
            }
        } catch (Exception e) {
            log.error("Error sending weekly summary report", e);
        }
    }

    /**
     * Process scheduled notifications every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void processScheduledNotifications() {
        try {
            notificationService.processScheduledNotifications();
        } catch (Exception e) {
            log.error("Failed to process scheduled notifications", e);
        }
    }

    /**
     * Clean up old notifications monthly (1st of each month at 2:00 AM)
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void cleanupOldNotifications() {
        log.info("Starting monthly notification cleanup");

        try {
            notificationService.cleanupOldNotifications(90); // Remove read notifications older than 90 days
            log.info("Monthly notification cleanup completed successfully");
        } catch (Exception e) {
            log.error("Failed to cleanup old notifications", e);
        }
    }

    /**
     * Send deadline approaching reminders every day at 8 AM
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDeadlineApproachingReminders() {
        log.info("Starting deadline approaching reminders task");

        try {
            List<CurriculumTrackingDto> expiringSoon = curriculumTrackingService.getExpiringSoonTrackings(3);

            LocalDateTime today = LocalDateTime.now();

            for (CurriculumTrackingDto tracking : expiringSoon) {

                LocalDateTime lastActionDate = tracking.getLastUpdatedAt();

                if (lastActionDate != null) {
                    long days = ChronoUnit.DAYS.between(tracking.getEstimatedCompletionDate().toLocalDate(), today.toLocalDate());

                    // Send reminder if the 'expiry date' is in less than 7 days
                    if (days < 7) {

                        if (tracking.getCurrentAssigneeEmail() != null) {
                            notificationService.sendDelayReminderNotification(
                                    tracking.getCurrentAssigneeEmail(),
                                    tracking.getCurriculumName(),
                                    tracking.getCurriculumCode(),
                                    tracking.getCurrentStage(),
                                    (int) days
                            );
                        }
                    }
                }
            }

            log.info("Deadline approaching reminders sent for {} trackings", expiringSoon.size());
        }catch (Exception e) {
            log.error("Failed to send deadline approaching reminders", e);
        }
    }

}