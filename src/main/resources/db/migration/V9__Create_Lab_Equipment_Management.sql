-- Create Lab Equipment Management Tables
-- Migration V9: Lab Equipment, Maintenance, and Calibration

-- Create lab_equipment table
CREATE TABLE lab_equipment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    serial_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    equipment_type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    purchase_date TIMESTAMP,
    warranty_expiry TIMESTAMP,
    last_maintenance TIMESTAMP,
    next_maintenance TIMESTAMP,
    calibration_due TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create equipment_maintenance table
CREATE TABLE equipment_maintenance (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    performed_by VARCHAR(255),
    vendor VARCHAR(255),
    cost DECIMAL(10,2),
    parts_replaced TEXT,
    maintenance_date TIMESTAMP NOT NULL,
    next_maintenance_due TIMESTAMP,
    downtime_hours INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id) REFERENCES lab_equipment(id) ON DELETE CASCADE
);

-- Create equipment_calibration table
CREATE TABLE equipment_calibration (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    calibration_date TIMESTAMP NOT NULL,
    next_calibration_due TIMESTAMP,
    performed_by VARCHAR(255),
    calibration_standard VARCHAR(255),
    reference_material VARCHAR(255),
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    calibration_result VARCHAR(50) NOT NULL,
    accuracy_achieved DECIMAL(10,6),
    tolerance_limit DECIMAL(10,6),
    certificate_number VARCHAR(255),
    calibration_agency VARCHAR(255),
    cost DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id) REFERENCES lab_equipment(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_lab_equipment_serial_number ON lab_equipment(serial_number);
CREATE INDEX idx_lab_equipment_status ON lab_equipment(status);
CREATE INDEX idx_lab_equipment_type ON lab_equipment(equipment_type);
CREATE INDEX idx_lab_equipment_manufacturer ON lab_equipment(manufacturer);
CREATE INDEX idx_lab_equipment_location ON lab_equipment(location);
CREATE INDEX idx_lab_equipment_next_maintenance ON lab_equipment(next_maintenance);
CREATE INDEX idx_lab_equipment_calibration_due ON lab_equipment(calibration_due);
CREATE INDEX idx_lab_equipment_warranty_expiry ON lab_equipment(warranty_expiry);

CREATE INDEX idx_equipment_maintenance_equipment_id ON equipment_maintenance(equipment_id);
CREATE INDEX idx_equipment_maintenance_date ON equipment_maintenance(maintenance_date);
CREATE INDEX idx_equipment_maintenance_type ON equipment_maintenance(maintenance_type);
CREATE INDEX idx_equipment_maintenance_next_due ON equipment_maintenance(next_maintenance_due);

CREATE INDEX idx_equipment_calibration_equipment_id ON equipment_calibration(equipment_id);
CREATE INDEX idx_equipment_calibration_date ON equipment_calibration(calibration_date);
CREATE INDEX idx_equipment_calibration_next_due ON equipment_calibration(next_calibration_due);
CREATE INDEX idx_equipment_calibration_result ON equipment_calibration(calibration_result);

-- Add constraints
ALTER TABLE lab_equipment ADD CONSTRAINT chk_equipment_status 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'CALIBRATION', 'OUT_OF_ORDER', 'RETIRED', 'RESERVED'));

ALTER TABLE lab_equipment ADD CONSTRAINT chk_equipment_type 
    CHECK (equipment_type IN ('SPECTROPHOTOMETER', 'MICROSCOPE', 'CENTRIFUGE', 'ANALYZER', 'CHROMATOGRAPH', 
                              'ELECTROPHORESIS', 'PIPETTE', 'DISPENSER', 'MIXER', 'HOMOGENIZER', 'SONICATOR',
                              'INCUBATOR', 'REFRIGERATOR', 'FREEZER', 'WATER_BATH', 'DRY_BATH',
                              'BIOSAFETY_CABINET', 'FUME_HOOD', 'AUTOCLAVE', 'STERILIZER',
                              'BALANCE', 'SCALE', 'PH_METER', 'THERMOMETER',
                              'PRINTER', 'COMPUTER', 'BARCODE_SCANNER', 'LABEL_PRINTER', 'OTHER'));

ALTER TABLE equipment_maintenance ADD CONSTRAINT chk_maintenance_type 
    CHECK (maintenance_type IN ('PREVENTIVE', 'CORRECTIVE', 'EMERGENCY', 'UPGRADE', 'INSPECTION', 'CLEANING', 'PARTS_REPLACEMENT'));

ALTER TABLE equipment_calibration ADD CONSTRAINT chk_calibration_result 
    CHECK (calibration_result IN ('PASSED', 'FAILED', 'ADJUSTED', 'LIMITED_USE', 'OUT_OF_TOLERANCE'));

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_lab_equipment_updated_at BEFORE UPDATE ON lab_equipment
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing
INSERT INTO lab_equipment (name, model, manufacturer, serial_number, equipment_type, location, status) VALUES
('Automated Chemistry Analyzer', 'AU5800', 'Beckman Coulter', 'BC-AU5800-001', 'ANALYZER', 'Chemistry Lab', 'ACTIVE'),
('Hematology Analyzer', 'DxH 900', 'Beckman Coulter', 'BC-DXH900-001', 'ANALYZER', 'Hematology Lab', 'ACTIVE'),
('Microscope', 'BX53', 'Olympus', 'OLY-BX53-001', 'MICROSCOPE', 'Microscopy Lab', 'ACTIVE'),
('Centrifuge', 'Allegra X-30R', 'Beckman Coulter', 'BC-X30R-001', 'CENTRIFUGE', 'Sample Prep', 'ACTIVE'),
('Incubator', 'Heratherm IMC18', 'Thermo Fisher', 'TF-IMC18-001', 'INCUBATOR', 'Microbiology Lab', 'ACTIVE'),
('Autoclave', 'SX-700', 'Tuttnauer', 'TUT-SX700-001', 'AUTOCLAVE', 'Sterilization Room', 'ACTIVE'),
('Balance', 'XS205', 'Mettler Toledo', 'MT-XS205-001', 'BALANCE', 'Weighing Room', 'ACTIVE'),
('Refrigerator', 'TSX2305PA', 'Thermo Fisher', 'TF-TSX2305-001', 'REFRIGERATOR', 'Sample Storage', 'ACTIVE');

-- Insert sample maintenance records
INSERT INTO equipment_maintenance (equipment_id, maintenance_type, description, performed_by, maintenance_date, next_maintenance_due) VALUES
(1, 'PREVENTIVE', 'Quarterly preventive maintenance - cleaned optical components, calibrated pipettes', 'John Smith', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '60 days'),
(2, 'CORRECTIVE', 'Replaced faulty pump assembly', 'Jane Doe', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '90 days'),
(3, 'CLEANING', 'Deep cleaning of microscope lenses and stage', 'Mike Johnson', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP + INTERVAL '30 days');

-- Insert sample calibration records
INSERT INTO equipment_calibration (equipment_id, calibration_date, next_calibration_due, performed_by, calibration_result, certificate_number) VALUES
(1, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP + INTERVAL '305 days', 'Calibration Services Inc', 'PASSED', 'CAL-2024-001'),
(2, CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP + INTERVAL '320 days', 'Beckman Service', 'PASSED', 'CAL-2024-002'),
(7, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP + INTERVAL '275 days', 'Mettler Toledo Service', 'PASSED', 'CAL-2024-003');
