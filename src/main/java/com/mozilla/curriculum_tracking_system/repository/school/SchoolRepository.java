package com.mozilla.curriculum_tracking_system.repository.school;

import com.mozilla.curriculum_tracking_system.model.school.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
}
