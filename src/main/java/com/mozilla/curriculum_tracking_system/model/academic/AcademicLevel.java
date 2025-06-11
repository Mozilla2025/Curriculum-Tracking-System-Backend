package com.mozilla.curriculum_tracking_system.model.academic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "academic_levels")
public class AcademicLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "academicLevel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Curriculum> curriculums = new HashSet<>();

}
