package com.mozilla.curriculum_tracking_system.enums;

import lombok.Getter;

@Getter
public enum DocumentType {
    CURRICULUM_PROPOSAL("Curriculum Proposal"),
    SUPPORTING_DOCUMENTS("Supporting Documents"),
    REVISION_DOCUMENTS("Revision Documents"),
    APPROVAL_CERTIFICATE("Approval Certificate"),
    AUDIT_REPORT("Audit Report"),
    OTHER("Other");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

}
