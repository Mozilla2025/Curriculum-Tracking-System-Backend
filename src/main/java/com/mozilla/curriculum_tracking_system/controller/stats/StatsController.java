package com.mozilla.curriculum_tracking_system.controller.stats;

import com.mozilla.curriculum_tracking_system.dto.stats.SystemStatsDto;
import com.mozilla.curriculum_tracking_system.dto.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.curriculum.ICurriculumService;
import com.mozilla.curriculum_tracking_system.service.department.IDepartmentService;
import com.mozilla.curriculum_tracking_system.service.school.ISchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final ISchoolService schoolService;
    private final IDepartmentService departmentService;
    private final ICurriculumService curriculumService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getSystemSummary() {
        log.info("Fetching system-wide statistics summary");

        SystemStatsDto stats = SystemStatsDto.builder()
                .totalSchools(schoolService.getSchoolCount())
                .totalDepartments(departmentService.getTotalDepartmentCount())
                .totalCurriculums(curriculumService.getCurriculumStats().getTotalCurriculums())
                .build();

        return ResponseEntity.ok(new ApiResponse("System statistics retrieved successfully", stats));
    }
}