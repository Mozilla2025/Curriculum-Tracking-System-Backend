-- Migration for Curriculum Tracking System
-- Create curriculum_tracking table
CREATE TABLE curriculum_tracking (
    id BIGSERIAL PRIMARY KEY,
    curriculum_id BIGINT NOT NULL UNIQUE,
    current_stage VARCHAR(50) NOT NULL DEFAULT 'SCHOOL_BOARD',
    status VARCHAR(50) NOT NULL DEFAULT 'UNDER_REVIEW',
    initiated_by BIGINT NOT NULL,
    current_assignee BIGINT,
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_completion_date TIMESTAMP,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_curriculum_tracking_curriculum
        FOREIGN KEY (curriculum_id) REFERENCES curriculums(id) ON DELETE CASCADE,
    CONSTRAINT fk_curriculum_tracking_initiated_by
        FOREIGN KEY (initiated_by) REFERENCES users(id),
    CONSTRAINT fk_curriculum_tracking_current_assignee
        FOREIGN KEY (current_assignee) REFERENCES users(id),

    CONSTRAINT chk_curriculum_tracking_stage
        CHECK (current_stage IN ('SCHOOL_BOARD', 'DEAN_COMMITTEE', 'SENATE',
                                'QA_INTERNAL_REVIEW', 'VICE_CHANCELLOR_REVIEW',
                                'CUE_EXTERNAL_REVIEW', 'COMPLETED')),
    CONSTRAINT chk_curriculum_tracking_status
        CHECK (status IN ('UNDER_REVIEW', 'ACCREDITED', 'APPROVED_BY_CUE',
                         'MINOR_REVAMP', 'MAJOR_REVAMP'))
);

-- Create curriculum_tracking_history table
CREATE TABLE curriculum_tracking_history (
    id BIGSERIAL PRIMARY KEY,
    curriculum_tracking_id BIGINT NOT NULL,
    stage VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    performed_by BIGINT NOT NULL,
    performed_by_email VARCHAR(100) NOT NULL,
    assigned_to BIGINT,
    assigned_to_email VARCHAR(100),
    from_stage VARCHAR(50),
    to_stage VARCHAR(50),
    comments TEXT,
    action_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    is_milestone BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_tracking_history_curriculum_tracking
        FOREIGN KEY (curriculum_tracking_id) REFERENCES curriculum_tracking(id) ON DELETE CASCADE,
    CONSTRAINT fk_tracking_history_performed_by
        FOREIGN KEY (performed_by) REFERENCES users(id),
    CONSTRAINT fk_tracking_history_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users(id),

    CONSTRAINT chk_tracking_history_stage
        CHECK (stage IN ('SCHOOL_BOARD', 'DEAN_COMMITTEE', 'SENATE',
                        'QA_INTERNAL_REVIEW', 'VICE_CHANCELLOR_REVIEW',
                        'CUE_EXTERNAL_REVIEW', 'COMPLETED')),
    CONSTRAINT chk_tracking_history_action_type
        CHECK (action_type IN ('SUBMITTED', 'APPROVED', 'SENT_BACK', 'REVIEWED',
                              'REJECTED', 'ACCREDITED', 'REVAMP_REQUESTED')),
    CONSTRAINT chk_tracking_history_from_stage
        CHECK (from_stage IS NULL OR from_stage IN ('SCHOOL_BOARD', 'DEAN_COMMITTEE', 'SENATE',
                                                   'QA_INTERNAL_REVIEW', 'VICE_CHANCELLOR_REVIEW',
                                                   'CUE_EXTERNAL_REVIEW', 'COMPLETED')),
    CONSTRAINT chk_tracking_history_to_stage
        CHECK (to_stage IS NULL OR to_stage IN ('SCHOOL_BOARD', 'DEAN_COMMITTEE', 'SENATE',
                                               'QA_INTERNAL_REVIEW', 'VICE_CHANCELLOR_REVIEW',
                                               'CUE_EXTERNAL_REVIEW', 'COMPLETED'))
);

-- Create curriculum_tracking_documents table
CREATE TABLE curriculum_tracking_documents (
    id BIGSERIAL PRIMARY KEY,
    tracking_history_id BIGINT NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    firebase_url TEXT NOT NULL,
    firebase_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    file_extension VARCHAR(10),
    description TEXT,
    uploaded_by BIGINT NOT NULL,
    uploaded_by_email VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    document_version INTEGER NOT NULL DEFAULT 1,
    checksum VARCHAR(64),

    CONSTRAINT fk_tracking_documents_history
        FOREIGN KEY (tracking_history_id) REFERENCES curriculum_tracking_history(id) ON DELETE CASCADE,
    CONSTRAINT fk_tracking_documents_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(id),

    CONSTRAINT chk_tracking_documents_file_size
        CHECK (file_size IS NULL OR file_size > 0),
    CONSTRAINT chk_tracking_documents_document_version
        CHECK (document_version > 0)
);

-- Create indexes for better performance
CREATE INDEX idx_curriculum_tracking_curriculum_id ON curriculum_tracking(curriculum_id);
CREATE INDEX idx_curriculum_tracking_current_stage ON curriculum_tracking(current_stage);
CREATE INDEX idx_curriculum_tracking_status ON curriculum_tracking(status);
CREATE INDEX idx_curriculum_tracking_current_assignee ON curriculum_tracking(current_assignee);
CREATE INDEX idx_curriculum_tracking_initiated_by ON curriculum_tracking(initiated_by);
CREATE INDEX idx_curriculum_tracking_is_active ON curriculum_tracking(is_active);
CREATE INDEX idx_curriculum_tracking_initiated_at ON curriculum_tracking(initiated_at);
CREATE INDEX idx_curriculum_tracking_completed_at ON curriculum_tracking(completed_at);

CREATE INDEX idx_tracking_history_curriculum_tracking_id ON curriculum_tracking_history(curriculum_tracking_id);
CREATE INDEX idx_tracking_history_stage ON curriculum_tracking_history(stage);
CREATE INDEX idx_tracking_history_action_type ON curriculum_tracking_history(action_type);
CREATE INDEX idx_tracking_history_performed_by ON curriculum_tracking_history(performed_by);
CREATE INDEX idx_tracking_history_assigned_to ON curriculum_tracking_history(assigned_to);
CREATE INDEX idx_tracking_history_action_date ON curriculum_tracking_history(action_date);
CREATE INDEX idx_tracking_history_due_date ON curriculum_tracking_history(due_date);
CREATE INDEX idx_tracking_history_is_milestone ON curriculum_tracking_history(is_milestone);

CREATE INDEX idx_tracking_documents_tracking_history_id ON curriculum_tracking_documents(tracking_history_id);
CREATE INDEX idx_tracking_documents_uploaded_by ON curriculum_tracking_documents(uploaded_by);
CREATE INDEX idx_tracking_documents_is_active ON curriculum_tracking_documents(is_active);
CREATE INDEX idx_tracking_documents_uploaded_at ON curriculum_tracking_documents(uploaded_at);
CREATE INDEX idx_tracking_documents_content_type ON curriculum_tracking_documents(content_type);
CREATE INDEX idx_tracking_documents_file_extension ON curriculum_tracking_documents(file_extension);

-- Create composite indexes for common queries
CREATE INDEX idx_curriculum_tracking_stage_status ON curriculum_tracking(current_stage, status);
CREATE INDEX idx_curriculum_tracking_assignee_stage ON curriculum_tracking(current_assignee, current_stage) WHERE current_assignee IS NOT NULL;
CREATE INDEX idx_tracking_history_curriculum_action_date ON curriculum_tracking_history(curriculum_tracking_id, action_date DESC);
CREATE INDEX idx_tracking_documents_history_active ON curriculum_tracking_documents(tracking_history_id, is_active);

-- Add comments to tables and columns for documentation
COMMENT ON TABLE curriculum_tracking IS 'Main table for tracking curriculum approval process through various stages';
COMMENT ON COLUMN curriculum_tracking.current_stage IS 'Current stage in the approval process';
COMMENT ON COLUMN curriculum_tracking.status IS 'Overall status of the curriculum tracking';
COMMENT ON COLUMN curriculum_tracking.initiated_by IS 'QA user who initiated the tracking process';
COMMENT ON COLUMN curriculum_tracking.current_assignee IS 'User currently responsible for the curriculum at current stage';

COMMENT ON TABLE curriculum_tracking_history IS 'Audit trail of all actions taken during curriculum tracking';
COMMENT ON COLUMN curriculum_tracking_history.stage IS 'Stage where this action was performed';
COMMENT ON COLUMN curriculum_tracking_history.action_type IS 'Type of action performed (submitted, approved, sent back, etc.)';
COMMENT ON COLUMN curriculum_tracking_history.from_stage IS 'Previous stage (for stage transitions)';
COMMENT ON COLUMN curriculum_tracking_history.to_stage IS 'Next stage (for stage transitions)';
COMMENT ON COLUMN curriculum_tracking_history.is_milestone IS 'Whether this action represents a major milestone';

COMMENT ON TABLE curriculum_tracking_documents IS 'Documents attached to curriculum tracking history entries';
COMMENT ON COLUMN curriculum_tracking_documents.firebase_url IS 'Public URL to access the document in Firebase Storage';
COMMENT ON COLUMN curriculum_tracking_documents.firebase_path IS 'Storage path/key in Firebase Storage';
COMMENT ON COLUMN curriculum_tracking_documents.document_version IS 'Version number for document versioning';
COMMENT ON COLUMN curriculum_tracking_documents.checksum IS 'File checksum for integrity verification';