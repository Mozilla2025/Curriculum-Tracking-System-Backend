package com.mozilla.curriculum_tracking_system.repository.department;

import com.mozilla.curriculum_tracking_system.model.department.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Page<Department> findBySchoolId(Long schoolId, Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.school.id = :schoolId AND LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Department> findBySchoolIdAndNameContainingIgnoreCase(@Param("schoolId") Long schoolId,
                                                               @Param("searchTerm") String searchTerm,
                                                               Pageable pageable);

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Department> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(@Param("searchTerm") String searchTerm,
                                                                              Pageable pageable);

    /**
     * Check if department exists by name and school ID
     */
    boolean existsByNameAndSchoolId(String name, Long schoolId);

    /**
     * Check if department exists by code and school ID
     */
    boolean existsByCodeAndSchoolId(String code, Long schoolId);

    /**
     * Find department by ID with school information
     */
    @Query("SELECT d FROM Department d JOIN FETCH d.school WHERE d.id = :id")
    Optional<Department> findByIdWithSchool(@Param("id") Long id);

    /**
     * Count departments by school ID
     */
    long countBySchoolId(Long schoolId);
}
