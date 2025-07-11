package com.mozilla.curriculum_tracking_system.controller.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingHistoryDto;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.service.tracking.ICurriculumTrackingHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tracking/history")
@RequiredArgsConstructor
@Slf4j
public class CurriculumTrackingHistoryController {

    private final ICurriculumTrackingHistoryService trackingHistoryService;
    private final IAuthenticationService authenticationService;


    @GetMapping("/curriculum-tracking/{curriculumTrackingId}")
    public ResponseEntity<ApiResponse> getTrackingHistory(@PathVariable Long curriculumTrackingId) {
        log.debug("GET /tracking/history/curriculum-tracking/{}", curriculumTrackingId);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getTrackingHistory(curriculumTrackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking history retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/curriculum-tracking/{curriculumTrackingId}/paginated")
    public ResponseEntity<ApiResponse> getTrackingHistoryPaginated(
            @PathVariable Long curriculumTrackingId,
            @PageableDefault(size = 10, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("GET /tracking/history/curriculum-tracking/{}/paginated - pageable: {}", curriculumTrackingId, pageable);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getTrackingHistory(curriculumTrackingId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking history retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/curriculum-tracking/{curriculumTrackingId}/recent")
    public ResponseEntity<ApiResponse> getRecentTrackingHistory(
            @PathVariable Long curriculumTrackingId,
            @RequestParam(defaultValue = "5") int limit) {

        log.debug("GET /tracking/history/curriculum-tracking/{}/recent?limit={}", curriculumTrackingId, limit);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getRecentTrackingHistory(curriculumTrackingId, limit);

        ApiResponse apiResponse = new ApiResponse(
                "Recent tracking history retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get tracking history by ID
     */
    @GetMapping("/{historyId}")
    public ResponseEntity<ApiResponse> getTrackingHistoryById(@PathVariable Long historyId) {

        CurriculumTrackingHistoryDto result = trackingHistoryService.getTrackingHistoryById(historyId);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking history entry retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get milestone history for a curriculum tracking
     */
    @GetMapping("/curriculum-tracking/{curriculumTrackingId}/milestones")
    public ResponseEntity<ApiResponse> getMilestoneHistory(@PathVariable Long curriculumTrackingId) {
        log.debug("GET /tracking/history/curriculum-tracking/{}/milestones", curriculumTrackingId);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getMilestoneHistory(curriculumTrackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Milestone history retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get stage transitions for a curriculum tracking
     */
    @GetMapping("/curriculum-tracking/{curriculumTrackingId}/transitions")
    public ResponseEntity<ApiResponse> getStageTransitions(@PathVariable Long curriculumTrackingId) {
        log.debug("GET /tracking/history/curriculum-tracking/{}/transitions", curriculumTrackingId);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getStageTransitions(curriculumTrackingId);

        ApiResponse apiResponse = new ApiResponse(
                "Stage transitions retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/performer/{userId}")
    public ResponseEntity<ApiResponse> getHistoryByPerformer(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("GET /tracking/history/performer/{} - pageable: {}", userId, pageable);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getHistoryByPerformer(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "History by performer retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/assignee/{userId}")
    public ResponseEntity<ApiResponse> getHistoryByAssignee(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("GET /tracking/history/assignee/{} - pageable: {}", userId, pageable);

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getHistoryByAssignee(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "History by assignee retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchTrackingHistory(@RequestParam String searchTerm) {

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.searchTrackingHistory(searchTerm);

        ApiResponse apiResponse = new ApiResponse(
                "Tracking history search completed successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse> getOverdueHistoryItems() {
        log.debug("GET /tracking/history/overdue");

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getOverdueHistoryItems();

        ApiResponse apiResponse = new ApiResponse(
                "Overdue history items retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/my-actions")
    public ResponseEntity<ApiResponse> getMyPerformedActions(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = authenticationService.getUserIdFromToken(extractToken(authorizationHeader));

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getHistoryByPerformer(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "My performed actions retrieved successfully",
                result
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/my-assignments")
    public ResponseEntity<ApiResponse> getMyAssignedTasksHistory(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = authenticationService.getUserIdFromToken(extractToken(authorizationHeader));

        List<CurriculumTrackingHistoryDto> result = trackingHistoryService.getHistoryByAssignee(userId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "My assigned tasks history retrieved successfully",
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
