package com.mozilla.curriculum_tracking_system.mapper;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import org.springframework.stereotype.Component;

@Component
public class CurriculumMapper {

    public CurriculumDto mapToDto(Curriculum curriculum){
        return CurriculumDto.builder()
                .name(curriculum.getName())
                .code(curriculum.getCode())
                .durationSemesters(curriculum.getDurationSemesters())
                .status(curriculum.getStatus())
                .approvedAt(curriculum.getApprovedAt())
                .effectiveDate(curriculum.getEffectiveDate())
                .expiryDate(curriculum.getExpiryDate())
                .isActive(curriculum.isActive())
                .createdAt(curriculum.getCreatedAt())
                .updatedAt(curriculum.getUpdatedAt())
                .schoolId(curriculum.getSchool() != null ? curriculum.getId() : null)
                .departmentId(curriculum.getDepartment() != null ? curriculum.getId() : null)
                .departmentName(curriculum.getDepartment() != null ? curriculum.getName() : null)
                .academicLevelName(curriculum.getAcademicLevel() != null ? curriculum.getAcademicLevel())
                .build();
    }
}
