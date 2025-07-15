package com.mozilla.curriculum_tracking_system.model.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "curriculum_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"curriculum", "initiatedBy", "currentAssignee", "trackingSteps"})
public class CurriculumTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false, length = 50)
    private String trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @Enumerated(EnumType.STRING)
    @Column(name = "curriculum_stage", nullable = false)
    private TrackingStage currentStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TrackingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_assignee", nullable = false)
    private User currentAssignee;

    @Column(name = "initial_notes", columnDefinition = "TEXT")
    private String initialNotes;

    @Column(name = "expected_completion_date")
    private LocalDateTime expectedCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tracking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrackingStep> trackingSteps = new ArrayList<>();

    @PrePersist
    private void generateTrackingId() {
        if (this.trackingId == null && this.curriculum != null) {
            this.trackingId = generateUniqueTrackingId();
        }
        if (this.currentStage == null) {
            this.currentStage = TrackingStage.IDEATION;
        }
        if (this.status == null) {
            this.status = TrackingStatus.INITIATED;
        }
    }

    private String generateUniqueTrackingId() {
        String curriculumCode = curriculum.getCode() != null ?
                curriculum.getCode().replaceAll("[^A-Z0-9]", "") : "CUR";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "TRK-" + curriculumCode + "-" + timestamp;
    }

    public void moveToNextStage() {
        this.currentStage = this.currentStage.getNextStage();
        if (this.currentStage == TrackingStage.ACCREDITED) {
            this.status = TrackingStatus.COMPLETED;
            this.actualCompletionDate = LocalDateTime.now();
        }
    }

    public void returnToStage(TrackingStage targetStage) {
        this.currentStage = targetStage;
        this.status = TrackingStatus.RETURNED_FOR_REVISION;
    }

    public boolean isCompleted() {
        return this.currentStage == TrackingStage.ACCREDITED ||
                this.actualCompletionDate != null;
    }
}
