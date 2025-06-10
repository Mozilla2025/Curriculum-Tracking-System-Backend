package com.mozilla.curriculum_tracking_system.service.department;

import com.mozilla.curriculum_tracking_system.dto.department.CreateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentPageResponse;
import com.mozilla.curriculum_tracking_system.dto.department.UpdateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.DepartmentMapper;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.repository.department.DepartmentRepository;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService implements IDepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SchoolRepository schoolRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentPageResponse getAllDepartments(Pageable pageable) {
        Page<Department> departmentPage = departmentRepository.findAll(pageable);
        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    public DepartmentPageResponse getDepartmentsBySchoolId(Long schoolId, Pageable pageable) {
        validateSchoolExists(schoolId);
        Page<Department> departmentPage = departmentRepository.findBySchoolId(schoolId, pageable);
        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    public DepartmentPageResponse searchDepartments(String searchTerm, Pageable pageable) {
        if (!StringUtils.hasText(searchTerm)) {
            return getAllDepartments(pageable);
        }
        Page<Department> departmentPage = departmentRepository
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(searchTerm.trim(), pageable);
        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    public DepartmentPageResponse searchDepartmentsBySchoolId(Long schoolId, String searchTerm, Pageable pageable) {
        validateSchoolExists(schoolId);

        if (!StringUtils.hasText(searchTerm)) {
            return getDepartmentsBySchoolId(schoolId, pageable);
        }

        Page<Department> departmentPage = departmentRepository
                .findBySchoolIdAndNameContainingIgnoreCase(schoolId, searchTerm.trim(), pageable);

        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    public DepartmentDto getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findByIdWithSchool(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        return departmentMapper.mapToDto(department);
    }

    @Override
    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request) {

        validateCreateDepartmentRequest(request);

        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + request.getSchoolId()));

        if (!school.isActive()) {
            throw new BadRequestException("Cannot create department for inactive school");
        }

        // Check for duplicate name
        if (departmentRepository.existsByNameAndSchoolId(request.getName(), request.getSchoolId())) {
            throw new BadRequestException("Department with name '" + request.getName() + "' already exists in this school");
        }

        // Check for duplicate code if provided
        if (StringUtils.hasText(request.getCode()) &&
                departmentRepository.existsByCodeAndSchoolId(request.getCode(), request.getSchoolId())) {
            throw new BadRequestException("Department with code '" + request.getCode() + "' already exists in this school");
        }

        Department department = Department.builder()
                .name(request.getName().trim())
                .code(StringUtils.hasText(request.getCode()) ? request.getCode().trim().toUpperCase() : null)
                .headId(request.getHeadId())
                .school(school)
                .build();

        return departmentMapper.mapToDto(department);
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long departmentId, UpdateDepartmentRequest request) {

        Department department = departmentRepository.findByIdWithSchool(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        validateUpdateDepartmentRequest(department, request);

        if (StringUtils.hasText(request.getName())) {
            String newName = request.getName().trim();
            if (!newName.equals(department.getName()) &&
                    departmentRepository.existsByNameAndSchoolId(newName, department.getSchool().getId())) {
                throw new BadRequestException("Department with name '" + newName + "' already exists in this school");
            }
            department.setName(newName);
        }

        if (request.getCode() != null) {
            String newCode = StringUtils.hasText(request.getCode()) ? request.getCode().trim().toUpperCase() : null;
            if (newCode != null && !newCode.equals(department.getCode()) &&
                    departmentRepository.existsByCodeAndSchoolId(newCode, department.getSchool().getId())) {
                throw new BadRequestException("Department with code '" + newCode + "' already exists in this school");
            }
            department.setCode(newCode);
        }

        if (request.getHeadId() != null) {
            department.setHeadId(request.getHeadId());
        }

        Department updatedDepartment = departmentRepository.save(department);

        return departmentMapper.mapToDto(updatedDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        if (!department.getCurriculums().isEmpty()) {
            throw new BadRequestException("Cannot delete department with associated curriculums. " +
                    "Please remove or reassign curriculums first.");
        }

        departmentRepository.delete(department);
    }

    @Override
    public boolean existsById(Long departmentId) {
        return departmentRepository.existsById(departmentId);
    }

    @Override
    public long getDepartmentCountBySchoolId(Long schoolId) {
        validateSchoolExists(schoolId);
        return departmentRepository.countBySchoolId(schoolId);
    }


    private void validateSchoolExists(Long schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found with ID: " + schoolId);
        }
    }

    private void validateCreateDepartmentRequest(CreateDepartmentRequest request) {
        if (request == null) {
            throw new BadRequestException("Create department request cannot be null");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new BadRequestException("Department name is required");
        }

        if (request.getName().trim().length() > 100) {
            throw new BadRequestException("Department name must not exceed 100 characters");
        }

        if (StringUtils.hasText(request.getCode()) && request.getCode().trim().length() > 10) {
            throw new BadRequestException("Department code must not exceed 10 characters");
        }

        if (request.getSchoolId() == null) {
            throw new BadRequestException("School ID is required");
        }
    }

    private void validateUpdateDepartmentRequest(Department department, UpdateDepartmentRequest request) {
        if (request == null) {
            throw new BadRequestException("Update department request cannot be null");
        }

        if (StringUtils.hasText(request.getName()) && request.getName().trim().length() > 100) {
            throw new BadRequestException("Department name must not exceed 100 characters");
        }

        if (StringUtils.hasText(request.getCode()) && request.getCode().trim().length() > 10) {
            throw new BadRequestException("Department code must not exceed 10 characters");
        }
    }
}
