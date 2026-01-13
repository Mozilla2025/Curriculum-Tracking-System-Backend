package com.mozilla.curriculum_tracking_system.service.school;

import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;

import java.util.List;

public interface ISchoolService {
    List<SchoolDto> getAllSchools();

    SchoolDto getSchoolById(Long id);

    long getSchoolCount();

}
