package com.mozilla.curriculum_tracking_system.service.curriculum;

import com.mozilla.curriculum_tracking_system.dto.curriculum.*;
import com.mozilla.curriculum_tracking_system.enums.CurriculumStatus;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.exception.UnauthorizedException;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumMapper;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.repository.academic.AcademicLevelRepository;
import com.mozilla.curriculum_tracking_system.repository.curriculum.CurriculumRepository;
import com.mozilla.curriculum_tracking_system.repository.department.DepartmentRepository;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.specification.CurriculumSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumService implements ICurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final SchoolRepository schoolRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final CurriculumMapper curriculumMapper;
    private final IAuthenticationService authenticationService;

    @Override
    public CurriculumDto createCurriculum(CreateCurriculumRequest request, String authToken) {
        validateAdminAccess(authToken);
        validateCreateRequest(request);

        if (curriculumRepository.findByNameAndDepartmentIdAndAcademicLevelId(
                request.getName(), request.getDepartmentId(), request.getAcademicLevelId()
        ).isPresent()) {
            throw new BadRequestException("Curriculum with this name already exists in the specified department and academic level");
        }

        if (StringUtils.hasText(request.getCode()) && curriculumRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Curriculum with this code already exists");
        }

        School school = findSchoolById(request.getSchoolId());
        Department department = findDepartmentById(request.getDepartmentId());
        AcademicLevel academicLevel = findAcademicLevelById(request.getAcademicLevelId());

        // Validate that department belongs to the school
        if (!department.getSchool().getId().equals(school.getId())) {
            throw new BadRequestException("Department does not belong to the specified school");
        }

        // Create curriculum Entity
        Curriculum curriculum = curriculumMapper.toEntity(request, school, department, academicLevel);

        // Save Curriculum
        Curriculum savedCurriculum = curriculumRepository.save(curriculum);

        return curriculumMapper.toDto(savedCurriculum);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumDto getCurriculumById(Long id) {
        Curriculum curriculum = curriculumRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found"));

        return curriculumMapper.toDto(curriculum);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumPageResponse getAllCurriculums(Pageable pageable) {
        Page<Curriculum> curriculumPage = curriculumRepository.findAll(pageable);
        return curriculumMapper.buildCurriculumPageResponse(curriculumPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumPageResponse searchCurriculums(CurriculumSearchCriteria criteria, Pageable pageable) {
        Page<Curriculum> curriculumPage = curriculumRepository.findAll(
                CurriculumSpecification.withCriteria(criteria), pageable
        );
        return curriculumMapper.buildCurriculumPageResponse(curriculumPage);
    }

    @Override
    public CurriculumDto updateCurriculum(Long id, UpdateCurriculumRequest request, String authToken) {
        validateAdminAccess(authToken);

        Curriculum curriculum = findCurriculumById(id);
        validateUpdateRequest(curriculum, request);

        Department department = null;
        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(curriculum.getDepartment().getId())) {
            department = findDepartmentById(request.getDepartmentId());

            // Validate that department belongs to the same school
            if (!department.getSchool().getId().equals(curriculum.getSchool().getId())) {
                throw new BadRequestException("Department does not belong to the curriculum's school");
            }
        }

        AcademicLevel academicLevel = null;
        if (request.getAcademicLevelId() != null && !request.getAcademicLevelId().equals(curriculum.getAcademicLevel().getId())) {
            academicLevel = findAcademicLevelById(request.getAcademicLevelId());
        }

        curriculumMapper.updateEntityFromRequest(curriculum, request, department, academicLevel);

        Curriculum updatedCurriculum = curriculumRepository.save(curriculum);
        return curriculumMapper.toDto(updatedCurriculum);
    }

    @Override
    public void deleteCurriculum(Long id, String authToken) {
        validateAdminAccess(authToken);

        // Soft deletion...Just inactivate the curriculum
        Curriculum curriculum = findCurriculumById(id);
        curriculum.setActive(false);
        curriculumRepository.save(curriculum);
    }

    @Override
    public void permanentlyDeleteCurriculum(Long id, String authToken) {
        validateAdminAccess(authToken);

        if (!curriculumRepository.existsById(id)) {
            throw new ResourceNotFoundException("Curriculum not found");
        }

        curriculumRepository.deleteById(id);
    }

    @Override
    public CurriculumDto putCurriculumUnderReview(Long id, String authToken) {
        validateAdminAccess(authToken);

        Curriculum curriculum = findCurriculumById(id);
        curriculum.putUnderReview();

        Curriculum savedCurriculum = curriculumRepository.save(curriculum);
        return curriculumMapper.toDto(savedCurriculum);
    }

    @Override
    public CurriculumDto toggleCurriculumStatus(Long id, String authToken) {
        validateAdminAccess(authToken);

        Curriculum curriculum = findCurriculumById(id);
        curriculum.setActive(!curriculum.isActive());

        Curriculum savedCurriculum = curriculumRepository.save(curriculum);
        return curriculumMapper.toDto(savedCurriculum);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumPageResponse getCurriculumsBySchool(Long schoolId, Pageable pageable) {
        findSchoolById(schoolId);

        Page<Curriculum> curriculumPage = curriculumRepository.findBySchoolId(schoolId, pageable);
        return curriculumMapper.buildCurriculumPageResponse(curriculumPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumPageResponse getCurriculumsByDepartment(Long departmentId, Pageable pageable) {
        findDepartmentById(departmentId);

        Page<Curriculum> curriculumPage = curriculumRepository.findByDepartmentId(departmentId, pageable);
        return curriculumMapper.buildCurriculumPageResponse(curriculumPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumPageResponse getCurriculumsByAcademicLevel(Long academicLevelId, Pageable pageable) {
        findAcademicLevelById(academicLevelId);

        Page<Curriculum> curriculumPage = curriculumRepository.findByAcademicLevelId(academicLevelId, pageable);
        return curriculumMapper.buildCurriculumPageResponse(curriculumPage);
    }

    @Override
    public CurriculumStatusStats getCurriculumStats() {
        return CurriculumStatusStats.builder()
                .totalCurriculums(curriculumRepository.count())
                .pendingCurriculums(curriculumRepository.countByStatus(CurriculumStatus.PENDING))
                .approvedCurriculums(curriculumRepository.countByStatus(CurriculumStatus.APPROVED))
                .rejectedCurriculums(curriculumRepository.countByStatus(CurriculumStatus.REJECTED))
                .underReviewCurriculums(curriculumRepository.countByStatus(CurriculumStatus.UNDER_REVIEW))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumDto> getCurriculumsExpiringSoon(int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);

        List<Curriculum> curriculums = curriculumRepository.findCurriculumsExpiringSoon(startDate, endDate);
        return curriculumMapper.toDtoList(curriculums);
    }

    private void validateAdminAccess(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            throw new UnauthorizedException("Authorization token is required");
        }


        if (!authenticationService.validateToken(authToken)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        if (!authenticationService.isAdmin(authToken)) {
            throw new UnauthorizedException("Admin access required for this operation");
        }
    }

    private Curriculum findCurriculumById(Long id) {
        return curriculumRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found with ID: " + id));
    }

    private School findSchoolById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + id));
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
    }

    private AcademicLevel findAcademicLevelById(Long id) {
        return academicLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic level not found with ID: " + id));
    }

    private void validateCreateRequest(CreateCurriculumRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new BadRequestException("Curriculum name is required");
        }

        if (request.getSchoolId() == null) {
            throw new BadRequestException("School ID is required");
        }

        if (request.getDepartmentId() == null) {
            throw new BadRequestException("Department ID is required");
        }

        if (request.getAcademicLevelId() == null) {
            throw new BadRequestException("Academic level ID is required");
        }

        if (request.getDurationSemesters() != null && request.getDurationSemesters() <= 0) {
            throw new BadRequestException("Duration semesters must be positive");
        }

        if (request.getEffectiveDate() != null && request.getExpiryDate() != null
                && request.getEffectiveDate().isAfter(request.getExpiryDate())) {
            throw new BadRequestException("Effective date cannot be after expiry date");
        }
    }

    private void validateUpdateRequest(Curriculum curriculum, UpdateCurriculumRequest request) {
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(curriculum.getName())) {
            Long departmentId = request.getDepartmentId() != null ? request.getDepartmentId() : curriculum.getDepartment().getId();
            Long academicLevelId = request.getAcademicLevelId() != null ? request.getAcademicLevelId() : curriculum.getAcademicLevel().getId();

            if (curriculumRepository.existsByNameAndDepartmentIdAndAcademicLevelIdAndIdNot(
                    request.getName(), departmentId, academicLevelId, curriculum.getId())) {
                throw new BadRequestException("Curriculum with this name already exists in the specified department and academic level");
            }
        }

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(curriculum.getCode())) {
            if (curriculumRepository.existsByCodeAndIdNot(request.getCode(), curriculum.getId())) {
                throw new BadRequestException("Curriculum with this code already exists");
            }
        }

        if (request.getDurationSemesters() != null && request.getDurationSemesters() <= 0) {
            throw new BadRequestException("Duration semesters must be positive");
        }

        if (request.getEffectiveDate() != null && request.getExpiryDate() != null
                && request.getEffectiveDate().isAfter(request.getExpiryDate())) {
            throw new BadRequestException("Effective date cannot be after expiry date");
        }
    }
}