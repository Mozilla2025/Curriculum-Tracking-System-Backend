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

        @Query("SELECT d FROM Department d WHERE d.school.id = :schoolId AND " +
                "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Department> findBySchoolIdAndNameContainingIgnoreCase(
                @Param("schoolId") Long schoolId,
                @Param("searchTerm") String searchTerm,
                Pageable pageable);

        @Query("SELECT d FROM Department d WHERE " +
                "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                "LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Department> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                @Param("searchTerm") String searchTerm, Pageable pageable);

        Optional<Department> findByNameAndSchoolId(String name, Long schoolId);

        Optional<Department> findByCodeAndSchoolId(String code, Long schoolId);

        boolean existsByNameAndSchoolId(String name, Long schoolId);
        boolean existsByCodeAndSchoolId(String code, Long schoolId);

        @Query("SELECT d FROM Department d JOIN FETCH d.school WHERE d.id = :id")
        Optional<Department> findByIdWithSchool(@Param("id") Long id);

        long countBySchoolId(Long schoolId);

    }
