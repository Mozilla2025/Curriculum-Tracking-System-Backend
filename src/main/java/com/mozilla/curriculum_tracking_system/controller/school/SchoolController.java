package com.mozilla.curriculum_tracking_system.controller.school;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumMapper;
import com.mozilla.curriculum_tracking_system.mapper.DepartmentMapper;
import com.mozilla.curriculum_tracking_system.mapper.SchoolMapper;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
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
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/schools")
@Slf4j
public class SchoolController {

    private final ISchoolService schoolService;
    private final DepartmentMapper departmentMapper;
    private final CurriculumMapper curriculumMapper;

    @GetMapping
    public ResponseEntity<List<School>> getAllSchools(){

        log.info("Fetching all schools"); //is this necessary, what does it do?
        List<School> schools = schoolService.getAllSchools();
        log.info("Found {} schools", schools.size());
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/{id}")
    public ResponseEntity<School> getSchoolById(@Valid @PathVariable Long id){
        log.info("Fetching school with ID: {}", id);
        School school = schoolService.getSchoolById(id);
        log.info("Found school: {}", school.getName());
        return ResponseEntity.ok(school);
    }

    @GetMapping("/{id}/departments")
    public ResponseEntity<Set<DepartmentDto>> getSchoolDepartments(@Valid @PathVariable Long id){
        log.info("Fetching departments for school ID: {}", id);
        Set<Department> departments = schoolService.getSchoolDepartments(id);
        log.info("Found {} departments for school ID: {}", departments);
        Set<DepartmentDto> departmentDtos = departments.stream().map(departmentMapper::mapToDto).collect(Collectors.toSet());
        return ResponseEntity.ok(departmentDtos);
    }

    @GetMapping("/{id}/curricula")
    public ResponseEntity<Set<CurriculumDto>> getSchoolCurricula(@Valid @PathVariable Long id){
        log.info("Fetching curricula for school ID: {}", id);
        Set<Curriculum> curricula = schoolService.getSchoolCurricula(id);
        Set<CurriculumDto> curriculumDtos = curricula.stream().map(curriculumMapper::mapToDto).collect(Collectors.toSet());
        return ResponseEntity.ok(curriculumDtos);
    }

    @GetMapping("/{id}/curriculums/{academicLevel}")
    public ResponseEntity<Set<CurriculumDto>> getSchoolCurriculaByLevel(@Valid @PathVariable Long id, )
}
