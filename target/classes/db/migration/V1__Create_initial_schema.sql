-- Lab Operations Database Schema
-- Version 1: Initial schema with visit-only model

-- Create visits table
CREATE TABLE visits (
    visit_id SERIAL PRIMARY KEY,
    patient_details JSONB NOT NULL, -- stores name, age, gender, phone, address inline
    created_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'pending' -- pending, in-progress, awaiting-approval, approved, billed, completed
);

-- Create test_templates table
CREATE TABLE test_templates (
    template_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parameters JSONB NOT NULL, -- defines dynamic fields, reference ranges, types
    base_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create lab_tests table
CREATE TABLE lab_tests (
    test_id SERIAL PRIMARY KEY,
    visit_id INT NOT NULL REFERENCES visits(visit_id),
    test_template_id INT NOT NULL REFERENCES test_templates(template_id),
    status VARCHAR(50) DEFAULT 'pending',
    price DECIMAL(10,2) NOT NULL,
    results JSONB,
    approved BOOLEAN DEFAULT FALSE,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP
);

-- Create billing table
CREATE TABLE billing (
    bill_id SERIAL PRIMARY KEY,
    visit_id INT NOT NULL REFERENCES visits(visit_id),
    total_amount DECIMAL(10,2) NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_patient_phone ON visits USING GIN ((patient_details->>'phone'));
CREATE INDEX idx_lab_tests_visit_id ON lab_tests(visit_id);
CREATE INDEX idx_lab_tests_status ON lab_tests(status);
CREATE INDEX idx_lab_tests_approved ON lab_tests(approved);
CREATE INDEX idx_billing_visit_id ON billing(visit_id);
CREATE INDEX idx_billing_paid ON billing(paid);

-- Add constraints
ALTER TABLE visits ADD CONSTRAINT chk_visits_status 
    CHECK (status IN ('pending', 'in-progress', 'awaiting-approval', 'approved', 'billed', 'completed'));

ALTER TABLE lab_tests ADD CONSTRAINT chk_lab_tests_status 
    CHECK (status IN ('pending', 'in-progress', 'completed', 'approved'));

-- Add comments for documentation
COMMENT ON TABLE visits IS 'Patient visits with inline patient details stored as JSONB';
COMMENT ON COLUMN visits.patient_details IS 'JSON object containing name, age, gender, phone, address';
COMMENT ON TABLE test_templates IS 'Templates defining test parameters and pricing';
COMMENT ON COLUMN test_templates.parameters IS 'JSON object defining test fields, reference ranges, and types';
COMMENT ON TABLE lab_tests IS 'Individual tests performed during visits';
COMMENT ON COLUMN lab_tests.results IS 'JSON object containing test results';
COMMENT ON TABLE billing IS 'Billing information for visits';
