package com.mozilla.curriculum_tracking_system.mapper.department;

import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentPageResponse;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DepartmentMapper {

    public DepartmentPageResponse buildDepartmentPageResponse(Page<Department> departmentPage) {
        List<DepartmentDto> departmentDtos = departmentPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return DepartmentPageResponse.builder()
                .departments(departmentDtos)


                .currentPage(departmentPage.getNumber())
                .totalPages(departmentPage.getTotalPages())
                .totalElements(departmentPage.getTotalElements())
                .pageSize(departmentPage.getSize())
                .hasNext(departmentPage.hasNext())
                .hasPrevious(departmentPage.hasPrevious())
                .build();
    }

    public DepartmentDto mapToDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .headId(department.getHeadId())
                .schoolId(department.getSchool().getId())
                .schoolName(department.getSchool().getName())
                .curriculumCount(department.getCurriculums().size())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
