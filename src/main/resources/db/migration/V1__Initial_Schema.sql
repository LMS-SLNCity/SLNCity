-- Lab Operations Management System - Initial Schema
-- Version: 1.0
-- Description: Core tables for lab operations, equipment, inventory, and workflow management

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create custom types
CREATE TYPE visit_status AS ENUM ('PENDING', 'IN_PROGRESS', 'AWAITING_APPROVAL', 'APPROVED', 'BILLED', 'COMPLETED');
CREATE TYPE test_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'APPROVED');
CREATE TYPE sample_status AS ENUM ('COLLECTED', 'IN_TRANSIT', 'RECEIVED', 'ACCESSIONED', 'ACCEPTED', 'REJECTED', 'PROCESSING', 'ALIQUOTED', 'IN_ANALYSIS', 'ANALYSIS_COMPLETE', 'UNDER_REVIEW', 'REVIEWED', 'STORED', 'DISPOSED', 'ON_HOLD', 'RECALLED');
CREATE TYPE sample_type AS ENUM ('WHOLE_BLOOD', 'SERUM', 'PLASMA', 'RANDOM_URINE', 'FIRST_MORNING_URINE', 'MIDSTREAM_URINE', 'TWENTY_FOUR_HOUR_URINE', 'CEREBROSPINAL_FLUID', 'SYNOVIAL_FLUID', 'PLEURAL_FLUID', 'ASCITIC_FLUID', 'THROAT_SWAB', 'NASAL_SWAB', 'WOUND_SWAB', 'VAGINAL_SWAB', 'STOOL', 'SPUTUM', 'TISSUE_BIOPSY', 'SALIVA', 'HAIR', 'NAIL');
CREATE TYPE report_status AS ENUM ('DRAFT', 'GENERATED', 'AUTHORIZED', 'SENT');
CREATE TYPE report_type AS ENUM ('STANDARD', 'AMENDED', 'SUPPLEMENTARY');
CREATE TYPE equipment_status AS ENUM ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'CALIBRATION', 'OUT_OF_ORDER', 'RETIRED', 'RESERVED');
CREATE TYPE equipment_type AS ENUM ('SPECTROPHOTOMETER', 'MICROSCOPE', 'CENTRIFUGE', 'ANALYZER', 'CHROMATOGRAPH', 'ELECTROPHORESIS', 'PIPETTE', 'DISPENSER', 'MIXER', 'HOMOGENIZER', 'SONICATOR', 'INCUBATOR', 'REFRIGERATOR', 'FREEZER', 'WATER_BATH', 'DRY_BATH', 'BIOSAFETY_CABINET', 'FUME_HOOD', 'AUTOCLAVE', 'STERILIZER', 'BALANCE', 'SCALE', 'PH_METER', 'THERMOMETER', 'PRINTER', 'COMPUTER', 'BARCODE_SCANNER', 'LABEL_PRINTER', 'OTHER');
CREATE TYPE inventory_status AS ENUM ('ACTIVE', 'INACTIVE', 'EXPIRED', 'RECALLED', 'QUARANTINED', 'DISCONTINUED', 'DAMAGED');
CREATE TYPE inventory_category AS ENUM ('REAGENTS', 'BUFFERS', 'STAINS', 'STANDARDS', 'CALIBRATORS', 'TUBES', 'PIPETTE_TIPS', 'PLATES', 'SLIDES', 'FILTERS', 'SYRINGES', 'COLLECTION_TUBES', 'SWABS', 'CONTAINERS', 'TRANSPORT_MEDIA', 'GLOVES', 'MASKS', 'GOWNS', 'EYEWEAR', 'CLEANING_SUPPLIES', 'MAINTENANCE_PARTS', 'LABELS', 'FORMS', 'STATIONERY', 'QC_MATERIALS', 'PROFICIENCY_TESTING', 'OTHER');
CREATE TYPE transaction_type AS ENUM ('STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT', 'TRANSFER', 'RETURN', 'DISPOSAL', 'CONSUMPTION', 'LOSS');
CREATE TYPE maintenance_type AS ENUM ('PREVENTIVE', 'CORRECTIVE', 'EMERGENCY', 'UPGRADE', 'INSPECTION', 'CLEANING', 'PARTS_REPLACEMENT');
CREATE TYPE calibration_result AS ENUM ('PASSED', 'FAILED', 'ADJUSTED', 'LIMITED_USE', 'OUT_OF_TOLERANCE');

-- Core Tables

-- Visits table
CREATE TABLE visits (
    visit_id BIGSERIAL PRIMARY KEY,
    patient_details JSONB NOT NULL,
    status visit_status DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Test Templates table
CREATE TABLE test_templates (
    template_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parameters JSONB NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Lab Tests table
CREATE TABLE lab_tests (
    test_id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL REFERENCES visits(visit_id) ON DELETE CASCADE,
    test_template_id BIGINT NOT NULL REFERENCES test_templates(template_id),
    status test_status DEFAULT 'PENDING',
    results JSONB,
    price DECIMAL(10,2) NOT NULL,
    approved BOOLEAN DEFAULT FALSE,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP WITH TIME ZONE,
    results_entered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Samples table
CREATE TABLE samples (
    sample_id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL REFERENCES visits(visit_id) ON DELETE CASCADE,
    sample_number VARCHAR(255) NOT NULL UNIQUE,
    sample_type sample_type NOT NULL,
    status sample_status NOT NULL DEFAULT 'COLLECTED',
    collected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    collected_by VARCHAR(255) NOT NULL,
    collection_site VARCHAR(255),
    container_type VARCHAR(255),
    volume_required DOUBLE PRECISION,
    volume_received DOUBLE PRECISION,
    receipt_temperature DOUBLE PRECISION,
    storage_temperature DOUBLE PRECISION,
    storage_location VARCHAR(255),
    storage_conditions VARCHAR(255),
    preservative VARCHAR(255),
    received_at TIMESTAMP WITH TIME ZONE,
    received_by VARCHAR(255),
    receipt_condition VARCHAR(255),
    rejected BOOLEAN DEFAULT FALSE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    rejected_by VARCHAR(255),
    rejection_reason TEXT,
    processing_started_at TIMESTAMP WITH TIME ZONE,
    processing_completed_at TIMESTAMP WITH TIME ZONE,
    processed_by VARCHAR(255),
    disposed_at TIMESTAMP WITH TIME ZONE,
    disposed_by VARCHAR(255),
    disposal_method VARCHAR(255),
    disposal_batch VARCHAR(255),
    comments TEXT,
    chain_of_custody JSONB,
    collection_conditions JSONB,
    quality_indicators JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ULR Sequence Configuration table
CREATE TABLE ulr_sequence_config (
    config_id BIGSERIAL PRIMARY KEY,
    prefix VARCHAR(10) NOT NULL DEFAULT 'SLN',
    format_pattern VARCHAR(50) NOT NULL DEFAULT '{prefix}/{year}/{sequence:06d}',
    report_year INTEGER NOT NULL,
    sequence_number INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Lab Reports table
CREATE TABLE lab_reports (
    report_id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL REFERENCES visits(visit_id) ON DELETE CASCADE,
    ulr_number VARCHAR(50) NOT NULL UNIQUE,
    report_type report_type NOT NULL DEFAULT 'STANDARD',
    report_status report_status NOT NULL DEFAULT 'DRAFT',
    report_data JSONB,
    template_version VARCHAR(20),
    generated_at TIMESTAMP WITH TIME ZONE,
    authorized_at TIMESTAMP WITH TIME ZONE,
    authorized_by VARCHAR(255),
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Billing table
CREATE TABLE billing (
    bill_id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL UNIQUE REFERENCES visits(visit_id) ON DELETE CASCADE,
    total_amount DECIMAL(10,2) NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    payment_date TIMESTAMP WITH TIME ZONE,
    payment_method VARCHAR(100),
    payment_reference VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Lab Equipment table
CREATE TABLE lab_equipment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    equipment_type equipment_type NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    serial_number VARCHAR(255) NOT NULL UNIQUE,
    status equipment_status NOT NULL DEFAULT 'ACTIVE',
    location VARCHAR(255),
    purchase_date DATE,
    warranty_expiry DATE,
    last_maintenance DATE,
    next_maintenance DATE,
    calibration_due DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Equipment Maintenance table
CREATE TABLE equipment_maintenance (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES lab_equipment(id) ON DELETE CASCADE,
    maintenance_type maintenance_type NOT NULL,
    maintenance_date DATE NOT NULL,
    performed_by VARCHAR(255),
    vendor VARCHAR(255),
    description TEXT NOT NULL,
    parts_replaced TEXT,
    cost DECIMAL(10,2),
    downtime_hours INTEGER,
    next_maintenance_due DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Equipment Calibration table
CREATE TABLE equipment_calibration (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES lab_equipment(id) ON DELETE CASCADE,
    calibration_date DATE NOT NULL,
    performed_by VARCHAR(255),
    calibration_agency VARCHAR(255),
    certificate_number VARCHAR(255),
    calibration_standard VARCHAR(255),
    reference_material VARCHAR(255),
    calibration_result calibration_result NOT NULL,
    accuracy_achieved DECIMAL(10,2),
    tolerance_limit DECIMAL(10,2),
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    cost DECIMAL(10,2),
    next_calibration_due DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Inventory Items table
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL UNIQUE,
    category inventory_category NOT NULL,
    description TEXT,
    unit_of_measurement VARCHAR(100) NOT NULL,
    current_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER NOT NULL,
    maximum_stock_level INTEGER NOT NULL,
    reorder_point INTEGER,
    unit_cost DECIMAL(10,2),
    supplier VARCHAR(255),
    supplier_catalog_number VARCHAR(255),
    lot_number VARCHAR(255),
    expiry_date DATE,
    storage_location VARCHAR(255),
    storage_conditions VARCHAR(255),
    barcode VARCHAR(255),
    status inventory_status NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Inventory Transactions table
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    transaction_type transaction_type NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    stock_before INTEGER,
    stock_after INTEGER,
    transaction_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(255),
    reason VARCHAR(255),
    reference_number VARCHAR(255),
    supplier VARCHAR(255),
    lot_number VARCHAR(255),
    expiry_date DATE,
    notes TEXT
);

-- Create indexes for performance
CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_created_at ON visits(created_at);
CREATE INDEX idx_lab_tests_visit_id ON lab_tests(visit_id);
CREATE INDEX idx_lab_tests_status ON lab_tests(status);
CREATE INDEX idx_lab_tests_approved ON lab_tests(approved);
CREATE INDEX idx_samples_visit_id ON samples(visit_id);
CREATE INDEX idx_samples_status ON samples(status);
CREATE INDEX idx_samples_sample_number ON samples(sample_number);
CREATE INDEX idx_lab_reports_visit_id ON lab_reports(visit_id);
CREATE INDEX idx_lab_reports_ulr_number ON lab_reports(ulr_number);
CREATE INDEX idx_lab_reports_status ON lab_reports(report_status);
CREATE INDEX idx_billing_visit_id ON billing(visit_id);
CREATE INDEX idx_billing_paid ON billing(paid);
CREATE INDEX idx_equipment_status ON lab_equipment(status);
CREATE INDEX idx_equipment_type ON lab_equipment(equipment_type);
CREATE INDEX idx_equipment_serial ON lab_equipment(serial_number);
CREATE INDEX idx_inventory_sku ON inventory_items(sku);
CREATE INDEX idx_inventory_category ON inventory_items(category);
CREATE INDEX idx_inventory_status ON inventory_items(status);
CREATE INDEX idx_inventory_stock_level ON inventory_items(current_stock, minimum_stock_level);
CREATE INDEX idx_inventory_expiry ON inventory_items(expiry_date);
CREATE INDEX idx_transactions_item_id ON inventory_transactions(inventory_item_id);
CREATE INDEX idx_transactions_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_transactions_date ON inventory_transactions(transaction_date);

-- Insert initial ULR sequence configuration
INSERT INTO ulr_sequence_config (prefix, format_pattern, report_year, sequence_number, is_active)
VALUES ('SLN', '{prefix}/{year}/{sequence:06d}', EXTRACT(YEAR FROM CURRENT_DATE), 0, TRUE);

-- Create trigger function for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_visits_updated_at BEFORE UPDATE ON visits FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_test_templates_updated_at BEFORE UPDATE ON test_templates FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_lab_tests_updated_at BEFORE UPDATE ON lab_tests FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_samples_updated_at BEFORE UPDATE ON samples FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ulr_sequence_config_updated_at BEFORE UPDATE ON ulr_sequence_config FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_lab_reports_updated_at BEFORE UPDATE ON lab_reports FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_billing_updated_at BEFORE UPDATE ON billing FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_lab_equipment_updated_at BEFORE UPDATE ON lab_equipment FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_inventory_items_updated_at BEFORE UPDATE ON inventory_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Network Connection and Machine ID Issue Enums (Optional - only created if network monitoring is enabled)
-- These enums and tables are optional and can be created separately when network features are enabled
CREATE TYPE connection_status AS ENUM (
    'CONNECTED', 'DISCONNECTED', 'CONNECTING', 'RECONNECTING', 'FAILED',
    'TIMEOUT', 'AUTHENTICATION_FAILED', 'WEAK_SIGNAL', 'LIMITED_CONNECTIVITY',
    'NO_INTERNET', 'MAINTENANCE'
);

CREATE TYPE connection_type AS ENUM (
    'WLAN', 'ETHERNET', 'BLUETOOTH', 'USB', 'SERIAL', 'CELLULAR',
    'SATELLITE', 'VPN', 'HOTSPOT', 'MESH'
);

CREATE TYPE issue_type AS ENUM (
    'MACHINE_ID_MISMATCH', 'MACHINE_ID_DUPLICATE', 'MACHINE_ID_MISSING',
    'MACHINE_ID_INVALID_FORMAT', 'MAC_ADDRESS_CONFLICT', 'MAC_ADDRESS_SPOOFING',
    'IP_ADDRESS_CONFLICT', 'DHCP_LEASE_EXPIRED', 'DNS_RESOLUTION_FAILED',
    'NETWORK_AUTHENTICATION_FAILED', 'CERTIFICATE_EXPIRED', 'CERTIFICATE_INVALID',
    'FIRMWARE_OUTDATED', 'DRIVER_INCOMPATIBLE', 'HARDWARE_FAILURE',
    'SIGNAL_INTERFERENCE', 'BANDWIDTH_LIMITATION', 'CONNECTION_TIMEOUT',
    'PACKET_LOSS_HIGH', 'LATENCY_HIGH', 'SECURITY_BREACH_DETECTED',
    'UNAUTHORIZED_ACCESS_ATTEMPT', 'CONFIGURATION_ERROR', 'POWER_MANAGEMENT_ISSUE',
    'ADAPTER_NOT_RECOGNIZED', 'PROFILE_CORRUPTION', 'REGISTRY_CORRUPTION',
    'SERVICE_UNAVAILABLE', 'PROTOCOL_ERROR', 'FIREWALL_BLOCKING',
    'PROXY_CONFIGURATION_ERROR', 'VPN_CONNECTION_FAILED', 'MESH_NETWORK_ISSUE',
    'ROAMING_FAILURE', 'QOS_VIOLATION', 'COMPLIANCE_VIOLATION',
    'MONITORING_FAILURE', 'BACKUP_CONNECTION_FAILED', 'LOAD_BALANCING_ISSUE',
    'REDUNDANCY_FAILURE', 'OTHER'
);

CREATE TYPE issue_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFORMATIONAL');

CREATE TYPE issue_status AS ENUM (
    'OPEN', 'IN_PROGRESS', 'PENDING_APPROVAL', 'WAITING_FOR_PARTS',
    'WAITING_FOR_VENDOR', 'ESCALATED', 'RESOLVED', 'CLOSED', 'CANCELLED',
    'DUPLICATE', 'DEFERRED'
);

-- Network Connection Table
CREATE TABLE network_connections (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES lab_equipment(id) ON DELETE CASCADE,
    machine_id VARCHAR(255) NOT NULL UNIQUE,
    mac_address VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    ssid VARCHAR(255),
    connection_status connection_status NOT NULL DEFAULT 'DISCONNECTED',
    connection_type connection_type NOT NULL DEFAULT 'WLAN',
    signal_strength INTEGER,
    bandwidth_mbps DECIMAL(10,2),
    last_connected TIMESTAMP,
    last_disconnected TIMESTAMP,
    connection_uptime_hours DECIMAL(10,2),
    total_data_transferred_mb DECIMAL(15,2),
    firmware_version VARCHAR(255),
    driver_version VARCHAR(255),
    network_adapter_model VARCHAR(255),
    dns_servers VARCHAR(500),
    gateway_address VARCHAR(255),
    subnet_mask VARCHAR(255),
    dhcp_enabled BOOLEAN DEFAULT TRUE,
    security_protocol VARCHAR(255),
    encryption_type VARCHAR(255),
    last_ping_response_ms INTEGER,
    packet_loss_percentage DECIMAL(5,2),
    connection_errors_count INTEGER DEFAULT 0,
    auto_reconnect_enabled BOOLEAN DEFAULT TRUE,
    priority_level INTEGER DEFAULT 1,
    notes TEXT,
    network_diagnostics JSON,
    connection_history JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Machine ID Issues Table
CREATE TABLE machine_id_issues (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES lab_equipment(id) ON DELETE CASCADE,
    network_connection_id BIGINT REFERENCES network_connections(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    issue_type issue_type NOT NULL,
    severity issue_severity NOT NULL DEFAULT 'MEDIUM',
    status issue_status NOT NULL DEFAULT 'OPEN',
    machine_id_current VARCHAR(255),
    machine_id_expected VARCHAR(255),
    mac_address_current VARCHAR(255),
    mac_address_expected VARCHAR(255),
    ip_address_current VARCHAR(255),
    ip_address_expected VARCHAR(255),
    error_code VARCHAR(255),
    error_message TEXT,
    first_detected TIMESTAMP,
    last_occurrence TIMESTAMP,
    occurrence_count INTEGER DEFAULT 1,
    reported_by VARCHAR(255),
    assigned_to VARCHAR(255),
    resolved_by VARCHAR(255),
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    estimated_resolution_time_hours DECIMAL(10,2),
    actual_resolution_time_hours DECIMAL(10,2),
    impact_assessment TEXT,
    workaround_applied VARCHAR(500),
    root_cause_analysis TEXT,
    prevention_measures TEXT,
    escalated BOOLEAN DEFAULT FALSE,
    escalated_to VARCHAR(255),
    escalated_at TIMESTAMP,
    priority_level INTEGER DEFAULT 3,
    auto_detected BOOLEAN DEFAULT FALSE,
    requires_physical_access BOOLEAN DEFAULT FALSE,
    requires_network_restart BOOLEAN DEFAULT FALSE,
    requires_equipment_restart BOOLEAN DEFAULT FALSE,
    diagnostic_data JSON,
    resolution_steps JSON,
    related_logs JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create triggers for updated_at columns
CREATE TRIGGER update_network_connections_updated_at BEFORE UPDATE ON network_connections FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_machine_id_issues_updated_at BEFORE UPDATE ON machine_id_issues FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for performance
CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_created_at ON visits(created_at);
CREATE INDEX idx_lab_tests_status ON lab_tests(status);
CREATE INDEX idx_lab_tests_visit_id ON lab_tests(visit_id);
CREATE INDEX idx_samples_status ON samples(status);
CREATE INDEX idx_samples_visit_id ON samples(visit_id);
CREATE INDEX idx_billing_visit_id ON billing(visit_id);
CREATE INDEX idx_lab_reports_visit_id ON lab_reports(visit_id);
CREATE INDEX idx_lab_equipment_status ON lab_equipment(status);
CREATE INDEX idx_lab_equipment_type ON lab_equipment(equipment_type);
CREATE INDEX idx_inventory_items_status ON inventory_items(status);
CREATE INDEX idx_inventory_items_category ON inventory_items(category);

-- Network connection indexes
CREATE INDEX idx_network_connections_machine_id ON network_connections(machine_id);
CREATE INDEX idx_network_connections_mac_address ON network_connections(mac_address);
CREATE INDEX idx_network_connections_ip_address ON network_connections(ip_address);
CREATE INDEX idx_network_connections_status ON network_connections(connection_status);
CREATE INDEX idx_network_connections_type ON network_connections(connection_type);
CREATE INDEX idx_network_connections_equipment_id ON network_connections(equipment_id);
CREATE INDEX idx_network_connections_ssid ON network_connections(ssid);

-- Machine ID issue indexes
CREATE INDEX idx_machine_id_issues_equipment_id ON machine_id_issues(equipment_id);
CREATE INDEX idx_machine_id_issues_network_connection_id ON machine_id_issues(network_connection_id);
CREATE INDEX idx_machine_id_issues_status ON machine_id_issues(status);
CREATE INDEX idx_machine_id_issues_severity ON machine_id_issues(severity);
CREATE INDEX idx_machine_id_issues_type ON machine_id_issues(issue_type);
CREATE INDEX idx_machine_id_issues_machine_id ON machine_id_issues(machine_id_current);
CREATE INDEX idx_machine_id_issues_assigned_to ON machine_id_issues(assigned_to);
CREATE INDEX idx_machine_id_issues_created_at ON machine_id_issues(created_at);
CREATE INDEX idx_machine_id_issues_priority ON machine_id_issues(priority_level);
