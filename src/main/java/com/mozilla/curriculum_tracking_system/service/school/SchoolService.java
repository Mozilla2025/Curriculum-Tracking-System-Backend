package com.mozilla.curriculum_tracking_system.service.school;


import com.mozilla.curriculum_tracking_system.dto.school.SchoolDto;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.SchoolMapper;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.user.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolMapper schoolMapper;
    private final UserManagementService userService;

    @Override
    public List<SchoolDto> getAllSchools() {
        log.debug("Fetching all schools from database");
        List<School> schools = schoolRepository.findAll();
        List<SchoolDto> schoolDtos = schools.stream().map(schoolMapper::mapToDto).toList();
        log.debug("Retrieved {} schools from database", schools.size());
        return schoolDtos;
    }

    @Override
    public SchoolDto getSchoolById(Long id) {
        log.debug("fetching school with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("School ID cannot be null");
        }

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("School not found with ID: {}", id);
                    return new ResourceNotFoundException("School not found with ID: " + id);
                });
        return schoolMapper.mapToDto(school);
    }


    public UserResponse getSchoolDean(Long schoolId){

        log.debug("fetching the Dean of School ID: {}", schoolId);

        Long deanID = schoolRepository.findDeanIdBySchoolId(schoolId);

        UserResponse schoolDean = userService.getUserById(deanID);

        return schoolDean;
    }
}


