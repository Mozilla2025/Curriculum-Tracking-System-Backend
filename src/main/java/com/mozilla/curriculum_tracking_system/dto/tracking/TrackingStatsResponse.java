package com.mozilla.curriculum_tracking_system.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tracking statistics and metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingStatsResponse {
    private long totalTrackings;
    private long activeTrackings;
    private long completedTrackings;
    private long overdueTrackings;

    // Status breakdown
    private long initiatedCount;
    private long inProgressCount;
    private long approvedCount;
    private long rejectedCount;
    private long returnedForRevisionCount;

    // Stage breakdown
    private long atIdeationCount;
    private long atReviewApprovalCount;
    private long atSchoolBoardCount;
    private long atDeanCommitteeCount;
    private long atSenateCount;
    private long atQaInternalAuditCount;
    private long atCueExternalAuditCount;
    private long atViceChancellorApprovalCount;

    // Performance metrics
    private Double averageCompletionDays;
    private long totalDocumentsUploaded;
    private long totalStorageUsedBytes;
    private String formattedStorageUsed;
}

