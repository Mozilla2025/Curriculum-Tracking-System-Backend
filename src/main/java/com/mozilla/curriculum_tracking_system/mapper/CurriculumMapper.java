package com.mozilla.curriculum_tracking_system.mapper;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumDto;
import com.mozilla.curriculum_tracking_system.dto.curriculum.CreateCurriculumRequest;
import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumPageResponse;
import com.mozilla.curriculum_tracking_system.dto.curriculum.UpdateCurriculumRequest;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class CurriculumMapper {

    public CurriculumPageResponse buildCurriculumPageResponse(Page<Curriculum> curriculumPage) {
        List<CurriculumDto> curriculumDtos = curriculumPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return CurriculumPageResponse.builder()
                .curriculums(curriculumDtos)
                .currentPage(curriculumPage.getNumber())
                .totalPages(curriculumPage.getTotalPages())
                .totalElements(curriculumPage.getTotalElements())
                .pageSize(curriculumPage.getSize())
                .hasNext(curriculumPage.hasNext())
                .hasPrevious(curriculumPage.hasPrevious())
                .build();
    }


    public CurriculumDto toDto(Curriculum curriculum) {
        if (curriculum == null) {
            return null;
        }

        return CurriculumDto.builder()
                .id(curriculum.getId())
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
                .schoolId(curriculum.getSchool() != null ? curriculum.getSchool().getId() : null)
                .schoolName(curriculum.getSchool() != null ? curriculum.getSchool().getName() : null)
                .departmentId(curriculum.getDepartment() != null ? curriculum.getDepartment().getId() : null)
                .departmentName(curriculum.getDepartment() != null ? curriculum.getDepartment().getName() : null)
                .academicLevelName(curriculum.getAcademicLevel() != null ? curriculum.getAcademicLevel().getName() : null)
                .build();
    }

    public Curriculum toEntity(CreateCurriculumRequest request, School school, Department department, AcademicLevel academicLevel) {
        if (request == null) {
            return null;
        }

        return Curriculum.builder()
                .name(request.getName())
                .code(request.getCode())
                .durationSemesters(request.getDurationSemesters())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .school(school)
                .department(department)
                .academicLevel(academicLevel)
                .build();
    }


    public void updateEntityFromRequest(Curriculum curriculum, UpdateCurriculumRequest request,
                                        Department department, AcademicLevel academicLevel) {
        if (request == null || curriculum == null) {
            return;
        }

        if (request.getName() != null) {
            curriculum.setName(request.getName());
        }
        if (request.getCode() != null) {
            curriculum.setCode(request.getCode());
        }
        if (request.getDurationSemesters() != null) {
            curriculum.setDurationSemesters(request.getDurationSemesters());
        }
        if (request.getEffectiveDate() != null) {
            curriculum.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getExpiryDate() != null) {
            curriculum.setExpiryDate(request.getExpiryDate());
        }
        if (department != null) {
            curriculum.setDepartment(department);
        }
        if (academicLevel != null) {
            curriculum.setAcademicLevel(academicLevel);
        }
    }


    public List<CurriculumDto> toDtoList(List<Curriculum> curriculums) {
        if (curriculums == null) {
            return null;
        }

        return curriculums.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
