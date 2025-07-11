package com.mozilla.curriculum_tracking_system.controller.curriculum;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CreateCurriculumRequest;
import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumStatusStats;
import com.mozilla.curriculum_tracking_system.dto.curriculum.UpdateCurriculumRequest;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.curriculum.ICurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/curriculums")
@RequiredArgsConstructor
public class CurriculumAdminController {
    private final ICurriculumService curriculumService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createCurriculum(
            @Valid @RequestBody CreateCurriculumRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {


        String token = extractToken(authorizationHeader);
        CurriculumDto createdCurriculum = curriculumService.createCurriculum(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum created successfully",
                createdCurriculum
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/update/{curriculumId}")
    public ResponseEntity<ApiResponse> updateCurriculum(
            @PathVariable Long curriculumId,
            @Valid @RequestBody UpdateCurriculumRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {


        String token = extractToken(authorizationHeader);
        CurriculumDto updatedCurriculum = curriculumService.updateCurriculum(curriculumId, request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum updated successfully",
                updatedCurriculum
        );

        return ResponseEntity.ok(apiResponse);
    }


    @DeleteMapping("/delete/{curriculumId}")
    public ResponseEntity<ApiResponse> deleteCurriculum(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {


        String token = extractToken(authorizationHeader);
        curriculumService.deleteCurriculum(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum deleted successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/permanent-delete/{curriculumId}")
    public ResponseEntity<ApiResponse> permanentlyDeleteCurriculum(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {


        String token = extractToken(authorizationHeader);
        curriculumService.permanentlyDeleteCurriculum(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum permanently deleted successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/review/{curriculumId}")
    public ResponseEntity<ApiResponse> putCurriculumUnderReview(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {


        String token = extractToken(authorizationHeader);
        CurriculumDto curriculum = curriculumService.putCurriculumUnderReview(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum put under review successfully",
                curriculum
        );

        return ResponseEntity.ok(apiResponse);
    }


    @PutMapping("/toggle-status/{curriculumId}")
    public ResponseEntity<ApiResponse> toggleCurriculumStatus(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {


        String token = extractToken(authorizationHeader);
        CurriculumDto curriculum = curriculumService.toggleCurriculumStatus(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum status toggled successfully",
                curriculum
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getCurriculumStats() {


        CurriculumStatusStats stats = curriculumService.getCurriculumStats();

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum statistics retrieved successfully",
                stats
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse> getCurriculumsExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {


        List<CurriculumDto> expiringSoon = curriculumService.getCurriculumsExpiringSoon(days);

        ApiResponse apiResponse = new ApiResponse(
                "Expiring curriculums retrieved successfully",
                expiringSoon
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
