package com.mozilla.curriculum_tracking_system.mapper.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDetailDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingOverviewDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.InitiateTrackingRequest;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for CurriculumTracking entities and DTOs
 * Handles conversions between tracking entities and their corresponding DTOs
 */
@Component
public class CurriculumTrackingMapper {

    /**
     * Convert CurriculumTracking entity to detailed DTO
     */
    public CurriculumTrackingDetailDto toDetailDto(CurriculumTracking tracking) {
        if (tracking == null) {
            return null;
        }

        return CurriculumTrackingDetailDto.builder()
                .id(tracking.getId())
                .trackingId(tracking.getTrackingId())
                .curriculumId(tracking.getCurriculum() != null ? tracking.getCurriculum().getId() : null)
                .curriculumName(tracking.getCurriculum() != null ? tracking.getCurriculum().getName() : null)
                .curriculumCode(tracking.getCurriculum() != null ? tracking.getCurriculum().getCode() : null)
                .displayCurriculumName(tracking.getCurriculumDisplayName())
                .displayCurriculumCode(tracking.getCurriculumDisplayCode())
                .proposedCurriculumName(tracking.getProposedCurriculumName())
                .proposedCurriculumCode(tracking.getProposedCurriculumCode())
                .proposedDurationSemesters(tracking.getProposedDurationSemesters())
                .curriculumDescription(tracking.getCurriculumDescription())
                .proposedEffectiveDate(tracking.getProposedEffectiveDate())
                .proposedExpiryDate(tracking.getProposedExpiryDate())
                .schoolId(tracking.getSchool().getId())
                .schoolName(tracking.getSchool().getName())
                .departmentId(tracking.getDepartment().getId())
                .departmentName(tracking.getDepartment().getName())
                .academicLevelId(tracking.getAcademicLevel().getId())
                .academicLevelName(tracking.getAcademicLevel().getName())
                .currentStage(tracking.getCurrentStage())
                .currentStageDisplayName(tracking.getCurrentStage().getDisplayName())
                .status(tracking.getStatus())
                .statusDisplayName(tracking.getStatus().getDisplayName())
                .initiatedByName(getFullName(tracking.getInitiatedBy()))
                .initiatedByEmail(tracking.getInitiatedBy().getEmail())
                .currentAssigneeName(tracking.getCurrentAssignee() != null ?
                        getFullName(tracking.getCurrentAssignee()) : null)
                .currentAssigneeEmail(tracking.getCurrentAssignee() != null ?
                        tracking.getCurrentAssignee().getEmail() : null)
                .initialNotes(tracking.getInitialNotes())
                .createdAt(tracking.getCreatedAt())
                .updatedAt(tracking.getUpdatedAt())
                .expectedCompletionDate(tracking.getExpectedCompletionDate())
                .actualCompletionDate(tracking.getActualCompletionDate())
                .isActive(tracking.getIsActive())
                .isCompleted(tracking.isCompleted())
                .isIdeationStage(tracking.isIdeationStage())
                .build();
    }

    /**
     * Convert CurriculumTracking entity to overview DTO (for list views)
     */
    public CurriculumTrackingOverviewDto toOverviewDto(CurriculumTracking tracking) {
        if (tracking == null) {
            return null;
        }

        return CurriculumTrackingOverviewDto.builder()
                .id(tracking.getId())
                .trackingId(tracking.getTrackingId())
                .curriculumId(tracking.getCurriculum() != null ? tracking.getCurriculum().getId() : null)
                .curriculumName(tracking.getCurriculum() != null ? tracking.getCurriculum().getName() : null)
                .curriculumCode(tracking.getCurriculum() != null ? tracking.getCurriculum().getCode() : null)
                .displayCurriculumName(tracking.getCurriculumDisplayName())
                .displayCurriculumCode(tracking.getCurriculumDisplayCode())
                .proposedCurriculumName(tracking.getProposedCurriculumName())
                .proposedCurriculumCode(tracking.getProposedCurriculumCode())
                .schoolId(tracking.getSchool().getId())
                .schoolName(tracking.getSchool().getName())
                .departmentId(tracking.getDepartment().getId())
                .departmentName(tracking.getDepartment().getName())
                .academicLevelId(tracking.getAcademicLevel().getId())
                .academicLevelName(tracking.getAcademicLevel().getName())
                .currentStage(tracking.getCurrentStage())
                .currentStageDisplayName(tracking.getCurrentStage().getDisplayName())
                .status(tracking.getStatus())
                .statusDisplayName(tracking.getStatus().getDisplayName())
                .initiatedByName(getFullName(tracking.getInitiatedBy()))
                .currentAssigneeName(tracking.getCurrentAssignee() != null ?
                        getFullName(tracking.getCurrentAssignee()) : null)
                .createdAt(tracking.getCreatedAt())
                .expectedCompletionDate(tracking.getExpectedCompletionDate())
                .isActive(tracking.getIsActive())
                .isIdeationStage(tracking.isIdeationStage())
                .build();
    }

    /**
     * Convert InitiateTrackingRequest to CurriculumTracking entity
     */
    public CurriculumTracking toEntity(InitiateTrackingRequest request,
                                       School school,
                                       Department department,
                                       AcademicLevel academicLevel,
                                       User initiatedBy,
                                       User currentAssignee) {
        if (request == null) {
            return null;
        }

        return CurriculumTracking.builder()
                .school(school)
                .department(department)
                .academicLevel(academicLevel)
                .proposedCurriculumName(request.getProposedCurriculumName())
                .proposedCurriculumCode(request.getProposedCurriculumCode())
                .proposedDurationSemesters(request.getProposedDurationSemesters())
                .curriculumDescription(request.getCurriculumDescription())
                .proposedEffectiveDate(request.getProposedEffectiveDate())
                .proposedExpiryDate(request.getProposedExpiryDate())
                .initiatedBy(initiatedBy)
                .currentAssignee(currentAssignee)
                .initialNotes(request.getInitialNotes())
                .expectedCompletionDate(request.getExpectedCompletionDate())
                .build();
    }

    /**
     * Convert Page of CurriculumTracking entities to paginated response
     */
    public CurriculumTrackingPageResponse toPageResponse(Page<CurriculumTracking> trackingPage) {
        if (trackingPage == null) {
            return null;
        }

        List<CurriculumTrackingOverviewDto> trackings = trackingPage.getContent().stream()
                .map(this::toOverviewDto)
                .collect(Collectors.toList());

        return CurriculumTrackingPageResponse.builder()
                .trackings(trackings)
                .currentPage(trackingPage.getNumber())
                .totalPages(trackingPage.getTotalPages())
                .totalElements(trackingPage.getTotalElements())
                .pageSize(trackingPage.getSize())
                .hasNext(trackingPage.hasNext())
                .hasPrevious(trackingPage.hasPrevious())
                .isFirst(trackingPage.isFirst())
                .isLast(trackingPage.isLast())
                .build();
    }

    /**
     * Convert list of CurriculumTracking entities to overview DTOs
     */
    public List<CurriculumTrackingOverviewDto> toOverviewDtoList(List<CurriculumTracking> trackings) {
        if (trackings == null) {
            return null;
        }

        return trackings.stream()
                .map(this::toOverviewDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of CurriculumTracking entities to detail DTOs
     */
    public List<CurriculumTrackingDetailDto> toDetailDtoList(List<CurriculumTracking> trackings) {
        if (trackings == null) {
            return null;
        }

        return trackings.stream()
                .map(this::toDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Update existing CurriculumTracking entity with new data
     */
    public void updateEntityFromRequest(CurriculumTracking tracking,
                                        InitiateTrackingRequest request,
                                        Department department,
                                        AcademicLevel academicLevel) {
        if (tracking == null || request == null) {
            return;
        }

        if (request.getProposedCurriculumName() != null) {
            tracking.setProposedCurriculumName(request.getProposedCurriculumName());
        }
        if (request.getProposedCurriculumCode() != null) {
            tracking.setProposedCurriculumCode(request.getProposedCurriculumCode());
        }
        if (request.getProposedDurationSemesters() != null) {
            tracking.setProposedDurationSemesters(request.getProposedDurationSemesters());
        }
        if (request.getCurriculumDescription() != null) {
            tracking.setCurriculumDescription(request.getCurriculumDescription());
        }
        if (request.getProposedEffectiveDate() != null) {
            tracking.setProposedEffectiveDate(request.getProposedEffectiveDate());
        }
        if (request.getProposedExpiryDate() != null) {
            tracking.setProposedExpiryDate(request.getProposedExpiryDate());
        }
        if (request.getExpectedCompletionDate() != null) {
            tracking.setExpectedCompletionDate(request.getExpectedCompletionDate());
        }
        if (department != null) {
            tracking.setDepartment(department);
        }
        if (academicLevel != null) {
            tracking.setAcademicLevel(academicLevel);
        }
    }

    /**
     * Helper method to get full name from User entity
     */
    private String getFullName(User user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";

        return (firstName + " " + lastName).trim();
    }
}