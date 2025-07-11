-- Migration to update roles for curriculum tracking system

-- Insert new roles for curriculum tracking if they don't exist
INSERT INTO roles (name, description, created_at, updated_at)
VALUES
    ('QA', 'Quality Assurance Officer - Senior Administrator for curriculum tracking', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SCHOOL_BOARD', 'School Board Member - Reviews curriculum at school level', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DEAN', 'Dean Committee Member - Critical curriculum review stage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SENATE', 'Senate Member - University-level curriculum review', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Update existing DEAN role description if it exists but has different description
UPDATE roles
SET
    description = 'Dean Committee Member - Critical curriculum review stage',
    updated_at = CURRENT_TIMESTAMP
WHERE name = 'DEAN'
AND description != 'Dean Committee Member - Critical curriculum review stage';

-- Create a view for easier role management queries
CREATE OR REPLACE VIEW v_curriculum_tracking_roles AS
SELECT
    r.id,
    r.name,
    r.description,
    COUNT(ur.user_id) as user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
WHERE r.name IN ('QA', 'SCHOOL_BOARD', 'DEAN', 'SENATE', 'ADMIN')
GROUP BY r.id, r.name, r.description
ORDER BY
    CASE r.name
        WHEN 'ADMIN' THEN 1
        WHEN 'QA' THEN 2
        WHEN 'SCHOOL_BOARD' THEN 3
        WHEN 'DEAN' THEN 4
        WHEN 'SENATE' THEN 5
        ELSE 6
    END;

-- Add comment to the view
COMMENT ON VIEW v_curriculum_tracking_roles IS 'View showing all roles relevant to curriculum tracking system with user counts';

CREATE OR REPLACE FUNCTION get_users_by_tracking_role(role_name VARCHAR)
RETURNS TABLE (
    user_id BIGINT,
    username VARCHAR,
    email VARCHAR,
    first_name VARCHAR,
    last_name VARCHAR,
    is_enabled BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.id,
        u.username,
        u.email,
        u.first_name,
        u.last_name,
        u.is_enabled
    FROM users u
    INNER JOIN user_roles ur ON u.id = ur.user_id
    INNER JOIN roles r ON ur.role_id = r.id
    WHERE r.name = role_name
    AND u.is_enabled = TRUE
    ORDER BY u.first_name, u.last_name;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_users_by_tracking_role(VARCHAR) IS 'Function to get all active users with a specific tracking role';

CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

-- Create a composite index for frequent role-based user queries
CREATE INDEX IF NOT EXISTS idx_user_roles_composite ON user_roles(role_id, user_id);

-- Add constraints to ensure data integrity for tracking-related roles
ALTER TABLE user_roles
ADD CONSTRAINT chk_user_roles_unique_per_user_role
UNIQUE (user_id, role_id);

-- Create a trigger to automatically log role changes for tracking system users
CREATE OR REPLACE FUNCTION log_tracking_role_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.user_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.user_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
DROP TRIGGER IF EXISTS trigger_log_tracking_role_changes ON user_roles;
CREATE TRIGGER trigger_log_tracking_role_changes
    AFTER INSERT OR DELETE ON user_roles
    FOR EACH ROW
    EXECUTE FUNCTION log_tracking_role_changes();

-- Add comment to the trigger
COMMENT ON TRIGGER trigger_log_tracking_role_changes ON user_roles IS 'Trigger to update user timestamp when tracking roles are modified';