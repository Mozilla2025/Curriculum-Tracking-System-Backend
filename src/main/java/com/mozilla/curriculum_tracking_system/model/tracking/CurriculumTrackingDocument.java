package com.mozilla.curriculum_tracking_system.model.tracking;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "curriculum_tracking_documents")
@ToString(exclude = {"trackingHistory"})
public class CurriculumTrackingDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_history_id", nullable = false)
    private CurriculumTrackingHistory trackingHistory;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "firebase_url", nullable = false, columnDefinition = "TEXT")
    private String firebaseUrl; // URL to the document in Firebase Storage

    @Column(name = "firebase_path", nullable = false)
    private String firebasePath; // Path/key in Firebase Storage

    @Column(name = "file_size")
    private Long fileSize; // File size in bytes

    @Column(name = "content_type")
    private String contentType; // MIME type (application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, etc.)

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(columnDefinition = "TEXT")
    private String description; // Description of the document

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy; // User ID who uploaded the document

    @Column(name = "uploaded_by_email", nullable = false)
    private String uploadedByEmail;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "document_version")
    @Builder.Default
    private Integer documentVersion = 1;

    @Column(name = "checksum")
    private String checksum;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * Get file extension from original filename if not set
     */
    public String getFileExtension() {
        if (this.fileExtension != null) {
            return this.fileExtension;
        }

        if (this.originalFilename != null && this.originalFilename.contains(".")) {
            return this.originalFilename.substring(this.originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        return "unknown";
    }

    /**
     * Check if the document is a PDF
     */
    public boolean isPdf() {
        return "pdf".equalsIgnoreCase(getFileExtension()) ||
                "application/pdf".equals(this.contentType);
    }

    /**
     * Check if the document is a Word document
     */
    public boolean isWordDocument() {
        String ext = getFileExtension();
        return "doc".equalsIgnoreCase(ext) ||
                "docx".equalsIgnoreCase(ext) ||
                "application/msword".equals(this.contentType) ||
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(this.contentType);
    }


    public String getFormattedFileSize() {
        if (this.fileSize == null) return "Unknown size";

        if (this.fileSize < 1024) {
            return this.fileSize + " B";
        } else if (this.fileSize < 1024 * 1024) {
            return String.format("%.1f KB", this.fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", this.fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * Deactivate the document
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Create a new version of this document
     */
    public CurriculumTrackingDocument createNewVersion() {
        return CurriculumTrackingDocument.builder()
                .trackingHistory(this.trackingHistory)
                .documentName(this.documentName)
                .originalFilename(this.originalFilename)
                .contentType(this.contentType)
                .fileExtension(this.fileExtension)
                .description(this.description)
                .uploadedBy(this.uploadedBy)
                .uploadedByEmail(this.uploadedByEmail)
                .documentVersion(this.documentVersion + 1)
                .isActive(true)
                .build();
    }
}