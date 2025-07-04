package com.mozilla.curriculum_tracking_system.controller.school;

import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumMapper;
import com.mozilla.curriculum_tracking_system.mapper.DepartmentMapper;
import com.mozilla.curriculum_tracking_system.service.school.ISchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/schools")
@Slf4j
public class SchoolController {

    private final ISchoolService schoolService;
    private final DepartmentMapper departmentMapper;
    private final CurriculumMapper curriculumMapper;

    @GetMapping("/get-all")
    public ResponseEntity<List<SchoolDto>> getAllSchools(){

        log.info("Fetching all schools");
        List<SchoolDto> schools = schoolService.getAllSchools();
        log.info("Found {} schools", schools.size());
        return ResponseEntity.ok(schools);
    }

    @GetMapping("get-by-id/{id}")
    public ResponseEntity<SchoolDto> getSchoolById(@Valid @PathVariable Long id){
        log.info("Fetching school with ID: {}", id);
        SchoolDto school = schoolService.getSchoolById(id);
        log.info("Found school: {}", school.getName());
        return ResponseEntity.ok(school);
    }

}
