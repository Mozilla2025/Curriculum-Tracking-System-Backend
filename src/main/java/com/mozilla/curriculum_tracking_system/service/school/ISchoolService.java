package com.mozilla.curriculum_tracking_system.service.school;

import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

public interface ISchoolService {
    List<School> getAllSchools();

    School getSchoolById(Long id);

    Set<Department> getSchoolDepartments(@Valid Long id);

    Set<Curriculum> getSchoolCurricula(@Valid Long id);
}
