package com.mozilla.curriculum_tracking_system.model.tracking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "curriculum_tracking_history")
@EqualsAndHashCode(exclude = {"curriculumTracking", "documents"})
@ToString(exclude = {"curriculumTracking", "documents"})
public class CurriculumTrackingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_tracking_id", nullable = false)
    private CurriculumTracking curriculumTracking;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private CurriculumTrackingStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private TrackingActionType actionType;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy; // User ID who performed the action

    @Column(name = "performed_by_email", nullable = false)
    private String performedByEmail;

    @Column(name = "assigned_to")
    private Long assignedTo; // User ID the curriculum is assigned to

    @Column(name = "assigned_to_email")
    private String assignedToEmail;

    @Column(name = "from_stage")
    @Enumerated(EnumType.STRING)
    private CurriculumTrackingStage fromStage;

    @Column(name = "to_stage")
    @Enumerated(EnumType.STRING)
    private CurriculumTrackingStage toStage;

    @Column(columnDefinition = "TEXT")
    private String comments; // Text comments/feedback

    @Column(name = "action_date", nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate; // Expected completion date for this stage

    @Builder.Default
    @Column(name = "is_milestone")
    private boolean isMilestone = false; // Mark important stages

    @JsonIgnore
    @OneToMany(mappedBy = "trackingHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CurriculumTrackingDocument> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.actionDate = LocalDateTime.now();
    }

    /**
     * Add a document to this tracking history entry
     */
    public void addDocument(CurriculumTrackingDocument document) {
        this.documents.add(document);
        document.setTrackingHistory(this);
    }

    /**
     * Remove a document from this tracking history entry
     */
    public void removeDocument(CurriculumTrackingDocument document) {
        this.documents.remove(document);
        document.setTrackingHistory(null);
    }

    /**
     * Check if this history entry represents a stage transition
     */
    public boolean isStageTransition() {
        return this.fromStage != null && this.toStage != null &&
                !this.fromStage.equals(this.toStage);
    }

    /**
     * Check if this represents a forward movement in the process
     */
    public boolean isForwardMovement() {
        if (!isStageTransition()) return false;

        return switch (this.actionType) {
            case SUBMITTED, APPROVED -> true;
            default -> false;
        };
    }

    /**
     * Check if this represents a backward movement in the process
     */
    public boolean isBackwardMovement() {
        return isStageTransition() && this.actionType == TrackingActionType.SENT_BACK;
    }

}
