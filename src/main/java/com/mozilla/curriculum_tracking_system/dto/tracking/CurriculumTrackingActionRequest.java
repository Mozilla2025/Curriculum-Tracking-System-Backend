package com.mozilla.curriculum_tracking_system.dto.tracking;

import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class CurriculumTrackingActionRequest {
        @NotNull(message = "Tracking ID is required")
        private Long trackingId;

        @NotNull(message = "Action type is required")
        private TrackingActionType actionType;

        private CurriculumTrackingStage targetStage; // For sending back to specific stage

        private Long assignToUserId; // For assigning to specific user

        private String assignToEmail;

        private String comments;

        private LocalDateTime dueDate;

        private List<MultipartFile> documents;

        private boolean isMilestone = false;
    }
