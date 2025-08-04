package com.mozilla.curriculum_tracking_system.model.tracking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "curriculum_tracking")
@EqualsAndHashCode(exclude = {"curriculum", "trackingHistory"})
@ToString(exclude = {"curriculum", "trackingHistory"})
public class CurriculumTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false, unique = true)
    private Curriculum curriculum;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
    private CurriculumTrackingStage currentStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CurriculumTrackingStatus status = CurriculumTrackingStatus.UNDER_REVIEW;

    @Column(name = "initiated_by", nullable = false)
    private Long initiatedBy; // QA user who initiated the tracking

    @Column(name = "current_assignee")
    private Long currentAssignee; // User currently responsible for the curriculum

    @Column(name = "initiated_at", nullable = false, updatable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "estimated_completion_date")
    private LocalDateTime estimatedCompletionDate;

    @Column(columnDefinition = "TEXT")
    private String notes; // General notes about the tracking process

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @JsonIgnore
    @OneToMany(mappedBy = "curriculumTracking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CurriculumTrackingHistory> trackingHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.initiatedAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.currentStage == null) {
            this.currentStage = CurriculumTrackingStage.SCHOOL_BOARD;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Move curriculum to the next stage
     */
    public void moveToNextStage() {
        this.currentStage = this.currentStage.getNextStage();
        if (this.currentStage == CurriculumTrackingStage.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Send curriculum back to previous stage
     */
    public void sendBackToStage(CurriculumTrackingStage targetStage) {
        if (this.currentStage.canSendBackTo(targetStage)) {
            this.currentStage = targetStage;
        } else {
            throw new IllegalArgumentException(
                    String.format("Cannot send curriculum back from %s to %s",
                            this.currentStage, targetStage)
            );
        }
    }

    /**
     * Mark curriculum as completed with final status
     */
    public void markAsCompleted(CurriculumTrackingStatus finalStatus) {
        this.status = finalStatus;
        this.currentStage = CurriculumTrackingStage.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Check if curriculum is at a specific stage
     */
    public boolean isAtStage(CurriculumTrackingStage stage) {
        return this.currentStage == stage;
    }

    /**
     * Check if curriculum tracking is completed
     */
    public boolean isCompleted() {
        return this.currentStage == CurriculumTrackingStage.COMPLETED ||
                this.completedAt != null;
    }

    /**
     * Add tracking history entry
     */
    public void addTrackingHistory(CurriculumTrackingHistory history) {
        this.trackingHistory.add(history);
        history.setCurriculumTracking(this);
    }
}
