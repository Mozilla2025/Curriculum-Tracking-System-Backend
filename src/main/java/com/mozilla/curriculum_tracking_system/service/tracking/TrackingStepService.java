package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingStepDto;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingStepPageResponse;
import com.mozilla.curriculum_tracking_system.dto.tracking.search.TrackingStepSearchCriteria;
import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import com.mozilla.curriculum_tracking_system.enums.TrackingStage;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.tracking.TrackingDocumentMapper;
import com.mozilla.curriculum_tracking_system.mapper.tracking.TrackingStepMapper;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingStep;
import com.mozilla.curriculum_tracking_system.repository.tracking.TrackingStepRepository;
import com.mozilla.curriculum_tracking_system.util.specifications.TrackingStepSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TrackingStepService implements ITrackingStepService {

    private final TrackingStepRepository trackingStepRepository;
    private final TrackingStepMapper trackingStepMapper;
    private final TrackingDocumentMapper trackingDocumentMapper;

    @Override
    public TrackingStepPageResponse getStepsByTracking(Long trackingId, Pageable pageable) {
        log.debug("Fetching tracking steps for tracking ID: {} with pagination: {}", trackingId, pageable);

        validateTrackingId(trackingId);

        var stepsPage = trackingStepRepository.findByTrackingIdWithDetails(trackingId, pageable);

        log.debug("Found {} tracking steps for tracking ID: {}", stepsPage.getTotalElements(), trackingId);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepDto getStepById(Long stepId) {
        log.debug("Fetching tracking step by ID: {}", stepId);

        validateStepId(stepId);

        var step = findStepByIdWithDetails(stepId);
        var stepDto = trackingStepMapper.toDto(step);

        // Add documents if present
        if (!step.getDocuments().isEmpty()) {
            var documentDtos = trackingDocumentMapper.toDtoList(step.getDocuments());
            stepDto.setDocuments(documentDtos);
        }

        log.debug("Successfully retrieved tracking step: {}", stepId);

        return stepDto;
    }

    @Override
    public TrackingStepPageResponse searchSteps(TrackingStepSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching tracking steps with criteria: {}", criteria);

        var specification = TrackingStepSpecification.withCriteria(criteria);
        var stepsPage = trackingStepRepository.findAll(specification, pageable);

        log.debug("Found {} tracking steps matching search criteria", stepsPage.getTotalElements());

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepPageResponse getStepsByStage(Long trackingId, TrackingStage stage, Pageable pageable) {
        log.debug("Fetching tracking steps for tracking ID: {} and stage: {}", trackingId, stage);

        validateTrackingId(trackingId);
        validateStage(stage);

        var stepsPage = trackingStepRepository.findByTrackingIdAndStage(trackingId, stage, pageable);

        log.debug("Found {} steps for tracking ID: {} at stage: {}", stepsPage.getTotalElements(), trackingId, stage);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepPageResponse getStepsByAction(Long trackingId, TrackingAction action, Pageable pageable) {
        log.debug("Fetching tracking steps for tracking ID: {} and action: {}", trackingId, action);

        validateTrackingId(trackingId);
        validateAction(action);

        var stepsPage = trackingStepRepository.findByTrackingIdAndAction(trackingId, action, pageable);

        log.debug("Found {} steps for tracking ID: {} with action: {}", stepsPage.getTotalElements(), trackingId, action);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepPageResponse getStepsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching tracking steps performed by user ID: {}", userId);

        validateUserId(userId);

        var stepsPage = trackingStepRepository.findByPerformedByIdOrderByPerformedAtDesc(userId, pageable);

        log.debug("Found {} steps performed by user ID: {}", stepsPage.getTotalElements(), userId);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepPageResponse getMilestoneSteps(Long trackingId, Pageable pageable) {
        log.debug("Fetching milestone steps for tracking ID: {}", trackingId);

        validateTrackingId(trackingId);

        var stepsPage = trackingStepRepository.findMilestoneStepsByTrackingId(trackingId, pageable);

        log.debug("Found {} milestone steps for tracking ID: {}", stepsPage.getTotalElements(), trackingId);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepPageResponse getStageTransitionSteps(Long trackingId, Pageable pageable) {
        log.debug("Fetching stage transition steps for tracking ID: {}", trackingId);

        validateTrackingId(trackingId);

        var stepsPage = trackingStepRepository.findStageTransitionStepsByTrackingId(trackingId, pageable);

        log.debug("Found {} stage transition steps for tracking ID: {}", stepsPage.getTotalElements(), trackingId);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public List<TrackingStepDto> getRecentSteps(Long trackingId, int limit) {
        log.debug("Fetching {} recent steps for tracking ID: {}", limit, trackingId);

        validateTrackingId(trackingId);
        validateLimit(limit);

        var recentSteps = trackingStepRepository.findRecentStepsByTrackingId(trackingId, limit);
        var stepDtos = trackingStepMapper.toDtoList(recentSteps);

        log.debug("Found {} recent steps for tracking ID: {}", stepDtos.size(), trackingId);

        return stepDtos;
    }

    @Override
    public TrackingStepPageResponse getStepsByDateRange(Long trackingId,
                                                        LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        Pageable pageable) {
        log.debug("Fetching tracking steps for tracking ID: {} between {} and {}", trackingId, startDate, endDate);

        validateTrackingId(trackingId);
        validateDateRange(startDate, endDate);

        var stepsPage = trackingStepRepository.findByTrackingIdAndPerformedAtBetween(
                trackingId, startDate, endDate, pageable);

        log.debug("Found {} steps for tracking ID: {} in date range", stepsPage.getTotalElements(), trackingId);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    @Override
    public TrackingStepDto getLatestStep(Long trackingId) {
        log.debug("Fetching latest step for tracking ID: {}", trackingId);

        validateTrackingId(trackingId);

        var latestStep = trackingStepRepository.findLatestStepByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("No steps found for tracking ID: " + trackingId));

        var stepDto = trackingStepMapper.toDto(latestStep);

        // Add documents if present
        if (!latestStep.getDocuments().isEmpty()) {
            var documentDtos = trackingDocumentMapper.toDtoList(latestStep.getDocuments());
            stepDto.setDocuments(documentDtos);
        }

        log.debug("Successfully retrieved latest step for tracking ID: {}", trackingId);

        return stepDto;
    }

    @Override
    public TrackingStepPageResponse getStepsAssignedToUser(Long userId, Pageable pageable) {
        log.debug("Fetching tracking steps assigned to user ID: {}", userId);

        validateUserId(userId);

        var stepsPage = trackingStepRepository.findByAssignedToIdOrderByPerformedAtDesc(userId, pageable);

        log.debug("Found {} steps assigned to user ID: {}", stepsPage.getTotalElements(), userId);

        return trackingStepMapper.toPageResponse(stepsPage);
    }

    private TrackingStep findStepByIdWithDetails(Long stepId) {
        return trackingStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking step not found with ID: " + stepId));
    }

    private void validateTrackingId(Long trackingId) {
        if (trackingId == null || trackingId <= 0) {
            throw new IllegalArgumentException("Invalid tracking ID: " + trackingId);
        }
    }

    private void validateStepId(Long stepId) {
        if (stepId == null || stepId <= 0) {
            throw new IllegalArgumentException("Invalid step ID: " + stepId);
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }
    }

    private void validateStage(TrackingStage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Tracking stage cannot be null");
        }
    }

    private void validateAction(TrackingAction action) {
        if (action == null) {
            throw new IllegalArgumentException("Tracking action cannot be null");
        }
    }

    private void validateLimit(int limit) {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100, got: " + limit);
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}
