package com.mozilla.curriculum_tracking_system.repository.curriculum;

import com.mozilla.curriculum_tracking_system.enums.CurriculumStatus;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, Long>, JpaSpecificationExecutor<Curriculum> {

    Optional<Curriculum> findByNameAndDepartmentIdAndAcademicLevelId(String name, Long departmentId, Long academicLevelId);

    Optional<Curriculum> findByCode(String code);


    Page<Curriculum> findBySchoolId(Long schoolId, Pageable pageable);

    Page<Curriculum> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Curriculum> findByAcademicLevelId(Long academicLevelId, Pageable pageable);

    @Query("SELECT c FROM Curriculum c " +
            "JOIN FETCH c.school s " +
            "JOIN FETCH c.department d " +
            "JOIN FETCH c.academicLevel al " +
            "WHERE c.id = :id")
    Optional<Curriculum> findByIdWithAssociations(@Param("id") Long id);

    @Query("SELECT c FROM Curriculum c WHERE c.expiryDate BETWEEN :startDate AND :endDate AND c.isActive = true")
    List<Curriculum> findCurriculumsExpiringSoon(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    long countByStatus(CurriculumStatus status);


    boolean existsByNameAndDepartmentIdAndAcademicLevelIdAndIdNot(String name, Long departmentId,
                                                                  Long academicLevelId, Long id);

    boolean existsByCodeAndIdNot(String code, Long id);
}