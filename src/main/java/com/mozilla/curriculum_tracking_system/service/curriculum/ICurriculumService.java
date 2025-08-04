package com.mozilla.curriculum_tracking_system.service.curriculum;

import com.mozilla.curriculum_tracking_system.constants.CacheConstants;
import com.mozilla.curriculum_tracking_system.dto.curriculum.*;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

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

    List<Curriculum> getAllActiveCurricula();

    CurriculumPageResponse searchCurriculums(CurriculumSearchCriteria criteria, Pageable pageable);

    CurriculumPageResponse getCurriculumsBySchool(Long schoolId, Pageable pageable);

    CurriculumPageResponse getCurriculumsByDepartment(Long departmentId, Pageable pageable);

    CurriculumPageResponse getCurriculumsByAcademicLevel(Long academicLevelId, Pageable pageable);

    CurriculumStatusStats getCurriculumStats();

    List<CurriculumDto> getCurriculumsExpiringSoon(int days);
}