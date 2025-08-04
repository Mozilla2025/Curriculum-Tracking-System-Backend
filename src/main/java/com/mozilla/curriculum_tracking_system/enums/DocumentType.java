package com.mozilla.curriculum_tracking_system.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Types of documents in curriculum tracking system")
@Getter
public enum DocumentType {
    @Schema(description = "Main curriculum proposal document")
    CURRICULUM_PROPOSAL("Curriculum Proposal"),

    @Schema(description = "Additional supporting documents and materials")
    SUPPORTING_DOCUMENTS("Supporting Documents"),

    @Schema(description = "Documents for revisions and updates")
    REVISION_DOCUMENTS("Revision Documents"),

    @Schema(description = "Official approval certificates")
    APPROVAL_CERTIFICATE("Approval Certificate"),

    @Schema(description = "Audit and review reports")
    AUDIT_REPORT("Audit Report"),

    @Schema(description = "Other miscellaneous documents")
    OTHER("Other");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }
}
