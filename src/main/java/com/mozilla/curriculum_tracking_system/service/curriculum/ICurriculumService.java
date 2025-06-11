package com.mozilla.curriculum_tracking_system.service.curriculum;


import com.mozilla.curriculum_tracking_system.dto.curriculum.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICurriculumService {

    /**
     * Create a new curriculum
     */
    CurriculumDto createCurriculum(CreateCurriculumRequest request);

    /**
     * Get curriculum by ID
     */
    CurriculumDto getCurriculumById(Long id);

    /**
     * Get all curriculums with pagination
     */
    CurriculumPageResponse getAllCurriculums(Pageable pageable);

    /**
     * Search curriculums with criteria and pagination
     */
    CurriculumPageResponse searchCurriculums(CurriculumSearchCriteria criteria, Pageable pageable);

    /**
     * Update curriculum
     */
    CurriculumDto updateCurriculum(Long id, UpdateCurriculumRequest request);

    /**
     * Delete curriculum (soft delete by setting isActive to false)
     */
    void deleteCurriculum(Long id);

    /**
     * Permanently delete curriculum (hard delete)
     */
    void permanentlyDeleteCurriculum(Long id);


    /**
     * Put curriculum under review
     */
    CurriculumDto putCurriculumUnderReview(Long id);

    /**
     * Activate/Deactivate curriculum
     */
    CurriculumDto toggleCurriculumStatus(Long id);

    /**
     * Get curriculums by school
     */
    CurriculumPageResponse getCurriculumsBySchool(Long schoolId, Pageable pageable);

    /**
     * Get curriculums by department
     */
    CurriculumPageResponse getCurriculumsByDepartment(Long departmentId, Pageable pageable);

    /**
     * Get curriculums by academic level
     */
    CurriculumPageResponse getCurriculumsByAcademicLevel(Long academicLevelId, Pageable pageable);

    /**
     * Get curriculum statistics
     */
    CurriculumStatusStats getCurriculumStats();

    /**
     * Get curriculums expiring soon
     */
    List<CurriculumDto> getCurriculumsExpiringSoon(int days);
}