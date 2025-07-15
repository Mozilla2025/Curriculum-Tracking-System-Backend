package com.mozilla.curriculum_tracking_system.repository.tracking;

import com.mozilla.curriculum_tracking_system.enums.DocumentType;
import com.mozilla.curriculum_tracking_system.model.tracking.TrackingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface TrackingDocumentRepository extends JpaRepository<TrackingDocument, Long> {

    @Query("""
        SELECT td FROM TrackingDocument td
        JOIN FETCH td.uploadedBy ub
        WHERE td.trackingStep.id = :stepId AND td.isActive = true
        ORDER BY td.uploadedAt DESC
        """)
    List<TrackingDocument> findByTrackingStepIdAndActiveTrue(@Param("stepId") Long stepId);

    @Query("""
        SELECT td FROM TrackingDocument td
        JOIN FETCH td.uploadedBy ub
        JOIN FETCH td.trackingStep ts
        WHERE ts.tracking.id = :trackingId AND td.isActive = true
        ORDER BY td.uploadedAt DESC
        """)
    List<TrackingDocument> findByTrackingIdAndActiveTrue(@Param("trackingId") Long trackingId);

    @Query("""
        SELECT td FROM TrackingDocument td
        WHERE td.documentName = :documentName 
        AND td.trackingStep.id = :stepId 
        AND td.isActive = true
        ORDER BY td.versionNumber DESC
        """)
    List<TrackingDocument> findVersionsByDocumentNameAndStepId(@Param("documentName") String documentName,
                                                               @Param("stepId") Long stepId);

    @Query("""
        SELECT td FROM TrackingDocument td
        WHERE td.documentName = :documentName 
        AND td.trackingStep.id = :stepId 
        AND td.isActive = true
        ORDER BY td.versionNumber DESC
        LIMIT 1
        """)
    Optional<TrackingDocument> findLatestVersionByDocumentNameAndStepId(@Param("documentName") String documentName,
                                                                        @Param("stepId") Long stepId);

    @Query("""
        SELECT td FROM TrackingDocument td
        JOIN FETCH td.uploadedBy ub
        WHERE td.documentType = :type AND td.isActive = true
        ORDER BY td.uploadedAt DESC
        """)
    List<TrackingDocument> findByDocumentTypeAndActiveTrue(@Param("type") DocumentType type);

    @Query("""
        SELECT td FROM TrackingDocument td
        JOIN FETCH td.trackingStep ts
        JOIN FETCH ts.tracking t
        WHERE td.uploadedBy.id = :userId AND td.isActive = true
        ORDER BY td.uploadedAt DESC
        """)
    List<TrackingDocument> findByUploadedByIdAndActiveTrue(@Param("userId") Long userId);

    List<TrackingDocument> findByUploadedAtBetweenAndActiveTrue(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByFilePathAndActiveTrue(String filePath);
    Optional<TrackingDocument> findByFilePathAndActiveTrue(String filePath);


    long countByDocumentTypeAndActiveTrue(DocumentType type);
    long countByUploadedAtBetweenAndActiveTrue(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(td.fileSize) FROM TrackingDocument td WHERE td.isActive = true")
    Long getTotalStorageUsed();

    @Query("""
        SELECT td.documentType, COUNT(td), SUM(td.fileSize)
        FROM TrackingDocument td
        WHERE td.isActive = true
        GROUP BY td.documentType
        """)
    List<Object[]> getStorageStatsByDocumentType();
}
