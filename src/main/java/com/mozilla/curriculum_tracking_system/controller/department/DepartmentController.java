package com.mozilla.curriculum_tracking_system.controller.department;

import com.mozilla.curriculum_tracking_system.dto.department.CreateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentPageResponse;
import com.mozilla.curriculum_tracking_system.dto.department.UpdateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Get all departments with pagination and optional search
     */
    @GetMapping("/get-all-departments")
    public ResponseEntity<ApiResponse> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        log.debug("GET /api/v1/departments - page: {}, size: {}, sortBy: {}, sortDir: {}, search: {}",
                page, size, sortBy, sortDir, search);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        DepartmentPageResponse response = search != null ?
                departmentService.searchDepartments(search, pageable) :
                departmentService.getAllDepartments(pageable);

        ApiResponse apiResponse = new ApiResponse(
                search != null ? "Departments search completed successfully" : "Departments retrieved successfully",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get departments by school ID with pagination and optional search
     */
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse> getDepartmentsBySchool(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        log.debug("GET /api/v1/departments/school/{} - page: {}, size: {}, search: {}",
                schoolId, page, size, search);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        DepartmentPageResponse response = search != null ?
                departmentService.searchDepartmentsBySchoolId(schoolId, search, pageable) :
                departmentService.getDepartmentsBySchoolId(schoolId, pageable);

        ApiResponse apiResponse = new ApiResponse(
                "School departments retrieved successfully",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get department by ID
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse> getDepartmentById(@PathVariable Long departmentId) {
        log.debug("GET /api/v1/departments/{}", departmentId);

        DepartmentDto department = departmentService.getDepartmentById(departmentId);

        ApiResponse apiResponse = new ApiResponse(
                "Department retrieved successfully",
                department
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Create a new department
     */
    @PostMapping("/create-department")
    public ResponseEntity<ApiResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.debug("POST /api/v1/departments - request: {}", request);

        DepartmentDto createdDepartment = departmentService.createDepartment(request);

        ApiResponse apiResponse = new ApiResponse(
                "Department created successfully",
                createdDepartment
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Update an existing department
     */
    @PutMapping("/update/{departmentId}")
    public ResponseEntity<ApiResponse> updateDepartment(
            @PathVariable Long departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        log.debug("PUT /api/v1/departments/{} - request: {}", departmentId, request);

        DepartmentDto updatedDepartment = departmentService.updateDepartment(departmentId, request);

        ApiResponse apiResponse = new ApiResponse(
                "Department updated successfully",
                updatedDepartment
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete a department
     */
    @DeleteMapping("/delete/{departmentId}")
    public ResponseEntity<ApiResponse> deleteDepartment(@PathVariable Long departmentId) {
        log.debug("DELETE /api/v1/departments/{}", departmentId);

        departmentService.deleteDepartment(departmentId);

        ApiResponse apiResponse = new ApiResponse(
                "Department deleted successfully",
                null
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Get department count by school ID
     */
    @GetMapping("/school/{schoolId}/count")
    public ResponseEntity<ApiResponse> getDepartmentCount(@PathVariable Long schoolId) {
        log.debug("GET /api/v1/departments/school/{}/count", schoolId);

        long count = departmentService.getDepartmentCountBySchoolId(schoolId);

        ApiResponse apiResponse = new ApiResponse(
                "Department count retrieved successfully",
                count
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Check if department exists
     */
    @GetMapping("/{departmentId}/exists")
    public ResponseEntity<ApiResponse> departmentExists(@PathVariable Long departmentId) {
        log.debug("GET /api/v1/departments/{}/exists", departmentId);

        boolean exists = departmentService.existsById(departmentId);

        ApiResponse apiResponse = new ApiResponse(
                "Department existence check completed",
                exists
        );

        return ResponseEntity.ok(apiResponse);
    }
}