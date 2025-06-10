package com.mozilla.curriculum_tracking_system.controller.department;

import com.mozilla.curriculum_tracking_system.annotation.AdminOnly;
import com.mozilla.curriculum_tracking_system.dto.department.CreateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.department.UpdateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/departments/admin")
@RequiredArgsConstructor
@Slf4j
public class DepartmentAdminController {
    private final DepartmentService departmentService;


    /**
     * Create a new department
     */
    @PostMapping("/create-department")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can create departments")
    public ResponseEntity<ApiResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("POST /api/v1/departments - request: {}", request);

        String token = extractToken(authorizationHeader);
        DepartmentDto createdDepartment = departmentService.createDepartment(request, token);

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
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can update departments")
    public ResponseEntity<ApiResponse> updateDepartment(
            @PathVariable Long departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("PUT /api/v1/departments/{} - request: {}", departmentId, request);

        String token = extractToken(authorizationHeader);
        DepartmentDto updatedDepartment = departmentService.updateDepartment(departmentId, request, token);

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
    @PreAuthorize("hasRole('ADMIN')")
    @AdminOnly(message = "Only administrators can delete departments")
    public ResponseEntity<ApiResponse> deleteDepartment(
            @PathVariable Long departmentId,
            @RequestHeader("Authorization") String authorizationHeader) {

        log.debug("DELETE /api/v1/departments/{}", departmentId);

        String token = extractToken(authorizationHeader);
        departmentService.deleteDepartment(departmentId, token);

        ApiResponse apiResponse = new ApiResponse(
                "Department deleted successfully",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }


    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}
