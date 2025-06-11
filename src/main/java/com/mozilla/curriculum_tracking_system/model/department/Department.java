package com.mozilla.curriculum_tracking_system.model.department;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.school.School;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "departments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "school_id"}),
    @UniqueConstraint(columnNames = {"code", "school_id"})
})
public class Department {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String code;

    @Column(name = "head_id")
    private Long headId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @JsonIgnore
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Curriculum> curriculums = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addCurriculum(Curriculum curriculum) {
        this.curriculums.add(curriculum);
        curriculum.setDepartment(this);
    }

    public void removeCurriculum(Curriculum curriculum) {
        this.curriculums.remove(curriculum);
        curriculum.setDepartment(null);
    }
}
