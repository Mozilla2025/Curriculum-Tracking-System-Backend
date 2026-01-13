package com.mozilla.curriculum_tracking_system.service.department;

import com.mozilla.curriculum_tracking_system.dto.department.CreateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentPageResponse;
import com.mozilla.curriculum_tracking_system.dto.department.UpdateDepartmentRequest;
import org.springframework.data.domain.Pageable;

public interface IDepartmentService {

    DepartmentPageResponse getAllDepartments(Pageable pageable);

    DepartmentPageResponse getDepartmentsBySchoolId(Long schoolId, Pageable pageable);

    DepartmentPageResponse searchDepartments(String searchTerm, Pageable pageable);

    DepartmentPageResponse searchDepartmentsBySchoolId(Long schoolId, String searchTerm, Pageable pageable);

    DepartmentDto getDepartmentById(Long departmentId);

    boolean existsById(Long departmentId);

    long getDepartmentCountBySchoolId(Long schoolId);

    DepartmentDto createDepartment(CreateDepartmentRequest request, String token);

    DepartmentDto updateDepartment(Long departmentId, UpdateDepartmentRequest request, String token);

    void deleteDepartment(Long departmentId, String token);

    long getTotalDepartmentCount();
}