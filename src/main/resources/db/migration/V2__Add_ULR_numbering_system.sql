-- NABL 112 Compliance: Unique Laboratory Report (ULR) Numbering System
-- Version 2: Add ULR numbering and report management

-- Create ULR sequence configuration table
CREATE TABLE ulr_sequence_config (
    config_id SERIAL PRIMARY KEY,
    report_year INTEGER NOT NULL,
    sequence_number INTEGER NOT NULL DEFAULT 1,
    prefix VARCHAR(10) NOT NULL DEFAULT 'SLN', -- Lab prefix
    format_pattern VARCHAR(50) NOT NULL DEFAULT '{prefix}/{year}/{sequence:06d}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(report_year, is_active) -- Only one active config per year
);

-- Create lab reports table for NABL compliance
CREATE TABLE lab_reports (
    report_id BIGSERIAL PRIMARY KEY,
    ulr_number VARCHAR(50) UNIQUE NOT NULL, -- Unique Laboratory Report Number
    visit_id BIGINT NOT NULL REFERENCES visits(visit_id),
    report_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD', -- STANDARD, AMENDED, SUPPLEMENTARY
    report_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, GENERATED, AUTHORIZED, SENT
    generated_at TIMESTAMP,
    authorized_by VARCHAR(255),
    authorized_at TIMESTAMP,
    sent_at TIMESTAMP,
    report_data JSON, -- Complete report content
    template_version VARCHAR(20),
    nabl_compliant BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create report audit trail table
CREATE TABLE report_audit_trail (
    audit_id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES lab_reports(report_id),
    action VARCHAR(50) NOT NULL, -- CREATED, MODIFIED, AUTHORIZED, SENT, AMENDED
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP DEFAULT NOW(),
    old_values JSONB,
    new_values JSONB,
    comments TEXT
);

-- Insert initial ULR configuration for current year
INSERT INTO ulr_sequence_config (report_year, sequence_number, prefix, format_pattern)
VALUES (EXTRACT(YEAR FROM NOW()), 1, 'SLN', '{prefix}/{year}/{sequence:06d}');

-- Create indexes for performance
CREATE INDEX idx_lab_reports_ulr_number ON lab_reports(ulr_number);
CREATE INDEX idx_lab_reports_visit_id ON lab_reports(visit_id);
CREATE INDEX idx_lab_reports_status ON lab_reports(report_status);
CREATE INDEX idx_lab_reports_generated_at ON lab_reports(generated_at);
CREATE INDEX idx_report_audit_trail_report_id ON report_audit_trail(report_id);
CREATE INDEX idx_report_audit_trail_performed_at ON report_audit_trail(performed_at);

-- Add constraints
ALTER TABLE lab_reports ADD CONSTRAINT chk_lab_reports_type 
    CHECK (report_type IN ('STANDARD', 'AMENDED', 'SUPPLEMENTARY'));

ALTER TABLE lab_reports ADD CONSTRAINT chk_lab_reports_status 
    CHECK (report_status IN ('DRAFT', 'GENERATED', 'AUTHORIZED', 'SENT'));

ALTER TABLE report_audit_trail ADD CONSTRAINT chk_audit_action 
    CHECK (action IN ('CREATED', 'MODIFIED', 'AUTHORIZED', 'SENT', 'AMENDED', 'DELETED'));

-- Add comments for documentation
COMMENT ON TABLE ulr_sequence_config IS 'Configuration for ULR number generation per year';
COMMENT ON COLUMN ulr_sequence_config.format_pattern IS 'Pattern for ULR format: {prefix}/{year}/{sequence:06d}';
COMMENT ON TABLE lab_reports IS 'NABL-compliant laboratory reports with ULR numbers';
COMMENT ON COLUMN lab_reports.ulr_number IS 'Unique Laboratory Report Number as per NABL 112';
COMMENT ON TABLE report_audit_trail IS 'Complete audit trail for all report activities';

-- Function to generate next ULR number
CREATE OR REPLACE FUNCTION generate_ulr_number() RETURNS VARCHAR(50) AS $$
DECLARE
    current_year INTEGER;
    next_sequence INTEGER;
    ulr_prefix VARCHAR(10);
    format_pattern VARCHAR(50);
    ulr_number VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM NOW());
    
    -- Get or create configuration for current year
    SELECT prefix, format_pattern INTO ulr_prefix, format_pattern
    FROM ulr_sequence_config
    WHERE report_year = current_year AND is_active = true;

    IF NOT FOUND THEN
        -- Create new configuration for current year
        INSERT INTO ulr_sequence_config (report_year, sequence_number, prefix, format_pattern)
        VALUES (current_year, 1, 'SLN', '{prefix}/{year}/{sequence:06d}')
        RETURNING prefix, format_pattern INTO ulr_prefix, format_pattern;
        next_sequence := 1;
    ELSE
        -- Get and increment sequence number
        UPDATE ulr_sequence_config
        SET sequence_number = sequence_number + 1, updated_at = NOW()
        WHERE report_year = current_year AND is_active = true
        RETURNING sequence_number INTO next_sequence;
    END IF;
    
    -- Generate ULR number using format pattern
    -- Simple format: PREFIX/YEAR/SEQUENCE (e.g., SLN/2025/000001)
    ulr_number := ulr_prefix || '/' || current_year || '/' || LPAD(next_sequence::TEXT, 6, '0');
    
    RETURN ulr_number;
END;
$$ LANGUAGE plpgsql;

-- Trigger function to auto-generate ULR number
CREATE OR REPLACE FUNCTION auto_generate_ulr() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.ulr_number IS NULL OR NEW.ulr_number = '' THEN
        NEW.ulr_number := generate_ulr_number();
    END IF;
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for auto ULR generation
CREATE TRIGGER trigger_auto_generate_ulr
    BEFORE INSERT ON lab_reports
    FOR EACH ROW
    EXECUTE FUNCTION auto_generate_ulr();

-- Trigger function for audit trail
CREATE OR REPLACE FUNCTION audit_report_changes() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO report_audit_trail (report_id, action, performed_by, new_values)
        VALUES (NEW.report_id, 'CREATED', COALESCE(NEW.authorized_by, 'SYSTEM'), row_to_json(NEW));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO report_audit_trail (report_id, action, performed_by, old_values, new_values)
        VALUES (NEW.report_id, 'MODIFIED', COALESCE(NEW.authorized_by, 'SYSTEM'), row_to_json(OLD), row_to_json(NEW));
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO report_audit_trail (report_id, action, performed_by, old_values)
        VALUES (OLD.report_id, 'DELETED', 'SYSTEM', row_to_json(OLD));
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create audit trigger
CREATE TRIGGER trigger_audit_report_changes
    AFTER INSERT OR UPDATE OR DELETE ON lab_reports
    FOR EACH ROW
    EXECUTE FUNCTION audit_report_changes();
