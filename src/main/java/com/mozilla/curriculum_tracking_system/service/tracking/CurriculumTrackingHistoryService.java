package com.mozilla.curriculum_tracking_system.service.tracking;

import com.mozilla.curriculum_tracking_system.dto.tracking.CurriculumTrackingHistoryDto;
import com.mozilla.curriculum_tracking_system.enums.CurriculumTrackingStage;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.CurriculumTrackingMapper;
import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingHistory;
import com.mozilla.curriculum_tracking_system.repository.tracking.CurriculumTrackingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CurriculumTrackingHistoryService implements ICurriculumTrackingHistoryService {

    private final CurriculumTrackingHistoryRepository historyRepository;
    private final CurriculumTrackingMapper trackingMapper;

    @Override
    public List<CurriculumTrackingHistoryDto> getTrackingHistory(Long curriculumTrackingId) {
        log.debug("Fetching tracking history for curriculum tracking ID: {}", curriculumTrackingId);

        List<CurriculumTrackingHistory> history = historyRepository
                .findByCurriculumTrackingIdOrderByActionDateDesc(curriculumTrackingId);
        return trackingMapper.toHistoryDtoList(history);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> getTrackingHistory(Long curriculumTrackingId, Pageable pageable) {
        log.debug("Fetching tracking history for curriculum tracking ID: {} with pagination", curriculumTrackingId);

        List<CurriculumTrackingHistory> history = historyRepository
                .findByCurriculumTrackingId(curriculumTrackingId, pageable)
                .getContent();
        return trackingMapper.toHistoryDtoList(history);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> getRecentTrackingHistory(Long curriculumTrackingId, int limit) {
        log.debug("Fetching recent {} tracking history entries for curriculum tracking ID: {}",
                limit, curriculumTrackingId);

        Pageable pageable = PageRequest.of(0, limit);
        List<CurriculumTrackingHistory> recentHistory = historyRepository
                .findRecentByCurriculumTrackingId(curriculumTrackingId, pageable);
        return trackingMapper.toHistoryDtoList(recentHistory);
    }

    @Override
    public CurriculumTrackingHistoryDto getTrackingHistoryById(Long historyId) {
        log.debug("Fetching tracking history by ID: {}", historyId);

        CurriculumTrackingHistory history = historyRepository.findByIdWithDocuments(historyId);
        if (history == null) {
            throw new ResourceNotFoundException("Tracking history not found with ID: " + historyId);
        }
        return trackingMapper.toHistoryDto(history);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> getMilestoneHistory(Long curriculumTrackingId) {
        log.debug("Fetching milestone history for curriculum tracking ID: {}", curriculumTrackingId);

        List<CurriculumTrackingHistory> milestones = historyRepository
                .findMilestonesByCurriculumTrackingId(curriculumTrackingId);

        return trackingMapper.toHistoryDtoList(milestones);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> getStageTransitions(Long curriculumTrackingId) {
        log.debug("Fetching stage transitions for curriculum tracking ID: {}", curriculumTrackingId);

        List<CurriculumTrackingHistory> transitions = historyRepository
                .findByCurriculumTrackingIdOrderByActionDateDesc(curriculumTrackingId)
                .stream()
                .filter(CurriculumTrackingHistory::isStageTransition)
                .toList();

        return trackingMapper.toHistoryDtoList(transitions);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> getHistoryByPerformer(Long userId, Pageable pageable) {
        log.debug("Fetching tracking history by performer ID: {}", userId);

        List<CurriculumTrackingHistory> history = historyRepository
                .findByPerformedByOrderByActionDateDesc(userId, pageable)
                .getContent();

        return trackingMapper.toHistoryDtoList(history);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> getHistoryByAssignee(Long userId, Pageable pageable) {
        log.debug("Fetching tracking history by assignee ID: {}", userId);

        List<CurriculumTrackingHistory> history = historyRepository
                .findByAssignedToOrderByActionDateDesc(userId, pageable)
                .getContent();

        return trackingMapper.toHistoryDtoList(history);
    }

    @Override
    public List<CurriculumTrackingHistoryDto> searchTrackingHistory(String searchTerm) {
        log.debug("Searching tracking history with term: {}", searchTerm);

        List<CurriculumTrackingHistory> history = historyRepository
                .searchInComments(searchTerm);

        return trackingMapper.toHistoryDtoList(history);
    }

    @Override
    @Transactional
    public CurriculumTrackingHistoryDto addHistoryEntry(CurriculumTrackingHistory historyEntry) {
        log.debug("Adding new history entry for curriculum tracking ID: {}",
                historyEntry.getCurriculumTracking() != null ?
                        historyEntry.getCurriculumTracking().getId() : "null");

        // Validate required fields
        validateHistoryEntry(historyEntry);

        if (historyEntry.getActionDate() == null) {
            historyEntry.setActionDate(LocalDateTime.now());
        }

        if (historyEntry.isStageTransition()) {
            log.debug("Stage transition detected: {} -> {}",
                    historyEntry.getFromStage(), historyEntry.getToStage());

            log.debug("Movement direction: Forward={}, Backward={}",
                    historyEntry.isForwardMovement(), historyEntry.isBackwardMovement());
        }

        CurriculumTrackingHistory savedHistory = historyRepository.save(historyEntry);

        log.info("Successfully added history entry with ID: {} for tracking: {}",
                savedHistory.getId(), savedHistory.getCurriculumTracking().getId());

        return trackingMapper.toHistoryDto(savedHistory);
    }


    @Override
    public List<CurriculumTrackingHistoryDto> getOverdueHistoryItems() {
        log.debug("Fetching overdue history items");

        List<CurriculumTrackingHistory> overdueItems = historyRepository
                .findOverdueItems(LocalDateTime.now());

        return trackingMapper.toHistoryDtoList(overdueItems);
    }

    @Override
    @Transactional
    public void flushHistoryChanges() {
        historyRepository.flush();
    }


    private void validateHistoryEntry(CurriculumTrackingHistory historyEntry) {
        if (historyEntry.getCurriculumTracking() == null) {
            throw new IllegalArgumentException("Curriculum tracking is required for history entry");
        }

        if (historyEntry.getStage() == null) {
            throw new IllegalArgumentException("Stage is required for history entry");
        }

        if (historyEntry.getActionType() == null) {
            throw new IllegalArgumentException("Action type is required for history entry");
        }

        if (historyEntry.getPerformedBy() == null) {
            throw new IllegalArgumentException("Performed by user ID is required for history entry");
        }

        if (historyEntry.getPerformedByEmail() == null || historyEntry.getPerformedByEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Performed by email is required for history entry");
        }
    }

    @Override
    public long countActionsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Counting actions between {} and {}", startDate, endDate);
        return historyRepository.countByActionDateBetween(startDate, endDate);
    }

    private boolean isForwardMovement(CurriculumTrackingStage fromStage, CurriculumTrackingStage toStage) {

        // Define stage order for forward movement detection
        List<CurriculumTrackingStage> stageOrder = List.of(
                CurriculumTrackingStage.SCHOOL_BOARD,
                CurriculumTrackingStage.DEAN_COMMITTEE,
                CurriculumTrackingStage.SENATE,
                CurriculumTrackingStage.QA_INTERNAL_REVIEW,
                CurriculumTrackingStage.VICE_CHANCELLOR_REVIEW,
                CurriculumTrackingStage.CUE_EXTERNAL_REVIEW,
                CurriculumTrackingStage.COMPLETED
        );

        int fromIndex = stageOrder.indexOf(fromStage);
        int toIndex = stageOrder.indexOf(toStage);

        return fromIndex != -1 && toIndex != -1 && toIndex > fromIndex;
    }
}
