package com.mozilla.curriculum_tracking_system.mapper.curriculum;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingHistoryDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingTimelineDto;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CurriculumTrackingHistoryMapper {

    @Autowired
    private CurriculumTrackingDocumentMapper documentMapper;

    /**
     * Convert CurriculumTrackingHistory entity to DTO
     */
    public CurriculumTrackingHistoryDto toDto(CurriculumTrackingHistory history) {
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
                        ? documentMapper.toDtoList(history.getDocuments())
                        : null)
                .build();
    }

    /**
     * Convert list of CurriculumTrackingHistory entities to DTOs
     */
    public List<CurriculumTrackingHistoryDto> toDtoList(List<CurriculumTrackingHistory> histories) {
        if (histories == null) {
            return null;
        }
        return histories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
}
