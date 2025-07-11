package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.model.tracking.CurriculumTrackingDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurriculumTrackingDocumentRepository extends JpaRepository<CurriculumTrackingDocument, Long> {

    List<CurriculumTrackingDocument> findByTrackingHistoryIdAndIsActiveOrderByUploadedAtDesc(Long trackingHistoryId,
                                                                                             boolean isActive);

    Page<CurriculumTrackingDocument> findByTrackingHistoryId(Long trackingHistoryId, Pageable pageable);

    Page<CurriculumTrackingDocument> findByUploadedByAndIsActiveOrderByUploadedAtDesc(Long uploadedBy,
                                                                                      boolean isActive,
                                                                                      Pageable pageable);

    @Query("SELECT ctd FROM CurriculumTrackingDocument ctd " +
            "JOIN ctd.trackingHistory cth " +
            "WHERE cth.curriculumTracking.id = :trackingId " +
            "AND ctd.isActive = true " +
            "ORDER BY ctd.uploadedAt DESC")
    List<CurriculumTrackingDocument> findByCurriculumTrackingId(@Param("trackingId") Long trackingId);

    Optional<CurriculumTrackingDocument> findByFirebasePathAndIsActive(String firebasePath, boolean isActive);

    List<CurriculumTrackingDocument> findByContentTypeAndIsActiveOrderByUploadedAtDesc(String contentType,
                                                                                       boolean isActive);

    List<CurriculumTrackingDocument> findByFileExtensionAndIsActiveOrderByUploadedAtDesc(String fileExtension,
                                                                                         boolean isActive);

    @Query("SELECT ctd FROM CurriculumTrackingDocument ctd " +
            "WHERE ctd.fileSize > :sizeLimit " +
            "AND ctd.isActive = true " +
            "ORDER BY ctd.fileSize DESC")
    List<CurriculumTrackingDocument> findLargeFiles(@Param("sizeLimit") Long sizeLimit);

    List<CurriculumTrackingDocument> findByUploadedAtBetweenAndIsActiveOrderByUploadedAtDesc(LocalDateTime startDate,
                                                                                             LocalDateTime endDate,
                                                                                             boolean isActive);

    long countByTrackingHistoryIdAndIsActive(Long trackingHistoryId, boolean isActive);

    @Query("SELECT COUNT(ctd) FROM CurriculumTrackingDocument ctd " +
            "JOIN ctd.trackingHistory cth " +
            "WHERE cth.curriculumTracking.id = :trackingId " +
            "AND ctd.isActive = true")
    long countByCurriculumTrackingId(@Param("trackingId") Long trackingId);

    @Query("SELECT ctd FROM CurriculumTrackingDocument ctd " +
            "WHERE ctd.documentName = :documentName " +
            "AND ctd.trackingHistory.id = :trackingHistoryId " +
            "AND ctd.isActive = true " +
            "ORDER BY ctd.documentVersion DESC")
    List<CurriculumTrackingDocument> findVersionsByDocumentName(@Param("documentName") String documentName,
                                                                @Param("trackingHistoryId") Long trackingHistoryId);

    @Query("SELECT ctd FROM CurriculumTrackingDocument ctd " +
            "WHERE ctd.documentName = :documentName " +
            "AND ctd.trackingHistory.id = :trackingHistoryId " +
            "AND ctd.isActive = true " +
            "ORDER BY ctd.documentVersion DESC")
    Optional<CurriculumTrackingDocument> findLatestVersionByDocumentName(@Param("documentName") String documentName,
                                                                         @Param("trackingHistoryId") Long trackingHistoryId);

    @Query("SELECT ctd FROM CurriculumTrackingDocument ctd " +
            "WHERE ctd.isActive = true " +
            "AND (LOWER(ctd.documentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(ctd.originalFilename) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(ctd.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY ctd.uploadedAt DESC")
    Page<CurriculumTrackingDocument> searchDocuments(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("UPDATE CurriculumTrackingDocument ctd " +
            "SET ctd.isActive = false " +
            "WHERE ctd.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Query("SELECT ctd FROM CurriculumTrackingDocument ctd " +
            "WHERE ctd.trackingHistory IS NULL " +
            "OR ctd.trackingHistory.id NOT IN (SELECT cth.id FROM CurriculumTrackingHistory cth)")
    List<CurriculumTrackingDocument> findOrphanedDocuments();

    @Query("SELECT SUM(ctd.fileSize) FROM CurriculumTrackingDocument ctd WHERE ctd.isActive = true")
    Long getTotalStorageUsed();

    @Query("SELECT ctd.contentType, COUNT(ctd), SUM(ctd.fileSize) " +
            "FROM CurriculumTrackingDocument ctd " +
            "WHERE ctd.isActive = true " +
            "GROUP BY ctd.contentType " +
            "ORDER BY COUNT(ctd) DESC")
    List<Object[]> getStorageStatsByContentType();
}
