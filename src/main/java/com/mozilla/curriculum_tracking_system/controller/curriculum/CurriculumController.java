package com.mozilla.curriculum_tracking_system.controller.curriculum;

import com.mozilla.curriculum_tracking_system.annotation.AdminOnly;
import com.mozilla.curriculum_tracking_system.dto.curriculum.*;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.curriculum.ICurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/curriculums")
@RequiredArgsConstructor
@Slf4j
public class CurriculumController {

    private final ICurriculumService curriculumService;

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can create curriculums")
    public ResponseEntity<ApiResponse> createCurriculum(
            @Valid @RequestBody CreateCurriculumRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /api/v1/curriculums/admin/create - request: {}", request);

        String token = extractToken(authorizationHeader);
        CurriculumDto createdCurriculum = curriculumService.createCurriculum(request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum created successfully",
                createdCurriculum
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/admin/update/{curriculumId}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can update curriculums")
    public ResponseEntity<ApiResponse> updateCurriculum(
            @PathVariable Long curriculumId,
            @Valid @RequestBody UpdateCurriculumRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /api/v1/curriculums/admin/update/{} - request: {}", curriculumId, request);

        String token = extractToken(authorizationHeader);
        CurriculumDto updatedCurriculum = curriculumService.updateCurriculum(curriculumId, request, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum updated successfully",
                updatedCurriculum
        );

        return ResponseEntity.ok(apiResponse);
    }


    @DeleteMapping("/admin/delete/{curriculumId}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can delete curriculums")
    public ResponseEntity<ApiResponse> deleteCurriculum(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("DELETE /api/v1/curriculums/admin/delete/{}", curriculumId);

        String token = extractToken(authorizationHeader);
        curriculumService.deleteCurriculum(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum deleted successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/admin/permanent-delete/{curriculumId}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can permanently delete curriculums")
    public ResponseEntity<ApiResponse> permanentlyDeleteCurriculum(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("DELETE /api/v1/curriculums/admin/permanent-delete/{}", curriculumId);

        String token = extractToken(authorizationHeader);
        curriculumService.permanentlyDeleteCurriculum(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum permanently deleted successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/admin/review/{curriculumId}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can put curriculums under review")
    public ResponseEntity<ApiResponse> putCurriculumUnderReview(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /api/v1/curriculums/admin/review/{}", curriculumId);

        String token = extractToken(authorizationHeader);
        CurriculumDto curriculum = curriculumService.putCurriculumUnderReview(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum put under review successfully",
                curriculum
        );

        return ResponseEntity.ok(apiResponse);
    }


    @PutMapping("/admin/toggle-status/{curriculumId}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can toggle curriculum status")
    public ResponseEntity<ApiResponse> toggleCurriculumStatus(
            @PathVariable Long curriculumId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /api/v1/curriculums/admin/toggle-status/{}", curriculumId);

        String token = extractToken(authorizationHeader);
        CurriculumDto curriculum = curriculumService.toggleCurriculumStatus(curriculumId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum status toggled successfully",
                curriculum
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can view curriculum statistics")
    public ResponseEntity<ApiResponse> getCurriculumStats() {

        log.debug("GET /api/v1/curriculums/admin/stats");

        CurriculumStatusStats stats = curriculumService.getCurriculumStats();

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum statistics retrieved successfully",
                stats
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/admin/expiring-soon")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can view expiring curriculums")
    public ResponseEntity<ApiResponse> getCurriculumsExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {


        List<CurriculumDto> expiringSoon = curriculumService.getCurriculumsExpiringSoon(days);

        ApiResponse apiResponse = new ApiResponse(
                "Expiring curriculums retrieved successfully",
                expiringSoon
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{curriculumId}")
    public ResponseEntity<ApiResponse> getCurriculumById(@PathVariable Long curriculumId) {

        log.debug("GET /api/v1/curriculums/{}", curriculumId);

        CurriculumDto curriculum = curriculumService.getCurriculumById(curriculumId);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum retrieved successfully",
                curriculum
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCurriculums(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("GET /api/v1/curriculums - pageable: {}", pageable);

        CurriculumPageResponse curriculums = curriculumService.getAllCurriculums(pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculums retrieved successfully",
                curriculums
        );

        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/search")
    public ResponseEntity<ApiResponse> searchCurriculums(
            @RequestBody CurriculumSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {


        CurriculumPageResponse curriculums = curriculumService.searchCurriculums(criteria, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculums search completed successfully",
                curriculums
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse> getCurriculumsBySchool(
            @PathVariable Long schoolId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("GET /api/v1/curriculums/school/{} - pageable: {}", schoolId, pageable);

        CurriculumPageResponse curriculums = curriculumService.getCurriculumsBySchool(schoolId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "School curriculums retrieved successfully",
                curriculums
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse> getCurriculumsByDepartment(
            @PathVariable Long departmentId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("GET /api/v1/curriculums/department/{} - pageable: {}", departmentId, pageable);

        CurriculumPageResponse curriculums = curriculumService.getCurriculumsByDepartment(departmentId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Department curriculums retrieved successfully",
                curriculums
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/academic-level/{academicLevelId}")
    public ResponseEntity<ApiResponse> getCurriculumsByAcademicLevel(
            @PathVariable Long academicLevelId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("GET /api/v1/curriculums/academic-level/{} - pageable: {}", academicLevelId, pageable);

        CurriculumPageResponse curriculums = curriculumService.getCurriculumsByAcademicLevel(academicLevelId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "Academic level curriculums retrieved successfully",
                curriculums
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