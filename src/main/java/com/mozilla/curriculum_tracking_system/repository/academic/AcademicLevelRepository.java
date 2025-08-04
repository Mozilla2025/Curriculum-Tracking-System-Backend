package com.mozilla.curriculum_tracking_system.repository.academic;

import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, Long> {
    Optional<AcademicLevel> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
