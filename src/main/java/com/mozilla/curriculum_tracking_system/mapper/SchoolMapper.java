package com.mozilla.curriculum_tracking_system.mapper;

import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;
import com.mozilla.curriculum_tracking_system.model.school.School;
import org.springframework.stereotype.Component;

@Component
public class SchoolMapper {

    public SchoolDto mapToDto(School school) {
        return SchoolDto.builder()
                .name(school.getName())
                .code(school.getCode())
                .deanId(school.getDeanId())
                .build();
    }
}

