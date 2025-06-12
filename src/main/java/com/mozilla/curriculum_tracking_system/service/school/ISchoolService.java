package com.mozilla.curriculum_tracking_system.service.school;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

public interface ISchoolService {
    List<SchoolDto> getAllSchools();

    SchoolDto getSchoolById(Long id);

    Set<DepartmentDto> getSchoolDepartments(@Valid Long id);

    Set<CurriculumDto> getSchoolCurricula(@Valid Long id);

    Set<CurriculumDto> getSchoolCurriculaByLevel(@Valid Long id);

    @Cacheable(value = "school_curriculums_by_level", key = "#id + '_' + academic_level_id")
    Set<CurriculumDto> getSchoolCurriculaByLevel(Long id, AcademicLevel level);
}
