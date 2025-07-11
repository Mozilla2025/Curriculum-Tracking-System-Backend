package com.mozilla.curriculum_tracking_system.controller.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.*;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.tracking.ICurriculumTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tracking/curriculums")
@RequiredArgsConstructor
@Slf4j
public class CurriculumTrackingController {

    private final ICurriculumTrackingService curriculumTrackingService;

    /**
     * Initiate curriculum tracking process
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse> initiateCurriculumTracking(
            @Valid @ModelAttribute InitiateCurriculumTrackingRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /tracking/curriculums/initiate - request: {}", request);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDto result = curriculumTrackingService.initiateCurriculumTracking(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking initiated successfully",
                result
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Perform action on curriculum tracking
     */
    @PostMapping("/action")
    public ResponseEntity<ApiResponse> performTrackingAction(
            @Valid @ModelAttribute CurriculumTrackingActionRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /tracking/curriculums/action - request: {}", request);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDto result = curriculumTrackingService.performTrackingAction(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking action performed successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get curriculum tracking by ID
     */
    @GetMapping("/get-curriculum-tracking/{trackingId}")
    public ResponseEntity<ApiResponse> getCurriculumTrackingById(@PathVariable Long trackingId) {
        log.debug("GET /tracking/curriculums/get-curriculum-tracking{}", trackingId);

        CurriculumTrackingDto result = curriculumTrackingService.getCurriculumTrackingById(trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get curriculum tracking by curriculum ID
     */
    @GetMapping("/curriculum/{curriculumId}")
    public ResponseEntity<ApiResponse> getCurriculumTrackingByCurriculumId(@PathVariable Long curriculumId) {
        log.debug("GET /tracking/curriculums/curriculum/{}", curriculumId);

        CurriculumTrackingDto result = curriculumTrackingService.getCurriculumTrackingByCurriculumId(curriculumId);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all curriculum trackings with pagination
     */
    @GetMapping("/get-trackings")
    public ResponseEntity<ApiResponse> getAllCurriculumTrackings(
            @PageableDefault(size = 20, sort = "initiatedAt") Pageable pageable) {

        log.debug("GET /tracking/curriculums - pageable: {}", pageable);

        CurriculumTrackingPageResponse result = curriculumTrackingService.getAllCurriculumTrackings(pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum trackings retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Search curriculum trackings
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse> searchCurriculumTrackings(
            @RequestBody CurriculumTrackingSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "initiatedAt") Pageable pageable) {

        log.debug("POST /tracking/curriculums/search - searchRequest: {}, pageable: {}", searchRequest, pageable);

        CurriculumTrackingPageResponse result = curriculumTrackingService.searchCurriculumTrackings(searchRequest, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking search completed successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get my assigned trackings
     */
    @GetMapping("/my-assignments")
    public ResponseEntity<ApiResponse> getMyAssignedTrackings(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20, sort = "lastUpdatedAt") Pageable pageable) {

        log.debug("GET /tracking/curriculums/my-assignments - pageable: {}", pageable);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingPageResponse result = curriculumTrackingService.getMyAssignedTrackings(token, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "My assigned trackings retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get curriculum trackings by stage
     */
    @GetMapping("/stage/{stage}")
    public ResponseEntity<ApiResponse> getCurriculumTrackingsByStage(
            @PathVariable CurriculumTrackingStage stage,
            @PageableDefault(size = 20, sort = "lastUpdatedAt") Pageable pageable) {

        log.debug("GET /tracking/curriculums/stage/{} - pageable: {}", stage, pageable);

        CurriculumTrackingPageResponse result = curriculumTrackingService.getCurriculumTrackingsByStage(stage, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum trackings by stage retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get curriculum trackings by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getCurriculumTrackingsByStatus(
            @PathVariable CurriculumTrackingStatus status,
            @PageableDefault(size = 20, sort = "lastUpdatedAt") Pageable pageable) {

        log.debug("GET /tracking/curriculums/status/{} - pageable: {}", status, pageable);

        CurriculumTrackingPageResponse result = curriculumTrackingService.getCurriculumTrackingsByStatus(status, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum trackings by status retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get overdue trackings
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse> getOverdueTrackings() {
        log.debug("GET /tracking/curriculums/overdue");

        List<CurriculumTrackingDto> result = curriculumTrackingService.getOverdueTrackings();

        ApiResponse apiResponse = new ApiResponse(
                "Overdue trackings retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get trackings expiring soon
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse> getExpiringSoonTrackings(
            @RequestParam(defaultValue = "30") int days) {

        log.debug("GET /tracking/curriculums/expiring-soon?days={}", days);

        List<CurriculumTrackingDto> result = curriculumTrackingService.getExpiringSoonTrackings(days);

        ApiResponse apiResponse = new ApiResponse(
                "Expiring soon trackings retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get curriculum tracking statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getCurriculumTrackingStats() {
        log.debug("GET /tracking/curriculums/stats");

        CurriculumTrackingStatsDto result = curriculumTrackingService.getCurriculumTrackingStats();

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking statistics retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get curriculum tracking timeline
     */
    @GetMapping("/{trackingId}/timeline")
    public ResponseEntity<ApiResponse> getCurriculumTrackingTimeline(@PathVariable Long trackingId) {
        log.debug("GET /tracking/curriculums/{}/timeline", trackingId);

        CurriculumTrackingTimelineDto result = curriculumTrackingService.getCurriculumTrackingTimeline(trackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking timeline retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Assign tracking to user (QA only)
     */
    @PutMapping("/{trackingId}/assign/{userId}")
    public ResponseEntity<ApiResponse> assignTrackingToUser(
            @PathVariable Long trackingId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /tracking/curriculums/{}/assign/{}", trackingId, userId);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDto result = curriculumTrackingService.assignTrackingToUser(trackingId, userId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking assigned to user successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update tracking notes (QA only)
     */
    @PutMapping("/{trackingId}/notes")
    public ResponseEntity<ApiResponse> updateTrackingNotes(
            @PathVariable Long trackingId,
            @RequestBody String notes,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /tracking/curriculums/{}/notes", trackingId);

        String token = extractToken(authorizationHeader);
        CurriculumTrackingDto result = curriculumTrackingService.updateTrackingNotes(trackingId, notes, token);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking notes updated successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Deactivate curriculum tracking (QA only)
     */
    @DeleteMapping("/{trackingId}")
    public ResponseEntity<ApiResponse> deactivateCurriculumTracking(
            @PathVariable Long trackingId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("DELETE /tracking/curriculums/{}", trackingId);

        String token = extractToken(authorizationHeader);
        curriculumTrackingService.deactivateCurriculumTracking(trackingId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum tracking deactivated successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get available actions for tracking
     */
    @GetMapping("/{trackingId}/available-actions")
    public ResponseEntity<ApiResponse> getAvailableActions(
            @PathVariable Long trackingId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("GET /tracking/curriculums/{}/available-actions", trackingId);

        String token = extractToken(authorizationHeader);
        List<TrackingActionType> result = curriculumTrackingService.getAvailableActions(trackingId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Available actions retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get stage information
     */
    @GetMapping("/stages/{stage}/info")
    public ResponseEntity<ApiResponse> getStageInfo(@PathVariable CurriculumTrackingStage stage) {
        log.debug("GET /tracking/curriculums/stages/{}/info", stage);

        CurriculumTrackingStageInfo result = curriculumTrackingService.getStageInfo(stage);

        ApiResponse apiResponse = new ApiResponse(
                "Stage information retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Check if user can perform action
     */
    @GetMapping("/{trackingId}/can-perform/{actionType}")
    public ResponseEntity<ApiResponse> canPerformAction(
            @PathVariable Long trackingId,
            @PathVariable TrackingActionType actionType,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("GET /tracking/curriculums/{}/can-perform/{}", trackingId, actionType);

        String token = extractToken(authorizationHeader);
        boolean result = curriculumTrackingService.canPerformAction(trackingId, actionType, token);

        ApiResponse apiResponse = new ApiResponse(
                "Permission check completed successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}
