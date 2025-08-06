package com.mozilla.curriculum_tracking_system.scheduler;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumTrackingMapper;
import com.mozilla.curriculum_tracking_system.mapper.UserMapper;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.curriculum.CurriculumService;
import com.mozilla.curriculum_tracking_system.service.notification.NotificationService;
import com.mozilla.curriculum_tracking_system.service.school.ISchoolService;
import com.mozilla.curriculum_tracking_system.service.tracking.CurriculumTrackingService;
import com.mozilla.curriculum_tracking_system.service.user.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UserManagementService userService;
    private final ISchoolService schoolService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

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
            List<CurriculumTracking> underReviewEntities = trackingRepository
                    .findByStatusAndIsActive(CurriculumTrackingStatus.UNDER_REVIEW, true);

            // Convert to DTOs using the mapper with user emails
            List<CurriculumTrackingDto> underReviewTrackings = underReviewEntities.stream()
                    .map(tracking -> {
                        User initiatorUser = userRepository.findById(tracking.getInitiatedBy()).orElse(null);
                        User currentAssigneeUser = tracking.getCurrentAssignee() != null ?
                                userRepository.findById(tracking.getCurrentAssignee()).orElse(null) : null;

                        return trackingMapper.toDtoWithUserEmails(
                                tracking,
                                initiatorUser != null ? initiatorUser.getEmail() : null,
                                currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
                        );
                    })
                    .toList();

            LocalDateTime today = LocalDateTime.now();

            for (CurriculumTrackingDto trackingDto : underReviewTrackings) {
                LocalDateTime lastActionDate = trackingDto.getLastUpdatedAt();

                if (lastActionDate != null) {
                    long daysDelayed = ChronoUnit.DAYS.between(lastActionDate.toLocalDate(), today.toLocalDate());

                    // Send reminder if curriculum has been sitting for more than 7 days
                    if (daysDelayed > 7) {
                        String responsibleEmail = trackingDto.getCurrentAssigneeEmail();

                        if (responsibleEmail != null) {
                            notificationService.sendDelayReminderNotification(
                                    responsibleEmail,
                                    trackingDto.getCurriculumName(),
                                    trackingDto.getCurriculumCode(),
                                    trackingDto.getCurrentStage(),
                                    (int) daysDelayed
                            );

                            log.info("Delay reminder sent for curriculum: {}", trackingDto.getCurriculumCode());
                        }
                    }
                }
            }

            log.info("Completed scheduled check for delayed curricula. Processed {} tracking records.", underReviewTrackings.size());
        } catch (Exception e) {
            log.error("Error during scheduled delayed curricula check", e);
        }
    }

    /**
     * Check specifically for overdue curricula
     */
    @Scheduled(cron = "0 30 10 * * MON") // Runs 30 minutes after the delayed check
    public void checkOverdueCurricula() {
        log.info("Starting scheduled check for overdue curricula");

        try {
            List<CurriculumTrackingDto> overdueTrackings = curriculumTrackingService.getOverdueTrackings();

            for (CurriculumTrackingDto trackingDto : overdueTrackings) {
                String responsibleEmail = trackingDto.getCurrentAssigneeEmail();

                if (responsibleEmail != null) {
                    // Calculate how many days overdue
                    LocalDateTime estimatedCompletion = trackingDto.getEstimatedCompletionDate();
                    long daysOverdue = estimatedCompletion != null ?
                            ChronoUnit.DAYS.between(estimatedCompletion.toLocalDate(), LocalDate.now()) : 0;

                    notificationService.sendOverdueReminderNotification(
                            responsibleEmail,
                            trackingDto.getCurriculumName(),
                            trackingDto.getCurriculumCode(),
                            trackingDto.getCurrentStage(),
                            (int) daysOverdue
                    );

                    log.info("Overdue notification sent for curriculum: {}", trackingDto.getCurriculumCode());
                }
            }

            log.info("Completed scheduled check for overdue curricula");
        } catch (Exception e) {
            log.error("Error during scheduled overdue curricula check", e);
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

}