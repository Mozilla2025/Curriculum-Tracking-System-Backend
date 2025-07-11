package com.mozilla.curriculum_tracking_system.mapper;

import com.mozilla.curriculum_tracking_system.dto.tracking.*;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingDocument;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
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
                .totalHistoryEntries(tracking.getTrackingHistory() != null ? tracking.getTrackingHistory().size() : 0)
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
                .totalHistoryEntries(tracking.getTrackingHistory() != null ? tracking.getTrackingHistory().size() : 0)
                .build();
    }

    /**
     * Convert CurriculumTracking entity to DTO with recent history
     */
    public CurriculumTrackingDto toDtoWithRecentHistory(CurriculumTracking tracking,
                                                        List<CurriculumTrackingHistory> recentHistory) {
        CurriculumTrackingDto dto = toDto(tracking);
        if (dto != null && recentHistory != null) {
            dto.setRecentHistory(recentHistory.stream()
                    .map(this::toHistoryDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * Convert CurriculumTrackingHistory entity to DTO
     */
    public CurriculumTrackingHistoryDto toHistoryDto(CurriculumTrackingHistory history) {
        if (history == null) {
            return null;
        }

        return CurriculumTrackingHistoryDto.builder()
                .id(history.getId())
                .curriculumTrackingId(history.getCurriculumTracking() != null
                        ? history.getCurriculumTracking().getId() : null)
                .stage(history.getStage())
                .stageDisplayName(history.getStage() != null ? history.getStage().getDisplayName() : null)
                .actionType(history.getActionType())
                .performedBy(history.getPerformedBy())
                .performedByEmail(history.getPerformedByEmail())
                .assignedTo(history.getAssignedTo())
                .assignedToEmail(history.getAssignedToEmail())
                .fromStage(history.getFromStage())
                .fromStageDisplayName(history.getFromStage() != null ? history.getFromStage().getDisplayName() : null)
                .toStage(history.getToStage())
                .toStageDisplayName(history.getToStage() != null ? history.getToStage().getDisplayName() : null)
                .comments(history.getComments())
                .actionDate(history.getActionDate())
                .dueDate(history.getDueDate())
                .isMilestone(history.isMilestone())
                .isStageTransition(history.isStageTransition())
                .isForwardMovement(history.isForwardMovement())
                .isBackwardMovement(history.isBackwardMovement())
                .documents(history.getDocuments() != null
                        ? history.getDocuments().stream()
                        .map(this::toDocumentDto)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    /**
     * Convert CurriculumTrackingDocument entity to DTO
     */
    public CurriculumTrackingDocumentDto toDocumentDto(CurriculumTrackingDocument document) {
        if (document == null) {
            return null;
        }

        return CurriculumTrackingDocumentDto.builder()
                .id(document.getId())
                .trackingHistoryId(document.getTrackingHistory() != null
                        ? document.getTrackingHistory().getId() : null)
                .documentName(document.getDocumentName())
                .originalFilename(document.getOriginalFilename())
                .firebaseUrl(document.getFirebaseUrl())
                .firebasePath(document.getFirebasePath())
                .fileSize(document.getFileSize())
                .formattedFileSize(document.getFormattedFileSize())
                .contentType(document.getContentType())
                .fileExtension(document.getFileExtension())
                .description(document.getDescription())
                .uploadedBy(document.getUploadedBy())
                .uploadedByEmail(document.getUploadedByEmail())
                .uploadedAt(document.getUploadedAt())
                .isActive(document.isActive())
                .documentVersion(document.getDocumentVersion())
                .isPdf(document.isPdf())
                .isWordDocument(document.isWordDocument())
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
     * Convert list of CurriculumTrackingHistory entities to DTOs
     */
    public List<CurriculumTrackingHistoryDto> toHistoryDtoList(List<CurriculumTrackingHistory> histories) {
        if (histories == null) {
            return null;
        }
        return histories.stream()
                .map(this::toHistoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of CurriculumTrackingDocument entities to DTOs
     */
    public List<CurriculumTrackingDocumentDto> toDocumentDtoList(List<CurriculumTrackingDocument> documents) {
        if (documents == null) {
            return null;
        }
        return documents.stream()
                .map(this::toDocumentDto)
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
     * Build timeline DTO from tracking and history
     */
    public CurriculumTrackingTimelineDto buildTimelineDto(CurriculumTracking tracking,
                                                          List<CurriculumTrackingHistory> history) {
        if (tracking == null) {
            return null;
        }

        List<CurriculumTrackingTimelineDto.TimelineEntry> timeline = history != null
                ? history.stream()
                .map(h -> CurriculumTrackingTimelineDto.TimelineEntry.builder()
                        .date(h.getActionDate())
                        .stage(h.getStage())
                        .stageDisplayName(h.getStage() != null ? h.getStage().getDisplayName() : null)
                        .actionType(h.getActionType())
                        .performedByEmail(h.getPerformedByEmail())
                        .comments(h.getComments())
                        .isMilestone(h.isMilestone())
                        .isCurrentStage(h.getStage() == tracking.getCurrentStage())
                        .documentsCount(h.getDocuments() != null ? h.getDocuments().size() : 0)
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return CurriculumTrackingTimelineDto.builder()
                .trackingId(tracking.getId())
                .curriculumName(tracking.getCurriculum() != null ? tracking.getCurriculum().getName() : null)
                .timeline(timeline)
                .build();
    }

    /**
     * Create document upload response
     */
    public DocumentUploadResponse buildDocumentUploadResponse(CurriculumTrackingDocument document, String message) {
        if (document == null) {
            return DocumentUploadResponse.builder()
                    .message(message != null ? message : "Upload failed")
                    .build();
        }

        return DocumentUploadResponse.builder()
                .documentId(document.getId())
                .firebaseUrl(document.getFirebaseUrl())
                .firebasePath(document.getFirebasePath())
                .originalFilename(document.getOriginalFilename())
                .formattedFileSize(document.getFormattedFileSize())
                .message(message != null ? message : "Upload successful")
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
    }}