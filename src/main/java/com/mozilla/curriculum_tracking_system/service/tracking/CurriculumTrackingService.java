package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.*;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStatus;
import com.mozilla.curriculum_tracking_system.enums.TrackingActionType;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.exception.UnauthorizedException;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumTrackingMapper;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTracking;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.curriculum.CurriculumRepository;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;
import com.mozilla.curriculum_tracking_system.util.CurriculumTrackingSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CurriculumTrackingService implements ICurriculumTrackingService {

    private final CurriculumTrackingRepository curriculumTrackingRepository;
    private final CurriculumRepository curriculumRepository;
    private final UserRepository userRepository;
    private final CurriculumTrackingMapper trackingMapper;
    private final IAuthenticationService authenticationService;
    private final ICurriculumTrackingHistoryService historyService;
    private final ICurriculumTrackingDocumentService documentService;

    @Override
    public CurriculumTrackingDto initiateCurriculumTracking(InitiateCurriculumTrackingRequest request, String authToken) {
        log.info("Initiating curriculum tracking for curriculum ID: {}", request.getCurriculumId());

        validateQAAccess(authToken);
        validateInitiateRequest(request);

        Curriculum curriculum = findCurriculumById(request.getCurriculumId());

        if (curriculumTrackingRepository.existsByCurriculumId(request.getCurriculumId())) {
            throw new BadRequestException("Curriculum tracking already exists for curriculum ID: " + request.getCurriculumId());
        }

        Long userId = authenticationService.getUserIdFromToken(authToken);
        User initiatorUser = findUserById(userId);

        CurriculumTracking tracking = CurriculumTracking.builder()
                .curriculum(curriculum)
                .currentStage(CurriculumTrackingStage.SCHOOL_BOARD)
                .status(CurriculumTrackingStatus.UNDER_REVIEW)
                .initiatedBy(userId)
                .currentAssignee(null)
                .estimatedCompletionDate(request.getEstimatedCompletionDate())
                .notes(request.getNotes())
                .build();

        CurriculumTracking savedTracking = curriculumTrackingRepository.save(tracking);

        CurriculumTrackingHistory initialHistory = CurriculumTrackingHistory.builder()
                .curriculumTracking(savedTracking)
                .stage(CurriculumTrackingStage.SCHOOL_BOARD)
                .actionType(TrackingActionType.SUBMITTED)
                .performedBy(userId)
                .performedByEmail(initiatorUser.getEmail())
                .toStage(CurriculumTrackingStage.SCHOOL_BOARD)
                .comments(request.getInitialComments())
                .isMilestone(true)
                .build();

        CurriculumTrackingHistoryDto savedHistoryDto = historyService.addHistoryEntry(initialHistory);

        if (request.getInitialDocuments() != null && !request.getInitialDocuments().isEmpty()) {
            try {
                List<DocumentUploadResponse> uploadResponses = documentService.uploadMultipleDocuments(
                        savedHistoryDto.getId(),
                        request.getInitialDocuments(),
                        "Initial submission documents",
                        authToken
                );
                log.info("Uploaded {} initial documents for tracking {}", uploadResponses.size(), savedTracking.getId());
            } catch (Exception e) {
                log.warn("Failed to upload initial documents for tracking {}: {}", savedTracking.getId(), e.getMessage());
            }
        }

        log.info("Successfully initiated curriculum tracking with ID: {}", savedTracking.getId());

        return trackingMapper.toDtoWithUserEmails(savedTracking, initiatorUser.getEmail(), null);
    }

    @Override
    public CurriculumTrackingDto performTrackingAction(CurriculumTrackingActionRequest request, String authToken) {
        log.info("Performing tracking action {} for tracking ID: {}", request.getActionType(), request.getTrackingId());

        validateTrackingAction(request, authToken);

        CurriculumTracking tracking = findTrackingById(request.getTrackingId());
        Long userId = authenticationService.getUserIdFromToken(authToken);
        User performerUser = findUserById(userId);

        validateUserCanPerformAction(tracking, request.getActionType(), authToken);

        CurriculumTrackingStage fromStage = tracking.getCurrentStage();
        CurriculumTrackingStage toStage = determineTargetStage(tracking, request);

        User assigneeUser = null;
        String assigneeEmail = null;

        if (request.getAssignToUserId() != null) {
            assigneeUser = findUserById(request.getAssignToUserId());
            assigneeEmail = assigneeUser.getEmail();
        } else if (StringUtils.hasText(request.getAssignToEmail())) {

            assigneeUser = userRepository.findByEmail(request.getAssignToEmail())
                    .orElseThrow(() -> new BadRequestException("No user found with email: " + request.getAssignToEmail()));
            assigneeEmail = assigneeUser.getEmail();
        }

        updateTrackingForAction(tracking, request, toStage, assigneeUser != null ? assigneeUser.getId() : null);
        CurriculumTracking updatedTracking = curriculumTrackingRepository.saveAndFlush(tracking);

        // Create history entry
        CurriculumTrackingHistory historyEntry = CurriculumTrackingHistory.builder()
                .curriculumTracking(updatedTracking)
                .stage(fromStage)
                .actionType(request.getActionType())
                .performedBy(userId)
                .performedByEmail(performerUser.getEmail())
                .assignedTo(assigneeUser != null ? assigneeUser.getId() : null)
                .assignedToEmail(assigneeEmail)
                .fromStage(fromStage)
                .toStage(toStage)
                .comments(request.getComments())
                .dueDate(request.getDueDate())
                .isMilestone(request.isMilestone())
                .build();


        CurriculumTrackingHistoryDto savedHistoryDto = historyService.addHistoryEntry(historyEntry);

        historyService.flushHistoryChanges();

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            try {
                documentService.uploadMultipleDocuments(
                        savedHistoryDto.getId(),
                        request.getDocuments(),
                        request.getComments(),
                        authToken
                );
            } catch (Exception e) {
                log.warn("Failed to upload documents for tracking action: {}", e.getMessage());
            }
        }


        log.info("Successfully performed action {} on tracking {}", request.getActionType(), tracking.getId());

        User initiatorUser = findUserById(updatedTracking.getInitiatedBy());
        User currentAssigneeUser = updatedTracking.getCurrentAssignee() != null ?
                findUserById(updatedTracking.getCurrentAssignee()) : null;

        return trackingMapper.toDtoWithUserEmails(
                updatedTracking,
                initiatorUser.getEmail(),
                currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingDto getCurriculumTrackingById(Long trackingId) {
        log.debug("Fetching curriculum tracking by ID: {}", trackingId);

        CurriculumTracking tracking = curriculumTrackingRepository.findByIdWithCurriculumDetails(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum tracking not found with ID: " + trackingId));

        User initiatorUser = findUserById(tracking.getInitiatedBy());
        User currentAssigneeUser = tracking.getCurrentAssignee() != null ?
                findUserById(tracking.getCurrentAssignee()) : null;

        return trackingMapper.toDtoWithUserEmails(
                tracking,
                initiatorUser.getEmail(),
                currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingDto getCurriculumTrackingByCurriculumId(Long curriculumId) {
        log.debug("Fetching curriculum tracking by curriculum ID: {}", curriculumId);

        CurriculumTracking tracking = curriculumTrackingRepository.findByCurriculumId(curriculumId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum tracking not found for curriculum ID: " + curriculumId));

        User initiatorUser = findUserById(tracking.getInitiatedBy());
        User currentAssigneeUser = tracking.getCurrentAssignee() != null ?
                findUserById(tracking.getCurrentAssignee()) : null;

        return trackingMapper.toDtoWithUserEmails(
                tracking,
                initiatorUser.getEmail(),
                currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getAllCurriculumTrackings(Pageable pageable) {
        log.debug("Fetching all curriculum trackings with pagination: {}", pageable);

        Page<CurriculumTracking> trackingPage = curriculumTrackingRepository.findAll(pageable);
        return trackingMapper.buildPageResponseWithUserEmails(trackingPage, userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse searchCurriculumTrackings(CurriculumTrackingSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching curriculum trackings with criteria: {}", searchRequest);

        Specification<CurriculumTracking> spec = CurriculumTrackingSpecification.withCriteria(searchRequest);
        Page<CurriculumTracking> trackingPage = curriculumTrackingRepository.findAll(spec, pageable);

        return trackingMapper.buildPageResponseWithUserEmails(trackingPage, userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getMyAssignedTrackings(String authToken, Pageable pageable) {
        Long userId = authenticationService.getUserIdFromToken(authToken);
        log.debug("Fetching assigned trackings for user ID: {}", userId);

        Page<CurriculumTracking> trackingPage = curriculumTrackingRepository.findByCurrentAssignee(userId, pageable);
        return trackingMapper.buildPageResponseWithUserEmails(trackingPage, userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getCurriculumTrackingsByStage(CurriculumTrackingStage stage, Pageable pageable) {
        log.debug("Fetching curriculum trackings by stage: {}", stage);

        Page<CurriculumTracking> trackingPage = curriculumTrackingRepository.findByCurrentStage(stage, pageable);
        return trackingMapper.buildPageResponseWithUserEmails(trackingPage, userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingPageResponse getCurriculumTrackingsByStatus(CurriculumTrackingStatus status, Pageable pageable) {
        log.debug("Fetching curriculum trackings by status: {}", status);

        Page<CurriculumTracking> trackingPage = curriculumTrackingRepository.findByStatus(status, pageable);
        return trackingMapper.buildPageResponseWithUserEmails(trackingPage, userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDto> getOverdueTrackings() {
        log.debug("Fetching overdue curriculum trackings");

        List<CurriculumTracking> overdueTrackings = curriculumTrackingRepository.findOverdueTrackings(LocalDateTime.now());
        return overdueTrackings.stream()
                .map(tracking -> {
                    User initiatorUser = findUserById(tracking.getInitiatedBy());
                    User currentAssigneeUser = tracking.getCurrentAssignee() != null ?
                            findUserById(tracking.getCurrentAssignee()) : null;
                    return trackingMapper.toDtoWithUserEmails(
                            tracking,
                            initiatorUser.getEmail(),
                            currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumTrackingDto> getExpiringSoonTrackings(int days) {
        log.debug("Fetching trackings expiring in {} days", days);

        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime targetDate = currentDate.plusDays(days);

        List<CurriculumTracking> expiringSoon = curriculumTrackingRepository.findExpiringSoon(currentDate, targetDate);
        return expiringSoon.stream()
                .map(tracking -> {
                    User initiatorUser = findUserById(tracking.getInitiatedBy());
                    User currentAssigneeUser = tracking.getCurrentAssignee() != null ?
                            findUserById(tracking.getCurrentAssignee()) : null;
                    return trackingMapper.toDtoWithUserEmails(
                            tracking,
                            initiatorUser.getEmail(),
                            currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingStatsDto getCurriculumTrackingStats() {
        log.debug("Calculating curriculum tracking statistics");

        long totalTracked = curriculumTrackingRepository.count();
        long underReview = curriculumTrackingRepository.countByStatus(CurriculumTrackingStatus.UNDER_REVIEW);
        long accredited = curriculumTrackingRepository.countByStatus(CurriculumTrackingStatus.ACCREDITED);
        long approvedByCue = curriculumTrackingRepository.countByStatus(CurriculumTrackingStatus.APPROVED_BY_CUE);
        long minorRevamp = curriculumTrackingRepository.countByStatus(CurriculumTrackingStatus.MINOR_REVAMP);
        long majorRevamp = curriculumTrackingRepository.countByStatus(CurriculumTrackingStatus.MAJOR_REVAMP);

        long atSchoolBoard = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.SCHOOL_BOARD);
        long atDeanCommittee = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.DEAN_COMMITTEE);
        long atSenate = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.SENATE);
        long atQaInternalReview = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.QA_INTERNAL_REVIEW);
        long atViceChancellorReview = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.VICE_CHANCELLOR_REVIEW);
        long atCueExternalReview = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.CUE_EXTERNAL_REVIEW);
        long completed = curriculumTrackingRepository.countByCurrentStage(CurriculumTrackingStage.COMPLETED);

        Double avgCompletionTime = curriculumTrackingRepository.findAverageCompletionTimeInDays();
        double averageCompletionTimeInDays = avgCompletionTime != null ? avgCompletionTime : 0.0;

        long overdueTasks = curriculumTrackingRepository.findOverdueTrackings(LocalDateTime.now()).size();

        return trackingMapper.buildStatsDto(
                totalTracked, underReview, accredited, approvedByCue, minorRevamp, majorRevamp,
                atSchoolBoard, atDeanCommittee, atSenate, atQaInternalReview, atViceChancellorReview,
                atCueExternalReview, completed, averageCompletionTimeInDays, overdueTasks
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingTimelineDto getCurriculumTrackingTimeline(Long trackingId) {
        log.debug("Fetching curriculum tracking timeline for ID: {}", trackingId);

        CurriculumTracking tracking = findTrackingById(trackingId);
        List<CurriculumTrackingHistory> history = historyService.getTrackingHistory(trackingId)
                .stream()
                .map(dto -> {
                    return CurriculumTrackingHistory.builder()
                            .id(dto.getId())
                            .stage(dto.getStage())
                            .actionType(dto.getActionType())
                            .performedByEmail(dto.getPerformedByEmail())
                            .comments(dto.getComments())
                            .actionDate(dto.getActionDate())
                            .isMilestone(dto.isMilestone())
                            .build();
                })
                .collect(Collectors.toList());

        return trackingMapper.buildTimelineDto(tracking, history);
    }

    @Override
    public CurriculumTrackingDto assignTrackingToUser(Long trackingId, Long userId, String authToken) {
        log.info("Assigning tracking {} to user {}", trackingId, userId);

        validateQAAccess(authToken);

        CurriculumTracking tracking = findTrackingById(trackingId);
        User assigneeUser = findUserById(userId);

        tracking.setCurrentAssignee(userId);
        CurriculumTracking updatedTracking = curriculumTrackingRepository.save(tracking);

        User initiatorUser = findUserById(updatedTracking.getInitiatedBy());

        log.info("Successfully assigned tracking {} to user {}", trackingId, userId);
        return trackingMapper.toDtoWithUserEmails(
                updatedTracking,
                initiatorUser.getEmail(),
                assigneeUser.getEmail()
        );
    }

    @Override
    public CurriculumTrackingDto updateTrackingNotes(Long trackingId, String notes, String authToken) {
        log.info("Updating tracking notes for tracking ID: {}", trackingId);

        validateQAAccess(authToken);

        CurriculumTracking tracking = findTrackingById(trackingId);
        tracking.setNotes(notes);

        CurriculumTracking updatedTracking = curriculumTrackingRepository.save(tracking);

        User initiatorUser = findUserById(updatedTracking.getInitiatedBy());
        User currentAssigneeUser = updatedTracking.getCurrentAssignee() != null ?
                findUserById(updatedTracking.getCurrentAssignee()) : null;

        log.info("Successfully updated tracking notes for tracking ID: {}", trackingId);
        return trackingMapper.toDtoWithUserEmails(
                updatedTracking,
                initiatorUser.getEmail(),
                currentAssigneeUser != null ? currentAssigneeUser.getEmail() : null
        );
    }

    @Override
    public void deactivateCurriculumTracking(Long trackingId, String authToken) {
        log.info("Deactivating curriculum tracking ID: {}", trackingId);

        validateQAAccess(authToken);

        CurriculumTracking tracking = findTrackingById(trackingId);
        tracking.setActive(false);

        curriculumTrackingRepository.save(tracking);

        log.info("Successfully deactivated curriculum tracking ID: {}", trackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingActionType> getAvailableActions(Long trackingId, String authToken) {
        log.debug("Getting available actions for tracking ID: {}", trackingId);

        CurriculumTracking tracking = findTrackingById(trackingId);
        List<String> userRoles = authenticationService.getRolesFromToken(authToken);

        return determineAvailableActions(tracking.getCurrentStage(), userRoles);
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumTrackingStageInfo getStageInfo(CurriculumTrackingStage stage) {
        log.debug("Getting stage information for: {}", stage);

        return trackingMapper.toStageInfoDto(stage);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canPerformAction(Long trackingId, TrackingActionType actionType, String authToken) {
        try {
            CurriculumTracking tracking = findTrackingById(trackingId);
            validateUserCanPerformAction(tracking, actionType, authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void validateTrackingAction(CurriculumTrackingActionRequest request, String authToken) {
        if (request == null) {
            throw new BadRequestException("Tracking action request cannot be null");
        }

        if (request.getTrackingId() == null) {
            throw new BadRequestException("Tracking ID is required");
        }

        if (request.getActionType() == null) {
            throw new BadRequestException("Action type is required");
        }

        if (!authenticationService.validateToken(authToken)) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }


    private void validateQAAccess(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            throw new UnauthorizedException("Authorization token is required");
        }

        if (!authenticationService.validateToken(authToken)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        if (!authenticationService.hasRole(authToken, "QA")) {
            throw new UnauthorizedException("QA access required for this operation");
        }
    }

    private void validateInitiateRequest(InitiateCurriculumTrackingRequest request) {
        if (request == null) {
            throw new BadRequestException("Initiate tracking request cannot be null");
        }

        if (request.getCurriculumId() == null) {
            throw new BadRequestException("Curriculum ID is required");
        }
    }

    private Curriculum findCurriculumById(Long curriculumId) {
        return curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found with ID: " + curriculumId));
    }

    private CurriculumTracking findTrackingById(Long trackingId) {
        return curriculumTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum tracking not found with ID: " + trackingId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private void validateUserCanPerformAction(CurriculumTracking tracking, TrackingActionType actionType, String authToken) {
        List<String> userRoles = authenticationService.getRolesFromToken(authToken);
        CurriculumTrackingStage currentStage = tracking.getCurrentStage();

        String requiredRole = currentStage.getRequiredRole();

        if (!userRoles.contains(requiredRole) && !userRoles.contains("QA")) {
            throw new UnauthorizedException("User does not have permission to perform this action at stage: " + currentStage);
        }
    }

    private CurriculumTrackingStage determineTargetStage(CurriculumTracking tracking, CurriculumTrackingActionRequest request) {
        return switch (request.getActionType()) {
            case SUBMITTED, APPROVED -> tracking.getCurrentStage().getNextStage();
            case SENT_BACK ->
                    request.getTargetStage() != null ? request.getTargetStage() : tracking.getCurrentStage().getPreviousStage();
            case ACCREDITED -> CurriculumTrackingStage.COMPLETED;
            default -> tracking.getCurrentStage();
        };
    }

    private void updateTrackingForAction(CurriculumTracking tracking, CurriculumTrackingActionRequest request,
                                         CurriculumTrackingStage toStage, Long assigneeUserId) {
        switch (request.getActionType()) {
            case SUBMITTED:
            case APPROVED:
                tracking.setCurrentStage(toStage);
                if (assigneeUserId != null) {
                    tracking.setCurrentAssignee(assigneeUserId);
                }
                break;
            case SENT_BACK:
                tracking.setCurrentStage(toStage);
                tracking.setCurrentAssignee(null);
                break;
            case ACCREDITED:
                tracking.markAsCompleted(CurriculumTrackingStatus.ACCREDITED);
                break;
            case REJECTED:
                tracking.setActive(false);
                break;
            case REVAMP_REQUESTED:
                if (request.getComments() != null && request.getComments().toLowerCase().contains("major")) {
                    tracking.setStatus(CurriculumTrackingStatus.MAJOR_REVAMP);
                } else {
                    tracking.setStatus(CurriculumTrackingStatus.MINOR_REVAMP);
                }
                break;
        }
    }

    private List<TrackingActionType> determineAvailableActions(CurriculumTrackingStage stage, List<String> userRoles) {
        List<TrackingActionType> actions = List.of();

        String requiredRole = stage.getRequiredRole();
        boolean hasStageAccess = userRoles.contains(requiredRole) || userRoles.contains("QA");

        if (!hasStageAccess) {
            return actions;
        }

        actions = switch (stage) {
            case SCHOOL_BOARD, DEAN_COMMITTEE, SENATE ->
                    List.of(TrackingActionType.APPROVED, TrackingActionType.SENT_BACK, TrackingActionType.REVIEWED);
            case QA_INTERNAL_REVIEW, VICE_CHANCELLOR_REVIEW ->
                    List.of(TrackingActionType.APPROVED, TrackingActionType.SENT_BACK,
                            TrackingActionType.REVAMP_REQUESTED, TrackingActionType.REVIEWED);
            case CUE_EXTERNAL_REVIEW -> List.of(TrackingActionType.ACCREDITED, TrackingActionType.SENT_BACK,
                    TrackingActionType.REVAMP_REQUESTED);
            default -> List.of();
        };

        return actions;
    }
}