package com.mozilla.curriculum_tracking_system.model.tracking;

import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.enums.TrackingStatus;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.school.School;
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
@ToString(exclude = {"curriculum", "initiatedBy", "currentAssignee", "trackingSteps", "school", "department", "academicLevel"})
public class CurriculumTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false, length = 50)
    private String trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_level_id", nullable = false)
    private AcademicLevel academicLevel;

    @Column(name = "proposed_curriculum_name", nullable = false)
    private String proposedCurriculumName;

    @Column(name = "proposed_curriculum_code")
    private String proposedCurriculumCode;

    @Column(name = "proposed_duration_semesters")
    private Integer proposedDurationSemesters;

    @Column(name = "curriculum_description", columnDefinition = "TEXT")
    private String curriculumDescription;

    @Column(name = "proposed_effective_date")
    private LocalDateTime proposedEffectiveDate;

    @Column(name = "proposed_expiry_date")
    private LocalDateTime proposedExpiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
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
    @Column(name = "is_active", nullable = false)
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
    private void setDefaults() {
        if (this.currentStage == null) {
            this.currentStage = TrackingStage.IDEATION;
        }
        if (this.status == null) {
            this.status = TrackingStatus.INITIATED;
        }
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

    public boolean isIdeationStage() {
        return this.curriculum == null && this.currentStage == TrackingStage.IDEATION;
    }

    public void linkCurriculum(Curriculum curriculum) {
        this.curriculum = curriculum;
    }

    public String getCurriculumDisplayName() {
        return curriculum != null ? curriculum.getName() : proposedCurriculumName;
    }

    public String getCurriculumDisplayCode() {
        return curriculum != null ? curriculum.getCode() : proposedCurriculumCode;
    }

    public void setGeneratedTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
}