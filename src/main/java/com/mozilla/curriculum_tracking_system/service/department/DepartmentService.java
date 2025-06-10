package com.mozilla.curriculum_tracking_system.service.department;

import com.mozilla.curriculum_tracking_system.constants.CacheConstants;
import com.mozilla.curriculum_tracking_system.dto.department.CreateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentDto;
import com.mozilla.curriculum_tracking_system.dto.department.DepartmentPageResponse;
import com.mozilla.curriculum_tracking_system.dto.department.UpdateDepartmentRequest;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.exception.UnauthorizedException;
import com.mozilla.curriculum_tracking_system.mapper.DepartmentMapper;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.repository.department.DepartmentRepository;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.util.CacheKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DepartmentService implements IDepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SchoolRepository schoolRepository;
    private final DepartmentMapper departmentMapper;
    private final IAuthenticationService authenticationService;
    private final CacheKeyGenerator cacheKeyGenerator;

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENTS,
            key = "@cacheKeyGenerator.generateDepartmentPageableKey('all', #pageable)")
    public DepartmentPageResponse getAllDepartments(Pageable pageable) {
        log.debug("Fetching all departments from database - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Department> departmentPage = departmentRepository.findAll(pageable);
        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENTS_BY_SCHOOL,
            key = "@cacheKeyGenerator.generateDepartmentPageableKey('by_school', #pageable, #schoolId)")
    public DepartmentPageResponse getDepartmentsBySchoolId(Long schoolId, Pageable pageable) {
        log.debug("Fetching departments for school {} from database - Page: {}, Size: {}",
                schoolId, pageable.getPageNumber(), pageable.getPageSize());
        validateSchoolExists(schoolId);
        Page<Department> departmentPage = departmentRepository.findBySchoolId(schoolId, pageable);
        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENTS_SEARCH,
            key = "@cacheKeyGenerator.generateDepartmentSearchKey(#searchTerm, #pageable, null)")
    public DepartmentPageResponse searchDepartments(String searchTerm, Pageable pageable) {
        if (!StringUtils.hasText(searchTerm)) {
            return getAllDepartments(pageable);
        }

        log.debug("Searching departments with term '{}' from database - Page: {}, Size: {}",
                searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        Page<Department> departmentPage = departmentRepository
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(searchTerm.trim(), pageable);
        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENTS_SEARCH_BY_SCHOOL,
            key = "@cacheKeyGenerator.generateDepartmentSearchKey(#searchTerm, #pageable, #schoolId)")
    public DepartmentPageResponse searchDepartmentsBySchoolId(Long schoolId, String searchTerm, Pageable pageable) {
        validateSchoolExists(schoolId);

        if (!StringUtils.hasText(searchTerm)) {
            return getDepartmentsBySchoolId(schoolId, pageable);
        }

        log.debug("Searching departments for school {} with term '{}' from database - Page: {}, Size: {}",
                schoolId, searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        Page<Department> departmentPage = departmentRepository
                .findBySchoolIdAndNameContainingIgnoreCase(schoolId, searchTerm.trim(), pageable);

        return departmentMapper.buildDepartmentPageResponse(departmentPage);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENT_BY_ID,
            key = "@cacheKeyGenerator.generateSimpleKey(#departmentId)")
    public DepartmentDto getDepartmentById(Long departmentId) {
        log.debug("Fetching department {} from database", departmentId);
        Department department = departmentRepository.findByIdWithSchool(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        return departmentMapper.mapToDto(department);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.DEPARTMENTS, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_BY_SCHOOL, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_SEARCH_BY_SCHOOL, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENT_COUNT_BY_SCHOOL, allEntries = true)
    })
    public DepartmentDto createDepartment(CreateDepartmentRequest request, String token) {
        validateAdminAccess(token);
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

        Department savedDepartment = departmentRepository.save(department);
        log.info("Created new department: {} for school: {}", savedDepartment.getName(), school.getName());

        return departmentMapper.mapToDto(savedDepartment);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.DEPARTMENTS, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_BY_SCHOOL, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_SEARCH_BY_SCHOOL, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENT_BY_ID,
                    key = "@cacheKeyGenerator.generateSimpleKey(#departmentId)"),
            @CacheEvict(value = CacheConstants.DEPARTMENT_EXISTS,
                    key = "@cacheKeyGenerator.generateSimpleKey(#departmentId)"),
            @CacheEvict(value = CacheConstants.DEPARTMENT_COUNT_BY_SCHOOL, allEntries = true)
    })
    public DepartmentDto updateDepartment(Long departmentId, UpdateDepartmentRequest request, String token) {
        validateAdminAccess(token);

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
        log.info("Updated department: {} (ID: {})", updatedDepartment.getName(), departmentId);

        return departmentMapper.mapToDto(updatedDepartment);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.DEPARTMENTS, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_BY_SCHOOL, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENTS_SEARCH_BY_SCHOOL, allEntries = true),
            @CacheEvict(value = CacheConstants.DEPARTMENT_BY_ID,
                    key = "@cacheKeyGenerator.generateSimpleKey(#departmentId)"),
            @CacheEvict(value = CacheConstants.DEPARTMENT_EXISTS,
                    key = "@cacheKeyGenerator.generateSimpleKey(#departmentId)"),
            @CacheEvict(value = CacheConstants.DEPARTMENT_COUNT_BY_SCHOOL, allEntries = true)
    })
    public void deleteDepartment(Long departmentId, String token) {
        validateAdminAccess(token);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        if (!department.getCurriculums().isEmpty()) {
            throw new BadRequestException("Cannot delete department with associated curriculums. " +
                    "Please remove or reassign curriculums first.");
        }

        departmentRepository.delete(department);
        log.info("Deleted department: {} (ID: {})", department.getName(), departmentId);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENT_EXISTS,
            key = "@cacheKeyGenerator.generateSimpleKey(#departmentId)")
    public boolean existsById(Long departmentId) {
        log.debug("Checking if department {} exists", departmentId);
        return departmentRepository.existsById(departmentId);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENT_COUNT_BY_SCHOOL,
            key = "@cacheKeyGenerator.generateCountKey('department', #schoolId)")
    public long getDepartmentCountBySchoolId(Long schoolId) {
        log.debug("Getting department count for school {}", schoolId);
        validateSchoolExists(schoolId);
        return departmentRepository.countBySchoolId(schoolId);
    }

    // Validation methods
    @Cacheable(value = CacheConstants.SCHOOL_EXISTS,
            key = "@cacheKeyGenerator.generateSimpleKey(#schoolId)")
    private void validateSchoolExists(Long schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found with ID: " + schoolId);
        }
    }

    private void validateAdminAccess(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("Access token is required");
        }

        if (!authenticationService.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        if (!authenticationService.isAdmin(token)) {
            throw new UnauthorizedException("Admin access required for this operation");
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