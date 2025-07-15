package com.mozilla.curriculum_tracking_system.model.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents individual steps/actions in the tracking workflow
 */
@Entity
@Table(name = "tracking_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"tracking", "performedBy", "assignedTo", "documents"})
public class TrackingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_id", nullable = false)
    private CurriculumTracking tracking;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private TrackingStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private TrackingAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_stage")
    private TrackingStage fromStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_stage")
    private TrackingStage toStage;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Builder.Default
    @Column(name = "is_milestone", nullable = false)
    private Boolean isMilestone = false;

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @OneToMany(mappedBy = "trackingStep", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrackingDocument> documents = new ArrayList<>();

    public Boolean isStageTransition() {
        return fromStage != null && toStage != null && !fromStage.equals(toStage);
    }

    public Boolean isForwardMovement() {
        if (!isStageTransition()) return false;
        return toStage.ordinal() < fromStage.ordinal();
    }
}
