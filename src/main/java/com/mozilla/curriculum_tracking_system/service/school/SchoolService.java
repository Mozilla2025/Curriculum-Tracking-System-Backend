package com.mozilla.curriculum_tracking_system.service.school;


import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumMapper;
import com.mozilla.curriculum_tracking_system.mapper.DepartmentMapper;
import com.mozilla.curriculum_tracking_system.mapper.SchoolMapper;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService implements ISchoolService{

    private final SchoolRepository schoolRepository;
    private final SchoolMapper schoolMapper;
    private final DepartmentMapper departmentMapper;
    private final CurriculumMapper curriculumMapper;

    @Override
    @Cacheable(value = "schools", key = "'all_schools")
    public List<SchoolDto> getAllSchools() {
        log.debug("Fetching all schools from database");
        List<School> schools = schoolRepository.findAll();
        List<SchoolDto> schoolDtos = schools.stream().map(schoolMapper::mapToDto).toList();
        log.debug("Retrieved {} schools from database", schools);
        return schoolDtos;
    }

    @Override
    @Cacheable(value = "schools", key = "#schoolId")
    public SchoolDto getSchoolById(Long id) {
        log.debug("fetching school with ID: {}", id);

        if (id == null){
            throw new IllegalArgumentException("School ID cannot be null");
        }

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("School not found with ID: {}", id);
                    return new ResourceNotFoundException("School not found with ID: " + id);
                });
        SchoolDto schoolDto = schoolMapper.mapToDto(school);
        return schoolDto;
    }

    @Override
    @Cacheable(value = "School_departments", key = "#id")
    public Set<DepartmentDto> getSchoolDepartments(Long id) {
        log.debug("Fetching departments for school ID: {}", id);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("School not found with ID: " + id));

        Set<Department> departments = school.getDepartments();
        Set<DepartmentDto> departmentDtos = departments
                .stream()
                .map(departmentMapper::mapToDto).collect(Collectors.toSet());

        log.debug("Found {} departments for school ID: {}", departments.size(), id);
        return departmentDtos;
    }

    @Override
    @Cacheable(value = "school_curriculums", key = "#id")
    public Set<CurriculumDto> getSchoolCurricula(Long id) {
        log.debug("Fetching curricula for school id: {}",id);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("School not found with ID: " + id));

        Set<Curriculum> curricula = school.getCurriculums();
        Set<CurriculumDto> curriculumDtos = curricula
                .stream()
                .map(curriculumMapper::mapToDto).collect(Collectors.toSet());

        log.debug("Found {} curricula for school ID: {}",curricula.size(), id);
        return curriculumDtos;
    }

    @Cacheable(value = "school_curriculums_by_level", key = "#id + '_' + academic_level_id")
    @Override
    public Set<CurriculumDto> getSchoolCurriculaByLevel(Long id, AcademicLevel level) {
        log.debug("Fetching {} level curriculums for school ID: ", level, id);

        if (level == null){
            throw new IllegalArgumentException("Academic level cannot be null");
        }

        Set<CurriculumDto> allCurricula = getSchoolCurricula(id);
        Set<CurriculumDto> filteredCurricula = allCurricula.stream()
                        .filter(curriculum -> level.equals(curriculum.getAcademicLevelName()))
                        .collect(Collectors.toSet());

        log.debug("Found {} {} level curriculums for school ID: {}",
                filteredCurricula.size(), level, id);

        return filteredCurricula;
    }

}


