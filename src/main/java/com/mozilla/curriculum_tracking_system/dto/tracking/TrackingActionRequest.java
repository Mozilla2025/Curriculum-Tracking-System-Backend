package com.mozilla.curriculum_tracking_system.dto.tracking;


import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for performing tracking actions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingActionRequest {
    @NotNull(message = "Tracking ID is required")
    private Long trackingId;

    @NotNull(message = "Action is required")
    private TrackingAction action;

    private TrackingStage returnToStage;
    private Long assignToUserId;
    private String notes;
    private LocalDateTime dueDate;
    private Boolean isMilestone = false;
    private List<MultipartFile> documents;
}
