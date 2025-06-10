package com.mozilla.curriculum_tracking_system.model.school;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "schools")
@EqualsAndHashCode(exclude = {"departments", "curriculums"})
@ToString(exclude = {"departments", "curriculums"})
@NamedEntityGraphs({
    @NamedEntityGraph(name = "School.withDepartments", attributeNodes = @NamedAttributeNode("departments")),
    @NamedEntityGraph(name = "School.withCurriculums", attributeNodes = @NamedAttributeNode("curriculums")),
    @NamedEntityGraph(name = "School.detailed", attributeNodes = {
        @NamedAttributeNode("departments"),
        @NamedAttributeNode("curriculums")
    })
})
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 100)
    private String email;

    @Column(name = "dean_id")
    private Long deanId;

    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Department> departments = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    public void addDepartment(Department department) {
        this.departments.add(department);
        department.setSchool(this);
    }

    public void removeDepartment(Department department) {
        this.departments.remove(department);
        department.setSchool(null);
    }

}
