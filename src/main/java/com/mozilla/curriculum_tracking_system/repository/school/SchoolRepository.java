package com.mozilla.curriculum_tracking_system.repository.school;

import com.mozilla.curriculum_tracking_system.model.school.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    boolean existsByName(String name);

    boolean existsByCode(String code);
}
