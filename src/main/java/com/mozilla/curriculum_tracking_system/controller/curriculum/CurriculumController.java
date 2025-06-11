package com.mozilla.curriculum_tracking_system.controller.curriculum;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumPageResponse;
import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumSearchCriteria;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.curriculum.ICurriculumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users/curriculums")
@RequiredArgsConstructor
@Slf4j
public class CurriculumController {

    private final ICurriculumService curriculumService;


    @GetMapping("/get-by-id/{curriculumId}")
    public ResponseEntity<ApiResponse> getCurriculumById(@PathVariable Long curriculumId) {

        log.debug("GET /api/v1/curriculums/{}", curriculumId);

        CurriculumDto curriculum = curriculumService.getCurriculumById(curriculumId);

        ApiResponse apiResponse = new ApiResponse(
                "Curriculum retrieved successfully",
                curriculum
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/get-all")
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

}