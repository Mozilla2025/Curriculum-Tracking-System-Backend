package com.mozilla.curriculum_tracking_system.service.curriculum;

import com.mozilla.curriculum_tracking_system.dto.curriculum.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICurriculumService {

    CurriculumDto createCurriculum(CreateCurriculumRequest request, String authToken);

    CurriculumDto updateCurriculum(Long id, UpdateCurriculumRequest request, String authToken);

    void deleteCurriculum(Long id, String authToken);

    void permanentlyDeleteCurriculum(Long id, String authToken);

    CurriculumDto putCurriculumUnderReview(Long id, String authToken);

    CurriculumDto toggleCurriculumStatus(Long id, String authToken);

    CurriculumDto getCurriculumById(Long id);

    CurriculumPageResponse getAllCurriculums(Pageable pageable);

    CurriculumPageResponse searchCurriculums(CurriculumSearchCriteria criteria, Pageable pageable);

    CurriculumPageResponse getCurriculumsBySchool(Long schoolId, Pageable pageable);

    CurriculumPageResponse getCurriculumsByDepartment(Long departmentId, Pageable pageable);

    CurriculumPageResponse getCurriculumsByAcademicLevel(Long academicLevelId, Pageable pageable);

    CurriculumStatusStats getCurriculumStats();

    List<CurriculumDto> getCurriculumsExpiringSoon(int days);
}