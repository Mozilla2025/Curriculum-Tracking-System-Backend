package com.mozilla.curriculum_tracking_system.mapper.curriculum;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingStageInfo;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingStatsDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CurriculumTrackingMapper {

    /**
     * Convert CurriculumTracking entity to DTO
     */
    public CurriculumTrackingDto toDto(CurriculumTracking tracking) {
        if (tracking == null) {
            return null;
        }

        return CurriculumTrackingDto.builder()
                .id(tracking.getId())
                .curriculumId(tracking.getCurriculum() != null ? tracking.getCurriculum().getId() : null)
                .curriculumName(tracking.getCurriculum() != null ? tracking.getCurriculum().getName() : null)
                .curriculumCode(tracking.getCurriculum() != null ? tracking.getCurriculum().getCode() : null)
                .schoolName(tracking.getCurriculum() != null && tracking.getCurriculum().getSchool() != null
                        ? tracking.getCurriculum().getSchool().getName() : null)
                .departmentName(tracking.getCurriculum() != null && tracking.getCurriculum().getDepartment() != null
                        ? tracking.getCurriculum().getDepartment().getName() : null)
                .currentStage(tracking.getCurrentStage())
                .currentStageDisplayName(tracking.getCurrentStage() != null
                        ? tracking.getCurrentStage().getDisplayName() : null)
                .status(tracking.getStatus())
                .initiatedBy(tracking.getInitiatedBy())
                .currentAssignee(tracking.getCurrentAssignee())
                .initiatedAt(tracking.getInitiatedAt())
                .lastUpdatedAt(tracking.getLastUpdatedAt())
                .completedAt(tracking.getCompletedAt())
                .estimatedCompletionDate(tracking.getEstimatedCompletionDate())
                .notes(tracking.getNotes())
                .isActive(tracking.isActive())
                .isCompleted(tracking.isCompleted())
                .build();
    }

    /**
     * Convert CurriculumTracking entity to DTO with user emails populated
     */
    public CurriculumTrackingDto toDtoWithUserEmails(CurriculumTracking tracking, String initiatedByEmail, String currentAssigneeEmail) {
        if (tracking == null) {
            return null;
        }

        return CurriculumTrackingDto.builder()
                .id(tracking.getId())
                .curriculumId(tracking.getCurriculum() != null ? tracking.getCurriculum().getId() : null)
                .curriculumName(tracking.getCurriculum() != null ? tracking.getCurriculum().getName() : null)
                .curriculumCode(tracking.getCurriculum() != null ? tracking.getCurriculum().getCode() : null)
                .schoolName(tracking.getCurriculum() != null && tracking.getCurriculum().getSchool() != null
                        ? tracking.getCurriculum().getSchool().getName() : null)
                .departmentName(tracking.getCurriculum() != null && tracking.getCurriculum().getDepartment() != null
                        ? tracking.getCurriculum().getDepartment().getName() : null)
                .currentStage(tracking.getCurrentStage())
                .currentStageDisplayName(tracking.getCurrentStage() != null
                        ? tracking.getCurrentStage().getDisplayName() : null)
                .status(tracking.getStatus())
                .initiatedBy(tracking.getInitiatedBy())
                .initiatedByEmail(initiatedByEmail)
                .currentAssignee(tracking.getCurrentAssignee())
                .currentAssigneeEmail(currentAssigneeEmail)
                .initiatedAt(tracking.getInitiatedAt())
                .lastUpdatedAt(tracking.getLastUpdatedAt())
                .completedAt(tracking.getCompletedAt())
                .estimatedCompletionDate(tracking.getEstimatedCompletionDate())
                .notes(tracking.getNotes())
                .isActive(tracking.isActive())
                .isCompleted(tracking.isCompleted())
                .build();
    }

    /**
     * Convert list of CurriculumTracking entities to DTOs
     */
    public List<CurriculumTrackingDto> toDtoList(List<CurriculumTracking> trackings) {
        if (trackings == null) {
            return null;
        }
        return trackings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Build page response for curriculum trackings
     */
    public CurriculumTrackingPageResponse buildPageResponse(Page<CurriculumTracking> trackingPage) {
        List<CurriculumTrackingDto> trackingDtos = trackingPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return CurriculumTrackingPageResponse.builder()
                .trackings(trackingDtos)
                .currentPage(trackingPage.getNumber())
                .totalPages(trackingPage.getTotalPages())
                .totalElements(trackingPage.getTotalElements())
                .pageSize(trackingPage.getSize())
                .hasNext(trackingPage.hasNext())
                .hasPrevious(trackingPage.hasPrevious())
                .build();
    }

    /**
     * Build page response for curriculum trackings with user emails populated
     */
    public CurriculumTrackingPageResponse buildPageResponseWithUserEmails(Page<CurriculumTracking> trackingPage, UserRepository userRepository) {
        List<CurriculumTrackingDto> trackingDtos = trackingPage.getContent().stream()
                .map(tracking -> {
                    // Get user emails for each tracking
                    User initiatorUser = userRepository.findById(tracking.getInitiatedBy()).orElse(null);
                    User currentAssigneeUser = tracking.getCurrentAssignee() != null ?
                            userRepository.findById(tracking.getCurrentAssignee()).orElse(null) : null;

                    return toDtoWithUserEmails(
                            tracking,
                            initiatorUser != null ? initiatorUser.getEmail() : null,
                            currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
                    );
                })
                .collect(Collectors.toList());

        return CurriculumTrackingPageResponse.builder()
                .trackings(trackingDtos)
                .currentPage(trackingPage.getNumber())
                .totalPages(trackingPage.getTotalPages())
                .totalElements(trackingPage.getTotalElements())
                .pageSize(trackingPage.getSize())
                .hasNext(trackingPage.hasNext())
                .hasPrevious(trackingPage.hasPrevious())
                .build();
    }

    /**
     * Convert stage enum to stage info DTO
     */
    public CurriculumTrackingStageInfo toStageInfoDto(CurriculumTrackingStage stage) {
        if (stage == null) {
            return null;
        }

        return CurriculumTrackingStageInfo.builder()
                .stage(stage)
                .displayName(stage.getDisplayName())
                .requiredRole(stage.getRequiredRole())
                .nextStage(stage.getNextStage())
                .previousStage(stage.getPreviousStage())
                .canMoveForward(stage != CurriculumTrackingStage.COMPLETED)
                .canSendBack(stage != CurriculumTrackingStage.SCHOOL_BOARD)
                .possibleBackStages(getPossibleBackStages(stage))
                .build();
    }

    /**
     * Build statistics DTO from counts
     */
    public CurriculumTrackingStatsDto buildStatsDto(
            long totalTracked, long underReview, long accredited, long approvedByCue,
            long minorRevamp, long majorRevamp, long atSchoolBoard, long atDeanCommittee,
            long atSenate, long atQaInternalReview, long atViceChancellorReview,
            long atCueExternalReview, long completed, double avgCompletionTime, long overdue) {

        return CurriculumTrackingStatsDto.builder()
                .totalTracked(totalTracked)
                .underReview(underReview)
                .accredited(accredited)
                .approvedByCue(approvedByCue)
                .minorRevamp(minorRevamp)
                .majorRevamp(majorRevamp)
                .atSchoolBoard(atSchoolBoard)
                .atDeanCommittee(atDeanCommittee)
                .atSenate(atSenate)
                .atQaInternalReview(atQaInternalReview)
                .atViceChancellorReview(atViceChancellorReview)
                .atCueExternalReview(atCueExternalReview)
                .completed(completed)
                .averageCompletionTimeInDays(avgCompletionTime)
                .overdueTasks(overdue)
                .build();
    }

    private List<CurriculumTrackingStage> getPossibleBackStages(CurriculumTrackingStage stage) {
        return switch (stage) {
            case DEAN_COMMITTEE -> List.of(CurriculumTrackingStage.SCHOOL_BOARD);
            case SENATE, QA_INTERNAL_REVIEW -> List.of(CurriculumTrackingStage.DEAN_COMMITTEE);
            case VICE_CHANCELLOR_REVIEW, CUE_EXTERNAL_REVIEW ->
                    List.of(CurriculumTrackingStage.DEAN_COMMITTEE, CurriculumTrackingStage.QA_INTERNAL_REVIEW);
            default -> List.of();
        };
    }
}