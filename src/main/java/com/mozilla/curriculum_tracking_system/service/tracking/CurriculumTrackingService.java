package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDetailDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.InitiateTrackingRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingActionRequest;
import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.tracking.CurriculumTrackingMapper;
import com.mozilla.curriculum_tracking_system.mapper.tracking.TrackingStepMapper;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.academic.AcademicLevelRepository;
import com.mozilla.curriculum_tracking_system.repository.curriculum.CurriculumRepository;
import com.mozilla.curriculum_tracking_system.repository.department.DepartmentRepository;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingRepository;
import com.mozilla.curriculum_tracking_system.repository.tracking.TrackingStepRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.util.specifications.TrackingSpecification;
import com.mozilla.curriculum_tracking_system.util.tracking.TrackingIdGenerator;
import com.mozilla.curriculum_tracking_system.util.tracking.TrackingValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of curriculum tracking service that handles the complete lifecycle
 * of curriculum tracking from initiation to completion
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CurriculumTrackingService implements ICurriculumTrackingService {

    private final CurriculumTrackingRepository trackingRepository;
    private final TrackingStepRepository stepRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final CurriculumRepository curriculumRepository;

    private final CurriculumTrackingMapper trackingMapper;
    private final TrackingStepMapper stepMapper;

    private final IAuthenticationService authenticationService;
    private final TrackingValidationHelper validationHelper;
    private final ITrackingDocumentStorageService documentStorageService;
    private final TrackingIdGenerator trackingIdGenerator;

    @Override
    public CurriculumTrackingDetailDto initiateTracking(InitiateTrackingRequest request, String authToken) {
        log.info("Initiating curriculum tracking for department: {}, school: {}",
                request.getDepartmentId(), request.getSchoolId());

        validateInitiateRequest(request);

        // Validate user permissions for tracking initiation
        User initiatedBy = validationHelper.validateTrackingInitiation(
                authToken, request.getDepartmentId(), request.getSchoolId());

        // Validate entities exist and relationships are correct
        validationHelper.validateEntitiesExist(
                request.getSchoolId(), request.getDepartmentId(), request.getAcademicLevelId());

        // Load required entities
        School school = findSchoolById(request.getSchoolId());
        Department department = findDepartmentById(request.getDepartmentId());
        AcademicLevel academicLevel = findAcademicLevelById(request.getAcademicLevelId());

        // Check for duplicate ideation tracking
        validateNoDuplicateIdeationTracking(request);

        // Determine initial assignee based on stage and roles
        User initialAssignee = determineInitialAssignee(school, department);

        // Link to existing curriculum if provided
        Curriculum linkedCurriculum = null;
        if (request.getCurriculumId() != null) {
            linkedCurriculum = findCurriculumById(request.getCurriculumId());
            validateCurriculumCompatibility(linkedCurriculum, department, academicLevel);
        }

        // Create tracking entity
        CurriculumTracking tracking = trackingMapper.toEntity(
                request, school, department, academicLevel, initiatedBy, initialAssignee);

        if (linkedCurriculum != null) {
            tracking.linkCurriculum(linkedCurriculum);
        }

        String generateTrackingId = generateTrackingId(request, school, department, linkedCurriculum);
        tracking.setGeneratedTrackingId(generateTrackingId);

        // Save tracking
        CurriculumTracking savedTracking = trackingRepository.save(tracking);

        // Create initial tracking step
        createInitialTrackingStep(savedTracking, initiatedBy, initialAssignee, request.getInitialNotes());

        // Handle document uploads if provided
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            uploadInitialDocuments(savedTracking, request, initiatedBy.getId());
        }

        log.info("Successfully initiated tracking with ID: {} for user: {}",
                savedTracking.getTrackingId(), initiatedBy.getUsername());

        return trackingMapper.toDetailDto(savedTracking);
    }

    @Override
    public CurriculumTrackingDetailDto performTrackingAction(TrackingActionRequest request, String authToken) {
        log.info("Performing tracking action: {} on tracking: {}",
                request.getAction(), request.getTrackingId());

        validateActionRequest(request);

        CurriculumTracking tracking = findTrackingByIdWithDetails(request.getTrackingId());

        // Validate user can perform action at current stage
        validationHelper.validateStageAction(authToken, tracking.getCurrentStage(), tracking);

        // Get current user
        Long userId = authenticationService.getUserIdFromToken(authToken);
        User performedBy = findUserById(userId);

        // Process the action
        processTrackingAction(tracking, request, performedBy);

        // Save updated tracking
        CurriculumTracking updatedTracking = trackingRepository.save(tracking);

        log.info("Successfully performed action: {} on tracking: {} by user: {}",
                request.getAction(), tracking.getTrackingId(), performedBy.getUsername());

        return trackingMapper.toDetailDto(updatedTracking);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingDetailDto getTrackingById(Long trackingId) {
        log.debug("Fetching tracking by ID: {}", trackingId);

        CurriculumTracking tracking = findTrackingByIdWithDetails(trackingId);
        CurriculumTrackingDetailDto detailDto = trackingMapper.toDetailDto(tracking);

        // Add recent steps
        List<TrackingStep> recentSteps = stepRepository.findRecentStepsByTrackingId(trackingId, 5);
        detailDto.setRecentSteps(stepMapper.toDtoList(recentSteps));

        return detailDto;
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingDetailDto getTrackingByTrackingId(String trackingId) {
        log.debug("Fetching tracking by tracking ID: {}", trackingId);

        CurriculumTracking tracking = trackingRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with ID: " + trackingId));

        return getTrackingById(tracking.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getAllTrackings(Pageable pageable) {
        log.debug("Fetching all trackings with pagination: {}", pageable);

        Specification<CurriculumTracking> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);
        Page<CurriculumTracking> trackingsPage = trackingRepository.findAll(spec, pageable);

        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse searchTrackings(TrackingSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching trackings with criteria: {}", criteria);

        Specification<CurriculumTracking> spec = TrackingSpecification.withCriteria(criteria);
        Page<CurriculumTracking> trackingsPage = trackingRepository.findAll(spec, pageable);

        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsByStatus(TrackingStatus status, Pageable pageable) {
        log.debug("Fetching trackings by status: {}", status);

        Page<CurriculumTracking> trackingsPage = trackingRepository.findByStatusAndActiveTrue(status, pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsByStage(TrackingStage stage, Pageable pageable) {
        log.debug("Fetching trackings by stage: {}", stage);

        Page<CurriculumTracking> trackingsPage = trackingRepository.findByCurrentStageAndActiveTrue(stage, pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsByAssignee(Long userId, Pageable pageable) {
        log.debug("Fetching trackings by assignee: {}", userId);

        validateUserExists(userId);
        Page<CurriculumTracking> trackingsPage = trackingRepository.findByCurrentAssigneeIdAndActiveTrue(userId, pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsByInitiator(Long userId, Pageable pageable) {
        log.debug("Fetching trackings by initiator: {}", userId);

        validateUserExists(userId);
        Page<CurriculumTracking> trackingsPage = trackingRepository.findByInitiatedByIdAndActiveTrue(userId, pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsBySchool(Long schoolId, Pageable pageable) {
        log.debug("Fetching trackings by school: {}", schoolId);

        validateSchoolExists(schoolId);
        Page<CurriculumTracking> trackingsPage = trackingRepository.findBySchoolIdAndActiveTrue(schoolId, pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsByDepartment(Long departmentId, Pageable pageable) {
        log.debug("Fetching trackings by department: {}", departmentId);

        validateDepartmentExists(departmentId);
        Page<CurriculumTracking> trackingsPage = trackingRepository.findByDepartmentIdAndActiveTrue(departmentId, pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getIdeationTrackings(Pageable pageable) {
        log.debug("Fetching ideation trackings");

        Page<CurriculumTracking> trackingsPage = trackingRepository.findIdeationTrackings(pageable);
        return trackingMapper.toPageResponse(trackingsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getOverdueTrackings(Pageable pageable) {
        log.debug("Fetching overdue trackings");

        List<CurriculumTracking> overdueTrackings = trackingRepository.findOverdueTrackings(LocalDateTime.now());

        TrackingSearchCriteria criteria = TrackingSearchCriteria.builder()
                .isOverdue(true)
                .build();

        return searchTrackings(criteria, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getTrackingsExpiringSoon(int days, Pageable pageable) {
        log.debug("Fetching trackings expiring in {} days", days);

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);

        List<CurriculumTracking> expiringSoon = trackingRepository.findTrackingsExpiringSoon(startDate, endDate);

        TrackingSearchCriteria criteria = TrackingSearchCriteria.builder()
                .expectedCompletionBefore(endDate)
                .build();

        return searchTrackings(criteria, pageable);
    }

    @Override
    public CurriculumTrackingDetailDto updateTracking(Long trackingId, InitiateTrackingRequest request, String authToken) {
        log.info("Updating tracking: {}", trackingId);

        validationHelper.validateWritePermission(authToken, "update tracking");

        CurriculumTracking tracking = findTrackingByIdWithDetails(trackingId);

        if (request.getDepartmentId() != null || request.getAcademicLevelId() != null) {
            validationHelper.validateEntitiesExist(
                    tracking.getSchool().getId(),
                    request.getDepartmentId() != null ? request.getDepartmentId() : tracking.getDepartment().getId(),
                    request.getAcademicLevelId() != null ? request.getAcademicLevelId() : tracking.getAcademicLevel().getId());
        }

        Department newDepartment = request.getDepartmentId() != null ?
                findDepartmentById(request.getDepartmentId()) : null;
        AcademicLevel newAcademicLevel = request.getAcademicLevelId() != null ?
                findAcademicLevelById(request.getAcademicLevelId()) : null;

        trackingMapper.updateEntityFromRequest(tracking, request, newDepartment, newAcademicLevel);

        CurriculumTracking updatedTracking = trackingRepository.save(tracking);

        log.info("Successfully updated tracking: {}", trackingId);
        return trackingMapper.toDetailDto(updatedTracking);
    }

    @Override
    public void deactivateTracking(Long trackingId, String authToken) {
        log.info("Deactivating tracking: {}", trackingId);

        validationHelper.validateWritePermission(authToken, "deactivate tracking");

        CurriculumTracking tracking = findTrackingByIdWithDetails(trackingId);
        tracking.setIsActive(false);

        trackingRepository.save(tracking);
        log.info("Successfully deactivated tracking: {}", trackingId);
    }

    @Override
    public CurriculumTrackingDetailDto reactivateTracking(Long trackingId, String authToken) {
        log.info("Reactivating tracking: {}", trackingId);

        validationHelper.validateWritePermission(authToken, "reactivate tracking");

        CurriculumTracking tracking = trackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with ID: " + trackingId));

        tracking.setIsActive(true);
        CurriculumTracking reactivatedTracking = trackingRepository.save(tracking);

        log.info("Successfully reactivated tracking: {}", trackingId);
        return trackingMapper.toDetailDto(reactivatedTracking);
    }

    @Override
    public CurriculumTrackingDetailDto assignTracking(Long trackingId, Long assigneeId, String authToken) {
        log.info("Assigning tracking: {} to user: {}", trackingId, assigneeId);

        validationHelper.validateAssignmentPermission(authToken, assigneeId);

        CurriculumTracking tracking = findTrackingByIdWithDetails(trackingId);
        User newAssignee = findUserById(assigneeId);

        tracking.setCurrentAssignee(newAssignee);
        CurriculumTracking updatedTracking = trackingRepository.save(tracking);

        log.info("Successfully assigned tracking: {} to user: {}", trackingId, newAssignee.getUsername());
        return trackingMapper.toDetailDto(updatedTracking);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTrackingPermission(Long trackingId, Long userId) {
        log.debug("Checking tracking permission for user: {} on tracking: {}", userId, trackingId);

        try {
            CurriculumTracking tracking = findTrackingByIdWithDetails(trackingId);
            User user = findUserById(userId);

            // Users can access trackings they initiated
            if (tracking.getInitiatedBy().getId().equals(userId)) {
                return true;
            }

            // Users can access trackings assigned to them
            if (tracking.getCurrentAssignee() != null && tracking.getCurrentAssignee().getId().equals(userId)) {
                return true;
            }

            return true;
        } catch (Exception e) {
            log.warn("Error checking tracking permission for user: {} on tracking: {}", userId, trackingId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateStageTransition(Long trackingId, TrackingStage targetStage, String authToken) {
        log.debug("Validating stage transition for tracking: {} to stage: {}", trackingId, targetStage);

        try {
            CurriculumTracking tracking = findTrackingByIdWithDetails(trackingId);
            validationHelper.validateStageTransition(tracking.getCurrentStage(), targetStage);
            validationHelper.validateStageAction(authToken, targetStage, tracking);
            return true;
        } catch (Exception e) {
            log.warn("Stage transition validation failed for tracking: {} to stage: {}", trackingId, targetStage, e);
            return false;
        }
    }


    private void validateInitiateRequest(InitiateTrackingRequest request) {
        if (request == null) {
            throw new BadRequestException("Initiate tracking request cannot be null");
        }
        if (!StringUtils.hasText(request.getProposedCurriculumName())) {
            throw new BadRequestException("Proposed curriculum name is required");
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
    }

    private void validateActionRequest(TrackingActionRequest request) {
        if (request == null) {
            throw new BadRequestException("Tracking action request cannot be null");
        }
        if (request.getTrackingId() == null) {
            throw new BadRequestException("Tracking ID is required");
        }
        if (request.getAction() == null) {
            throw new BadRequestException("Action is required");
        }
    }

    private void validateNoDuplicateIdeationTracking(InitiateTrackingRequest request) {
        if (trackingRepository.existsByProposedCurriculumNameAndDepartmentIdAndAcademicLevelIdAndCurriculumIsNullAndIsActiveTrue(
                request.getProposedCurriculumName(), request.getDepartmentId(), request.getAcademicLevelId())) {
            throw new BadRequestException("An active ideation tracking with this name already exists for the specified department and academic level");
        }
    }

    private void validateCurriculumCompatibility(Curriculum curriculum, Department department, AcademicLevel academicLevel) {
        if (!curriculum.getDepartment().getId().equals(department.getId())) {
            throw new BadRequestException("Curriculum department does not match tracking department");
        }
        if (!curriculum.getAcademicLevel().getId().equals(academicLevel.getId())) {
            throw new BadRequestException("Curriculum academic level does not match tracking academic level");
        }
    }

    private User determineInitialAssignee(School school, Department department) {
        if (department.getHeadId() != null) {
            return findUserById(department.getHeadId());
        }

        // Fallback to dean if available
        if (school.getDeanId() != null) {
            return findUserById(school.getDeanId());
        }

        return null;
    }

    private void createInitialTrackingStep(CurriculumTracking tracking, User initiatedBy, User assignedTo, String notes) {
        TrackingStep initialStep = TrackingStep.builder()
                .tracking(tracking)
                .stage(TrackingStage.IDEATION)
                .action(TrackingAction.INITIATE)
                .performedBy(initiatedBy)
                .assignedTo(assignedTo)
                .notes(notes)
                .isMilestone(true)
                .build();

        stepRepository.save(initialStep);
    }

    private void uploadInitialDocuments(CurriculumTracking tracking, InitiateTrackingRequest request, Long uploadedBy) {
        try {
            TrackingStep initialStep = stepRepository.findLatestStepByTrackingId(tracking.getId())
                    .orElseThrow(() -> new RuntimeException("Initial step not found"));

            documentStorageService.uploadDocuments(
                    request.getDocuments(),
                    tracking.getId(),
                    initialStep.getId(),
                    com.mozilla.curriculum_tracking_system.enums.DocumentType.CURRICULUM_PROPOSAL,
                    null,
                    uploadedBy
            );
        } catch (Exception e) {
            log.error("Failed to upload initial documents for tracking: {}", tracking.getId(), e);
        }
    }

    private void processTrackingAction(CurriculumTracking tracking, TrackingActionRequest request, User performedBy) {
        User assignedTo = request.getAssignToUserId() != null ? findUserById(request.getAssignToUserId()) : null;

        TrackingStep step = stepMapper.toEntity(request, tracking, performedBy, assignedTo);

        switch (request.getAction()) {
            case APPROVE -> processApprovalAction(tracking, step);
            case REJECT -> processRejectionAction(tracking, step);
            case RETURN -> processReturnAction(tracking, step, request.getReturnToStage());
            case SUBMIT -> processSubmitAction(tracking, step);
            case COMPLETE -> processCompleteAction(tracking, step);
            default -> throw new BadRequestException("Unsupported action: " + request.getAction());
        }

        if (assignedTo != null) {
            tracking.setCurrentAssignee(assignedTo);
        }

        stepRepository.save(step);

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            uploadStepDocuments(step, request, performedBy.getId());
        }
    }

    private void processApprovalAction(CurriculumTracking tracking, TrackingStep step) {
        TrackingStage currentStage = tracking.getCurrentStage();
        TrackingStage nextStage = currentStage.getNextStage();

        tracking.moveToNextStage();

        step.setFromStage(currentStage);
        step.setToStage(nextStage);

        if (nextStage == TrackingStage.ACCREDITED) {
            tracking.setStatus(TrackingStatus.COMPLETED);
            step.setIsMilestone(true);
        } else {
            tracking.setStatus(TrackingStatus.IN_PROGRESS);
        }
    }

    private void processRejectionAction(CurriculumTracking tracking, TrackingStep step) {
        tracking.setStatus(TrackingStatus.REJECTED);
        step.setIsMilestone(true);
    }

    private void processReturnAction(CurriculumTracking tracking, TrackingStep step, TrackingStage returnToStage) {
        if (returnToStage == null) {
            throw new BadRequestException("Return to stage must be specified for return actions");
        }

        validationHelper.validateStageTransition(tracking.getCurrentStage(), returnToStage);

        TrackingStage currentStage = tracking.getCurrentStage();
        tracking.returnToStage(returnToStage);

        step.setFromStage(currentStage);
        step.setToStage(returnToStage);
        step.setIsMilestone(true);
    }

    private void processSubmitAction(CurriculumTracking tracking, TrackingStep step) {
        if (tracking.getStatus() == TrackingStatus.INITIATED) {
            tracking.setStatus(TrackingStatus.IN_PROGRESS);
        }
    }

    private void processCompleteAction(CurriculumTracking tracking, TrackingStep step) {
        tracking.setStatus(TrackingStatus.COMPLETED);
        tracking.setActualCompletionDate(LocalDateTime.now());
        step.setIsMilestone(true);
    }

    private void uploadStepDocuments(TrackingStep step, TrackingActionRequest request, Long uploadedBy) {
        try {
            documentStorageService.uploadDocuments(
                    request.getDocuments(),
                    step.getTracking().getId(),
                    step.getId(),
                    com.mozilla.curriculum_tracking_system.enums.DocumentType.SUPPORTING_DOCUMENTS,
                    null,
                    uploadedBy
            );
        } catch (Exception e) {
            log.error("Failed to upload step documents for step: {}", step.getId(), e);
        }
    }


    private CurriculumTracking findTrackingByIdWithDetails(Long trackingId) {
        return trackingRepository.findByIdWithDetails(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with ID: " + trackingId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private School findSchoolById(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
    }

    private Department findDepartmentById(Long departmentId) {
        return departmentRepository.findByIdWithSchool(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
    }

    private AcademicLevel findAcademicLevelById(Long academicLevelId) {
        return academicLevelRepository.findById(academicLevelId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic level not found with ID: " + academicLevelId));
    }

    private Curriculum findCurriculumById(Long curriculumId) {
        return curriculumRepository.findByIdWithAssociations(curriculumId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found with ID: " + curriculumId));
    }


    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    private void validateSchoolExists(Long schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found with ID: " + schoolId);
        }
    }

    private void validateDepartmentExists(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }
    }

    /**
     * Generate tracking ID based on the type of tracking (ideation vs existing curriculum)
     */
    private String generateTrackingId(InitiateTrackingRequest request, School school, Department department, Curriculum linkedCurriculum) {
        if (linkedCurriculum != null) {
            // For exiting curriculum
            return trackingIdGenerator.generateTrackingId(
                    linkedCurriculum.getCode(),
                    department.getCode()
            );
        } else {
            // Ideation stage
            return trackingIdGenerator.generateIdeationTrackingId(
                    request.getProposedCurriculumCode(),
                    department.getCode(),
                    school.getCode()
            );
        }
    }

}
