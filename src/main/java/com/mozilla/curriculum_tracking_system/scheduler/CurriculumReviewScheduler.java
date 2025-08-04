package com.university.curriculumtracking.scheduler;

import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import com.mozilla.curriculum_tracking_system.mapper.UserMapper;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.service.curriculum.CurriculumService;
import com.mozilla.curriculum_tracking_system.service.notification.NotificationService;
import com.mozilla.curriculum_tracking_system.service.school.ISchoolService;
import com.mozilla.curriculum_tracking_system.service.user.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumReviewScheduler {

    private final CurriculumService curriculumService;
    private final NotificationService notificationService;
    private final UserManagementService userService;
    private final ISchoolService schoolService;
    private final UserMapper userMapper;

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
                            notificationService.sendCurriculumReviewDueNotification(
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
     * Check for delayed curricula - runs every Monday at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * MON")
    public void checkDelayedCurricula() {
        log.info("Starting scheduled check for delayed curricula");

        try {
            List<Curriculum> inProgressCurricula = curriculumService.getCurriculaInProgress();
            LocalDate today = LocalDate.now();

            for (Curriculum curriculum : inProgressCurricula) {
                LocalDate lastActionDate = curriculum.getLastActionDate();

                if (lastActionDate != null) {
                    long daysDelayed = ChronoUnit.DAYS.between(lastActionDate, today);

                    // Send reminder if curriculum has been sitting for more than 7 days
                    if (daysDelayed > 7) {
                        String responsibleEmail = getCurrentResponsibleEmail(curriculum);

                        if (responsibleEmail != null) {
                            notificationService.sendDelayReminderNotification(
                                    responsibleEmail,
                                    curriculum.getName(),
                                    curriculum.getCode(),
                                    curriculum.getCurrentStage(),
                                    (int) daysDelayed
                            );

                            log.info("Delay reminder sent for curriculum: {}", curriculum.getCode());
                        }
                    }
                }
            }

            log.info("Completed scheduled check for delayed curricula");
        } catch (Exception e) {
            log.error("Error during scheduled delayed curricula check", e);
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
            List<String> qaAdminEmails = userService.getQAAdminEmails();

            if (!qaAdminEmails.isEmpty()) {
                // Generate summary data
                Map<String, Object> summaryData = curriculumService.generateWeeklySummary();

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
     * Get the email of the person currently responsible for the curriculum
     */
    private String getCurrentResponsibleEmail(Curriculum curriculum) {
        String currentStage = curriculum.getCurrentStage();

        switch (currentStage) {
            case "SCHOOL_BOARD":
                return userService.getSchoolBoardEmailByDepartment(curriculum.getDepartmentId());
            case "DEAN_COMMITTEE":
                return userService.getDeanEmailByDepartment(curriculum.getDepartmentId());
            case "SENATE":
                return userService.getSenateEmail();
            case "QUALITY_ASSURANCE":
                return userService.getQAEmail();
            case "VICE_CHANCELLOR":
                return userService.getViceChancellorEmail();
            default:
                return null;
        }
    }
}