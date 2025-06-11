package com.mozilla.curriculum_tracking_system.service.school;


import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService implements ISchoolService{

    private SchoolRepository schoolRepository;

    @Override
    @Cacheable(value = "schools", key = "'all_schools")
    public List<School> getAllSchools() {
        log.debug("Fetching all schools from database");
        List<School> schools = schoolRepository.findAll();
        log.debug("Retrieved {} schools from database", schools);
        return schools;
    }

    @Override
    @Cacheable(value = "schools", key = "#schoolId")
    public School getSchoolById(Long id) {
        log.debug("fetching school with ID: {}", id);

        if (id == null){
            throw new IllegalArgumentException("School ID cannot be null");
        }

        return schoolRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("School not found with ID: {}", id);
                    return new ResourceNotFoundException("School not found with ID: " + id);
                });
    }

    @Override
    @Cacheable(value = "School_departments", key = "#id")
    public Set<Department> getSchoolDepartments(Long id) {
        log.debug("Fetching departments for school ID: {}", id);

        School school = getSchoolById(id);
        Set<Department> departments = school.getDepartments();

        log.debug("Found {} departments for school ID: {}", departments.size(), id);
        return departments;
    }

    @Override
    @Cacheable(value = "school_curriculums", key = "#id")
    public Set<Curriculum> getSchoolCurricula(Long id) {
        log.debug("Fetching curricula for school id: {}",id);

        School school = getSchoolById(id);
        Set<Curriculum> curricula = school.getCurriculums();

        log.debug("Found {} curricula for school ID: {}",curricula.size(), id);
        return curricula;
    }


}


