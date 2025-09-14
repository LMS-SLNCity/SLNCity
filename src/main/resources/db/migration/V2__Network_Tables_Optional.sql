-- Optional Network Connection and Machine ID Management Tables
-- This migration is only applied when network monitoring features are enabled
-- The system can operate without these tables in basic mode

-- Check if network monitoring is enabled before creating tables
-- This is handled by the application configuration

-- Network Connection Table (Optional)
CREATE TABLE IF NOT EXISTS network_connections (
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

-- Machine ID Issues Table (Optional)
CREATE TABLE IF NOT EXISTS machine_id_issues (
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

-- Create triggers for updated_at columns (only if tables exist)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'network_connections') THEN
        CREATE TRIGGER update_network_connections_updated_at 
        BEFORE UPDATE ON network_connections 
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'machine_id_issues') THEN
        CREATE TRIGGER update_machine_id_issues_updated_at 
        BEFORE UPDATE ON machine_id_issues 
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

-- Create indexes for performance (only if tables exist)
DO $$
BEGIN
    -- Network connection indexes
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'network_connections') THEN
        CREATE INDEX IF NOT EXISTS idx_network_connections_machine_id ON network_connections(machine_id);
        CREATE INDEX IF NOT EXISTS idx_network_connections_mac_address ON network_connections(mac_address);
        CREATE INDEX IF NOT EXISTS idx_network_connections_ip_address ON network_connections(ip_address);
        CREATE INDEX IF NOT EXISTS idx_network_connections_status ON network_connections(connection_status);
        CREATE INDEX IF NOT EXISTS idx_network_connections_type ON network_connections(connection_type);
        CREATE INDEX IF NOT EXISTS idx_network_connections_equipment_id ON network_connections(equipment_id);
        CREATE INDEX IF NOT EXISTS idx_network_connections_ssid ON network_connections(ssid);
    END IF;
    
    -- Machine ID issue indexes
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'machine_id_issues') THEN
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_equipment_id ON machine_id_issues(equipment_id);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_network_connection_id ON machine_id_issues(network_connection_id);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_status ON machine_id_issues(status);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_severity ON machine_id_issues(severity);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_type ON machine_id_issues(issue_type);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_machine_id ON machine_id_issues(machine_id_current);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_assigned_to ON machine_id_issues(assigned_to);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_created_at ON machine_id_issues(created_at);
        CREATE INDEX IF NOT EXISTS idx_machine_id_issues_priority ON machine_id_issues(priority_level);
    END IF;
END $$;
