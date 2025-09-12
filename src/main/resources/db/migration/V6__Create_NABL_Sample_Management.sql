-- NABL-compliant Sample Management Tables
-- Implements complete sample lifecycle tracking according to NABL 112 requirements

-- Create samples table
CREATE TABLE samples (
    sample_id BIGSERIAL PRIMARY KEY,
    sample_number VARCHAR(50) UNIQUE NOT NULL,
    visit_id BIGINT NOT NULL REFERENCES visits(visit_id),
    sample_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'COLLECTED',
    
    -- NABL Collection Requirements
    collected_at TIMESTAMP NOT NULL,
    collected_by VARCHAR(100) NOT NULL,
    collection_site VARCHAR(200),
    collection_conditions JSON,
    
    -- NABL Receipt Requirements
    received_at TIMESTAMP,
    received_by VARCHAR(100),
    receipt_temperature DECIMAL(5,2),
    receipt_condition VARCHAR(100),
    
    -- NABL Processing Requirements
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processed_by VARCHAR(100),
    
    -- NABL Storage Requirements
    storage_location VARCHAR(200),
    storage_temperature DECIMAL(5,2),
    storage_conditions VARCHAR(500),
    
    -- NABL Chain of Custody
    chain_of_custody JSON,
    
    -- NABL Quality Control
    volume_received DECIMAL(8,2),
    volume_required DECIMAL(8,2),
    container_type VARCHAR(100),
    preservative VARCHAR(100),
    
    -- NABL Rejection Criteria
    rejected BOOLEAN DEFAULT FALSE,
    rejection_reason TEXT,
    rejected_by VARCHAR(100),
    rejected_at TIMESTAMP,
    
    -- NABL Disposal Requirements
    disposed_at TIMESTAMP,
    disposed_by VARCHAR(100),
    disposal_method VARCHAR(200),
    disposal_batch VARCHAR(100),
    
    -- NABL Comments and Notes
    comments TEXT,
    quality_indicators JSON,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_sample_status CHECK (status IN (
        'COLLECTED', 'IN_TRANSIT', 'RECEIVED', 'ACCESSIONED', 'ACCEPTED', 'REJECTED',
        'PROCESSING', 'ALIQUOTED', 'IN_ANALYSIS', 'ANALYSIS_COMPLETE', 'UNDER_REVIEW',
        'REVIEWED', 'STORED', 'DISPOSED', 'ON_HOLD', 'RECALLED'
    )),
    CONSTRAINT chk_sample_type CHECK (sample_type IN (
        'WHOLE_BLOOD', 'SERUM', 'PLASMA', 'RANDOM_URINE', 'FIRST_MORNING_URINE',
        'MIDSTREAM_URINE', 'TWENTY_FOUR_HOUR_URINE', 'CEREBROSPINAL_FLUID',
        'SYNOVIAL_FLUID', 'PLEURAL_FLUID', 'ASCITIC_FLUID', 'THROAT_SWAB',
        'NASAL_SWAB', 'WOUND_SWAB', 'VAGINAL_SWAB', 'STOOL', 'SPUTUM',
        'TISSUE_BIOPSY', 'SALIVA', 'HAIR', 'NAIL'
    )),
    CONSTRAINT chk_volume_positive CHECK (volume_received >= 0 AND volume_required >= 0),
    CONSTRAINT chk_temperature_range CHECK (
        receipt_temperature IS NULL OR (receipt_temperature >= -80 AND receipt_temperature <= 50)
    ),
    CONSTRAINT chk_storage_temperature_range CHECK (
        storage_temperature IS NULL OR (storage_temperature >= -80 AND storage_temperature <= 50)
    )
);

-- Create indexes for performance
CREATE INDEX idx_samples_sample_number ON samples(sample_number);
CREATE INDEX idx_samples_visit_id ON samples(visit_id);
CREATE INDEX idx_samples_status ON samples(status);
CREATE INDEX idx_samples_sample_type ON samples(sample_type);
CREATE INDEX idx_samples_collected_at ON samples(collected_at);
CREATE INDEX idx_samples_received_at ON samples(received_at);
CREATE INDEX idx_samples_collected_by ON samples(collected_by);
CREATE INDEX idx_samples_received_by ON samples(received_by);
CREATE INDEX idx_samples_processed_by ON samples(processed_by);
CREATE INDEX idx_samples_rejected ON samples(rejected);
CREATE INDEX idx_samples_storage_location ON samples(storage_location);
CREATE INDEX idx_samples_disposal_batch ON samples(disposal_batch);
CREATE INDEX idx_samples_created_at ON samples(created_at);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_samples_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_samples_updated_at
    BEFORE UPDATE ON samples
    FOR EACH ROW
    EXECUTE FUNCTION update_samples_updated_at();

-- Create sample audit log table for NABL compliance
CREATE TABLE sample_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT NOT NULL REFERENCES samples(sample_id),
    sample_number VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSON,
    comments TEXT,
    
    CONSTRAINT chk_audit_action CHECK (action IN (
        'COLLECTED', 'RECEIVED', 'ACCEPTED', 'REJECTED', 'PROCESSING_STARTED',
        'ALIQUOTED', 'ANALYSIS_STARTED', 'ANALYSIS_COMPLETED', 'REVIEWED',
        'STORED', 'DISPOSED', 'STATUS_CHANGED', 'RECALLED', 'ON_HOLD'
    ))
);

-- Create indexes for audit log
CREATE INDEX idx_sample_audit_sample_id ON sample_audit_log(sample_id);
CREATE INDEX idx_sample_audit_sample_number ON sample_audit_log(sample_number);
CREATE INDEX idx_sample_audit_action ON sample_audit_log(action);
CREATE INDEX idx_sample_audit_performed_by ON sample_audit_log(performed_by);
CREATE INDEX idx_sample_audit_performed_at ON sample_audit_log(performed_at);

-- Create trigger to automatically log sample status changes
CREATE OR REPLACE FUNCTION log_sample_status_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Only log if status actually changed
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO sample_audit_log (
            sample_id, sample_number, action, old_status, new_status,
            performed_by, details
        ) VALUES (
            NEW.sample_id, NEW.sample_number, 'STATUS_CHANGED',
            OLD.status, NEW.status, 'SYSTEM',
            json_build_object(
                'old_status', OLD.status,
                'new_status', NEW.status,
                'timestamp', CURRENT_TIMESTAMP
            )
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_sample_status_change
    AFTER UPDATE ON samples
    FOR EACH ROW
    EXECUTE FUNCTION log_sample_status_change();

-- Create sample quality control table
CREATE TABLE sample_quality_control (
    qc_id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT NOT NULL REFERENCES samples(sample_id),
    check_type VARCHAR(100) NOT NULL,
    check_result VARCHAR(50) NOT NULL,
    check_value VARCHAR(200),
    reference_range VARCHAR(200),
    checked_by VARCHAR(100) NOT NULL,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comments TEXT,
    
    CONSTRAINT chk_qc_result CHECK (check_result IN ('PASS', 'FAIL', 'WARNING', 'NOT_APPLICABLE')),
    CONSTRAINT chk_qc_type CHECK (check_type IN (
        'VOLUME_CHECK', 'TEMPERATURE_CHECK', 'VISUAL_INSPECTION',
        'CONTAINER_CHECK', 'LABELING_CHECK', 'INTEGRITY_CHECK',
        'HEMOLYSIS_CHECK', 'CLOTTING_CHECK', 'CONTAMINATION_CHECK'
    ))
);

-- Create indexes for quality control
CREATE INDEX idx_sample_qc_sample_id ON sample_quality_control(sample_id);
CREATE INDEX idx_sample_qc_check_type ON sample_quality_control(check_type);
CREATE INDEX idx_sample_qc_result ON sample_quality_control(check_result);
CREATE INDEX idx_sample_qc_checked_by ON sample_quality_control(checked_by);
CREATE INDEX idx_sample_qc_checked_at ON sample_quality_control(checked_at);

-- Create sample disposal tracking table
CREATE TABLE sample_disposal_batches (
    batch_id BIGSERIAL PRIMARY KEY,
    batch_number VARCHAR(50) UNIQUE NOT NULL,
    disposal_method VARCHAR(200) NOT NULL,
    disposal_date TIMESTAMP NOT NULL,
    disposed_by VARCHAR(100) NOT NULL,
    disposal_location VARCHAR(200),
    disposal_certificate VARCHAR(500),
    sample_count INTEGER NOT NULL DEFAULT 0,
    total_volume DECIMAL(10,2),
    disposal_cost DECIMAL(10,2),
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_disposal_method CHECK (disposal_method IN (
        'AUTOCLAVE', 'INCINERATION', 'CHEMICAL_TREATMENT',
        'BIOHAZARD_PICKUP', 'LANDFILL', 'RECYCLING'
    )),
    CONSTRAINT chk_sample_count_positive CHECK (sample_count >= 0)
);

-- Create indexes for disposal batches
CREATE INDEX idx_disposal_batch_number ON sample_disposal_batches(batch_number);
CREATE INDEX idx_disposal_method ON sample_disposal_batches(disposal_method);
CREATE INDEX idx_disposal_date ON sample_disposal_batches(disposal_date);
CREATE INDEX idx_disposal_disposed_by ON sample_disposal_batches(disposed_by);

-- Insert sample data for testing NABL compliance
INSERT INTO sample_disposal_batches (batch_number, disposal_method, disposal_date, disposed_by, disposal_location)
VALUES 
    ('BATCH-2025-001', 'AUTOCLAVE', CURRENT_TIMESTAMP, 'Lab Manager', 'Autoclave Room A'),
    ('BATCH-2025-002', 'INCINERATION', CURRENT_TIMESTAMP, 'Waste Management', 'External Facility');

-- Create view for NABL compliance dashboard
CREATE VIEW nabl_sample_compliance_view AS
SELECT 
    s.sample_id,
    s.sample_number,
    s.sample_type,
    s.status,
    s.collected_at,
    s.received_at,
    s.processing_started_at,
    s.processing_completed_at,
    s.disposed_at,
    v.visit_id,
    EXTRACT(EPOCH FROM (s.received_at - s.collected_at))/3600 as collection_to_receipt_hours,
    EXTRACT(EPOCH FROM (s.processing_completed_at - s.processing_started_at))/3600 as processing_duration_hours,
    CASE 
        WHEN s.rejected = true THEN 'REJECTED'
        WHEN s.status = 'DISPOSED' THEN 'COMPLETE'
        WHEN s.status IN ('PROCESSING', 'IN_ANALYSIS', 'UNDER_REVIEW') THEN 'IN_PROGRESS'
        ELSE 'PENDING'
    END as compliance_status,
    CASE 
        WHEN s.chain_of_custody IS NOT NULL THEN 'DOCUMENTED'
        ELSE 'MISSING'
    END as chain_of_custody_status
FROM samples s
JOIN visits v ON s.visit_id = v.visit_id;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON samples TO lab_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON sample_audit_log TO lab_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON sample_quality_control TO lab_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON sample_disposal_batches TO lab_user;
GRANT SELECT ON nabl_sample_compliance_view TO lab_user;
GRANT USAGE ON SEQUENCE samples_sample_id_seq TO lab_user;
GRANT USAGE ON SEQUENCE sample_audit_log_audit_id_seq TO lab_user;
GRANT USAGE ON SEQUENCE sample_quality_control_qc_id_seq TO lab_user;
GRANT USAGE ON SEQUENCE sample_disposal_batches_batch_id_seq TO lab_user;
