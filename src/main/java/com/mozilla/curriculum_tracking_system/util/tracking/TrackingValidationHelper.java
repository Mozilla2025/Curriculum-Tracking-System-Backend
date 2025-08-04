package com.mozilla.curriculum_tracking_system.util.tracking;

import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.exception.UnauthorizedException;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.department.DepartmentRepository;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Helper class for validating tracking operations and role-based permissions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingValidationHelper {

    private final IAuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SchoolRepository schoolRepository;

    /**
     * Validate user has permission to perform write operations on tracking
     */
    public void validateWritePermission(String authToken, String operation) {
        validateTokenAndGetUser(authToken);

        if (!hasWritePermission(authToken)) {
            throw new UnauthorizedException("Only QA and Dean roles can perform " + operation + " operations");
        }
    }

    /**
     * Validate user can initiate tracking for a specific department
     */
    public User validateTrackingInitiation(String authToken, Long departmentId, Long schoolId) {
        User user = validateTokenAndGetUser(authToken);
        List<String> userRoles = authenticationService.getRolesFromToken(authToken);

        // QA can initiate tracking anywhere
        if (hasQARole(userRoles)) {
            return user;
        }

        // Dean can initiate tracking in their school
        if (hasDeanRole(userRoles)) {
            validateDeanSchoolAccess(user, schoolId);
            return user;
        }

        // Department Head can only initiate in their own department
        if (hasHODRole(userRoles)) {
            validateHODDepartmentAccess(user, departmentId, schoolId);
            return user;
        }

        throw new UnauthorizedException("Insufficient permissions to initiate tracking");
    }

    /**
     * Validate user can perform action at specific tracking stage
     */
    public void validateStageAction(String authToken, TrackingStage currentStage, CurriculumTracking tracking) {
        User user = validateTokenAndGetUser(authToken);
        List<String> userRoles = authenticationService.getRolesFromToken(authToken);

        if (!canPerformStageAction(userRoles, currentStage)) {
            throw new UnauthorizedException("Insufficient permissions to perform action at stage: " + currentStage.getDisplayName());
        }

        // Additional validation for Dean - can only act up to Dean Committee stage
        if (hasDeanRole(userRoles) && !hasQARole(userRoles)) {
            validateDeanStageLimit(currentStage);
            // Dean can only act on their school's trackings
            validateDeanSchoolAccess(user, tracking.getSchool().getId());
        }
    }

    /**
     * Validate user can assign tracking to another user
     */
    public void validateAssignmentPermission(String authToken, Long assigneeId) {
        validateWritePermission(authToken, "assignment");

        // Validate assignee exists and has appropriate role
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with ID: " + assigneeId));

        List<String> assigneeRoles = assignee.getRoles().stream()
                .map(Role::getName)
                .toList();

        if (!hasWritePermission(assigneeRoles)) {
            throw new BadRequestException("Cannot assign tracking to user without QA or Dean role");
        }
    }

    /**
     * Validate stage transition is allowed
     */
    public void validateStageTransition(TrackingStage fromStage, TrackingStage toStage) {
        if (fromStage == null || toStage == null) {
            throw new BadRequestException("Both from and to stages must be specified");
        }

        // Forward progression
        if (toStage == fromStage.getNextStage()) {
            return;
        }

        // Valid return stages
        TrackingStage[] validReturnStages = fromStage.getValidReturnStages();
        for (TrackingStage validStage : validReturnStages) {
            if (toStage == validStage) {
                return;
            }
        }

        throw new BadRequestException("Invalid stage transition from " + fromStage.getDisplayName() + " to " + toStage.getDisplayName());
    }

    /**
     * Check if user has write permissions (QA or Dean)
     */
    public boolean hasWritePermission(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            return false;
        }

        try {
            List<String> roles = authenticationService.getRolesFromToken(authToken);
            return hasWritePermission(roles);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if roles include write permissions
     */
    private boolean hasWritePermission(List<String> roles) {
        return hasQARole(roles) || hasDeanRole(roles);
    }

    /**
     * Check if user can perform action at specific stage
     */
    private boolean canPerformStageAction(List<String> userRoles, TrackingStage stage) {
        // QA can perform actions at any stage
        if (hasQARole(userRoles)) {
            return true;
        }

        // Dean can perform actions up to Dean Committee stage
        if (hasDeanRole(userRoles)) {
            return stage.ordinal() <= TrackingStage.DEAN_COMMITTEE.ordinal();
        }

        return false;
    }

    /**
     * Validate Dean stage limitations
     */
    private void validateDeanStageLimit(TrackingStage stage) {
        if (stage.ordinal() > TrackingStage.DEAN_COMMITTEE.ordinal()) {
            throw new UnauthorizedException("Dean role cannot perform actions beyond Dean Committee stage");
        }
    }

    /**
     * Validate Dean has access to specific school
     */
    private void validateDeanSchoolAccess(User user, Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        if (school.getDeanId() != null && !school.getDeanId().equals(user.getId())) {
            throw new UnauthorizedException("Dean can only access trackings in their assigned school");
        }
    }

    /**
     * Validate HOD has access to specific department and school
     */
    private void validateHODDepartmentAccess(User user, Long departmentId, Long schoolId) {
        Department department = departmentRepository.findByIdWithSchool(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        // Validate department belongs to specified school
        if (!department.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("Department does not belong to the specified school");
        }

        // Validate user is head of this department
        if (department.getHeadId() == null || !department.getHeadId().equals(user.getId())) {
            throw new UnauthorizedException("Department Head can only initiate tracking in their assigned department");
        }
    }

    /**
     * Validate token and get user
     */
    private User validateTokenAndGetUser(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            throw new UnauthorizedException("Authentication token is required");
        }

        if (!authenticationService.validateToken(authToken)) {
            throw new UnauthorizedException("Invalid or expired authentication token");
        }

        Long userId = authenticationService.getUserIdFromToken(authToken);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Check if user has QA role
     */
    private boolean hasQARole(List<String> roles) {
        return roles != null && roles.contains(RoleConstants.QA);
    }

    /**
     * Check if user has Dean role
     */
    private boolean hasDeanRole(List<String> roles) {
        return roles != null && roles.contains(RoleConstants.DEAN);
    }

    /**
     * Check if user has HOD role
     */
    private boolean hasHODRole(List<String> roles) {
        return roles != null && roles.contains(RoleConstants.HEAD_OF_DEPARTMENT);
    }

    /**
     * Validate entities exist and are accessible
     */
    public void validateEntitiesExist(Long schoolId, Long departmentId, Long academicLevelId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found with ID: " + schoolId);
        }

        Optional<Department> department = departmentRepository.findByIdWithSchool(departmentId);
        if (department.isEmpty()) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }

        if (!department.get().getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("Department does not belong to the specified school");
        }
    }
}