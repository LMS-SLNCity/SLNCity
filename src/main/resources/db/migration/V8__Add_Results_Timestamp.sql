-- V8__Add_Results_Timestamp.sql
-- Add timestamp tracking for test results entry
-- Addresses Issue #27: Timestamp tracking for audit trail

-- Add results_entered_at column to lab_tests table
ALTER TABLE lab_tests 
ADD COLUMN IF NOT EXISTS results_entered_at TIMESTAMP;

-- Add comment for documentation
COMMENT ON COLUMN lab_tests.results_entered_at IS 'Timestamp when test results were entered for audit trail compliance';

-- Update existing records with a default timestamp (optional)
-- UPDATE lab_tests 
-- SET results_entered_at = approved_at 
-- WHERE results_entered_at IS NULL AND approved_at IS NOT NULL;

-- Create index for performance on timestamp queries
CREATE INDEX IF NOT EXISTS idx_lab_tests_results_entered_at ON lab_tests(results_entered_at);

-- Audit trail view for compliance reporting
CREATE OR REPLACE VIEW test_audit_trail AS
SELECT 
    lt.test_id,
    lt.visit_id,
    tt.name as test_name,
    lt.status,
    lt.results_entered_at,
    lt.approved,
    lt.approved_by,
    lt.approved_at,
    CASE 
        WHEN lt.results_entered_at IS NOT NULL AND lt.approved_at IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (lt.approved_at - lt.results_entered_at))/3600 
        ELSE NULL 
    END as approval_time_hours
FROM lab_tests lt
JOIN test_templates tt ON lt.test_template_id = tt.template_id
WHERE lt.results_entered_at IS NOT NULL
ORDER BY lt.results_entered_at DESC;

COMMENT ON VIEW test_audit_trail IS 'Audit trail view for test result entry and approval tracking';
