package com.mozilla.curriculum_tracking_system.controller.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingStepSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.tracking.ITrackingStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@RestController
@RequestMapping("${api.prefix}/tracking/steps")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TrackingStepController {

    private final ITrackingStepService trackingStepService;

    /**
     * Get all steps for a specific tracking
     */
    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<ApiResponse> getStepsByTracking(
            @PathVariable Long trackingId,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/tracking/{} - pageable: {}", trackingId, pageable);

        var stepsResponse = trackingStepService.getStepsByTracking(trackingId, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get a specific tracking step by ID
     */
    @GetMapping("/{stepId}")
    public ResponseEntity<ApiResponse> getStepById(@PathVariable Long stepId) {
        log.debug("GET /api/v1/tracking/steps/{}", stepId);

        var step = trackingStepService.getStepById(stepId);

        var apiResponse = new ApiResponse(
                "Tracking step retrieved successfully",
                step
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Search tracking steps with criteria
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse> searchSteps(
            @Valid @RequestBody TrackingStepSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("POST /api/v1/tracking/steps/search - criteria: {}, pageable: {}", criteria, pageable);

        var stepsResponse = trackingStepService.searchSteps(criteria, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps search completed successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps by tracking and stage
     */
    @GetMapping("/tracking/{trackingId}/stage/{stage}")
    public ResponseEntity<ApiResponse> getStepsByStage(
            @PathVariable Long trackingId,
            @PathVariable TrackingStage stage,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/tracking/{}/stage/{} - pageable: {}", trackingId, stage, pageable);

        var stepsResponse = trackingStepService.getStepsByStage(trackingId, stage, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps by stage retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps by tracking and action
     */
    @GetMapping("/tracking/{trackingId}/action/{action}")
    public ResponseEntity<ApiResponse> getStepsByAction(
            @PathVariable Long trackingId,
            @PathVariable TrackingAction action,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/tracking/{}/action/{} - pageable: {}", trackingId, action, pageable);

        var stepsResponse = trackingStepService.getStepsByAction(trackingId, action, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps by action retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps performed by a specific user
     */
    @GetMapping("/user/{userId}/performed")
    public ResponseEntity<ApiResponse> getStepsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/user/{}/performed - pageable: {}", userId, pageable);

        var stepsResponse = trackingStepService.getStepsByUser(userId, pageable);

        var apiResponse = new ApiResponse(
                "User tracking steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps assigned to a specific user
     */
    @GetMapping("/user/{userId}/assigned")
    public ResponseEntity<ApiResponse> getStepsAssignedToUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/user/{}/assigned - pageable: {}", userId, pageable);

        var stepsResponse = trackingStepService.getStepsAssignedToUser(userId, pageable);

        var apiResponse = new ApiResponse(
                "Assigned tracking steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get milestone steps for a tracking
     */
    @GetMapping("/tracking/{trackingId}/milestones")
    public ResponseEntity<ApiResponse> getMilestoneSteps(
            @PathVariable Long trackingId,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/tracking/{}/milestones - pageable: {}", trackingId, pageable);

        var stepsResponse = trackingStepService.getMilestoneSteps(trackingId, pageable);

        var apiResponse = new ApiResponse(
                "Milestone tracking steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get stage transition steps for a tracking
     */
    @GetMapping("/tracking/{trackingId}/transitions")
    public ResponseEntity<ApiResponse> getStageTransitionSteps(
            @PathVariable Long trackingId,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/tracking/{}/transitions - pageable: {}", trackingId, pageable);

        var stepsResponse = trackingStepService.getStageTransitionSteps(trackingId, pageable);

        var apiResponse = new ApiResponse(
                "Stage transition steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get recent steps for a tracking (last N steps)
     */
    @GetMapping("/tracking/{trackingId}/recent")
    public ResponseEntity<ApiResponse> getRecentSteps(
            @PathVariable Long trackingId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        log.debug("GET /api/v1/tracking/steps/tracking/{}/recent?limit={}", trackingId, limit);

        var recentSteps = trackingStepService.getRecentSteps(trackingId, limit);

        var apiResponse = new ApiResponse(
                "Recent tracking steps retrieved successfully",
                recentSteps
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get the latest step for a tracking
     */
    @GetMapping("/tracking/{trackingId}/latest")
    public ResponseEntity<ApiResponse> getLatestStep(@PathVariable Long trackingId) {
        log.debug("GET /api/v1/tracking/steps/tracking/{}/latest", trackingId);

        var latestStep = trackingStepService.getLatestStep(trackingId);

        var apiResponse = new ApiResponse(
                "Latest tracking step retrieved successfully",
                latestStep
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps within a date range for a tracking
     */
    @GetMapping("/tracking/{trackingId}/date-range")
    public ResponseEntity<ApiResponse> getStepsByDateRange(
            @PathVariable Long trackingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/tracking/{}/date-range?startDate={}&endDate={} - pageable: {}",
                trackingId, startDate, endDate, pageable);

        var stepsResponse = trackingStepService.getStepsByDateRange(trackingId, startDate, endDate, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps by date range retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps by stage across all trackings
     */
    @GetMapping("/stage/{stage}")
    public ResponseEntity<ApiResponse> getAllStepsByStage(
            @PathVariable TrackingStage stage,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/stage/{} - pageable: {}", stage, pageable);

        var criteria = TrackingStepSearchCriteria.builder()
                .stage(stage)
                .build();

        var stepsResponse = trackingStepService.searchSteps(criteria, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps by stage retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps by action across all trackings
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse> getAllStepsByAction(
            @PathVariable TrackingAction action,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/action/{} - pageable: {}", action, pageable);

        var criteria = TrackingStepSearchCriteria.builder()
                .action(action)
                .build();

        var stepsResponse = trackingStepService.searchSteps(criteria, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps by action retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get milestone steps across all trackings
     */
    @GetMapping("/milestones")
    public ResponseEntity<ApiResponse> getAllMilestoneSteps(
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/milestones - pageable: {}", pageable);

        var criteria = TrackingStepSearchCriteria.builder()
                .isMilestone(true)
                .build();

        var stepsResponse = trackingStepService.searchSteps(criteria, pageable);

        var apiResponse = new ApiResponse(
                "All milestone steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get stage transition steps across all trackings
     */
    @GetMapping("/transitions")
    public ResponseEntity<ApiResponse> getAllStageTransitionSteps(
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/transitions - pageable: {}", pageable);

        var criteria = TrackingStepSearchCriteria.builder()
                .isStageTransition(true)
                .build();

        var stepsResponse = trackingStepService.searchSteps(criteria, pageable);

        var apiResponse = new ApiResponse(
                "All stage transition steps retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get steps performed in a specific time range across all trackings
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse> getStepsByDateRangeGlobal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        log.debug("GET /api/v1/tracking/steps/date-range?startDate={}&endDate={} - pageable: {}",
                startDate, endDate, pageable);

        var criteria = TrackingStepSearchCriteria.builder()
                .performedAfter(startDate)
                .performedBefore(endDate)
                .build();

        var stepsResponse = trackingStepService.searchSteps(criteria, pageable);

        var apiResponse = new ApiResponse(
                "Tracking steps by date range retrieved successfully",
                stepsResponse
        );

        return ResponseEntity.ok(apiResponse);
    }
}
