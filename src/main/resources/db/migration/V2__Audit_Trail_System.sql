-- Lab Operations Management System - Audit Trail System
-- Version: 2.0
-- Description: Comprehensive audit logging for all database changes

-- Create audit action enum
CREATE TYPE audit_action AS ENUM ('INSERT', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'ACCESS', 'EXPORT', 'PRINT');

-- Create audit trail table
CREATE TABLE audit_trail (
    audit_id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id BIGINT,
    action audit_action NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_fields TEXT[],
    user_id VARCHAR(255),
    user_name VARCHAR(255),
    user_role VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    severity VARCHAR(20) DEFAULT 'INFO',
    module VARCHAR(100),
    additional_data JSONB
);

-- Create indexes for audit trail
CREATE INDEX idx_audit_trail_table_name ON audit_trail(table_name);
CREATE INDEX idx_audit_trail_record_id ON audit_trail(record_id);
CREATE INDEX idx_audit_trail_action ON audit_trail(action);
CREATE INDEX idx_audit_trail_user_id ON audit_trail(user_id);
CREATE INDEX idx_audit_trail_timestamp ON audit_trail(timestamp);
CREATE INDEX idx_audit_trail_severity ON audit_trail(severity);
CREATE INDEX idx_audit_trail_module ON audit_trail(module);

-- Create user sessions table for tracking active sessions
CREATE TABLE user_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    user_role VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    login_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    session_data JSONB
);

-- Create indexes for user sessions
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_is_active ON user_sessions(is_active);
CREATE INDEX idx_user_sessions_last_activity ON user_sessions(last_activity);

-- Create system notifications table
CREATE TABLE system_notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) DEFAULT 'INFO',
    target_user_id VARCHAR(255),
    target_role VARCHAR(100),
    is_read BOOLEAN DEFAULT FALSE,
    is_system_wide BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB
);

-- Create indexes for notifications
CREATE INDEX idx_notifications_type ON system_notifications(notification_type);
CREATE INDEX idx_notifications_target_user ON system_notifications(target_user_id);
CREATE INDEX idx_notifications_target_role ON system_notifications(target_role);
CREATE INDEX idx_notifications_is_read ON system_notifications(is_read);
CREATE INDEX idx_notifications_is_system_wide ON system_notifications(is_system_wide);
CREATE INDEX idx_notifications_created_at ON system_notifications(created_at);
CREATE INDEX idx_notifications_severity ON system_notifications(severity);

-- Create system alerts table for critical events
CREATE TABLE system_alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    alert_code VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    source_module VARCHAR(100),
    source_table VARCHAR(100),
    source_record_id BIGINT,
    triggered_by VARCHAR(255),
    triggered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(255),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    alert_data JSONB,
    auto_resolve BOOLEAN DEFAULT FALSE,
    escalation_level INTEGER DEFAULT 1
);

-- Create indexes for system alerts
CREATE INDEX idx_alerts_type ON system_alerts(alert_type);
CREATE INDEX idx_alerts_code ON system_alerts(alert_code);
CREATE INDEX idx_alerts_severity ON system_alerts(severity);
CREATE INDEX idx_alerts_status ON system_alerts(status);
CREATE INDEX idx_alerts_triggered_at ON system_alerts(triggered_at);
CREATE INDEX idx_alerts_source_module ON system_alerts(source_module);

-- Create generic audit trigger function
CREATE OR REPLACE FUNCTION audit_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
    old_data JSONB;
    new_data JSONB;
    changed_fields TEXT[] := '{}';
    field_name TEXT;
BEGIN
    -- Handle different operations
    IF TG_OP = 'DELETE' THEN
        old_data := to_jsonb(OLD);
        new_data := NULL;
        
        INSERT INTO audit_trail (
            table_name, record_id, action, old_values, new_values, 
            changed_fields, timestamp, description
        ) VALUES (
            TG_TABLE_NAME, OLD.id, 'DELETE', old_data, new_data, 
            changed_fields, CURRENT_TIMESTAMP, 
            'Record deleted from ' || TG_TABLE_NAME
        );
        
        RETURN OLD;
        
    ELSIF TG_OP = 'INSERT' THEN
        old_data := NULL;
        new_data := to_jsonb(NEW);
        
        INSERT INTO audit_trail (
            table_name, record_id, action, old_values, new_values, 
            changed_fields, timestamp, description
        ) VALUES (
            TG_TABLE_NAME, NEW.id, 'INSERT', old_data, new_data, 
            changed_fields, CURRENT_TIMESTAMP, 
            'New record created in ' || TG_TABLE_NAME
        );
        
        RETURN NEW;
        
    ELSIF TG_OP = 'UPDATE' THEN
        old_data := to_jsonb(OLD);
        new_data := to_jsonb(NEW);
        
        -- Find changed fields
        FOR field_name IN SELECT jsonb_object_keys(new_data) LOOP
            IF old_data->field_name IS DISTINCT FROM new_data->field_name THEN
                changed_fields := array_append(changed_fields, field_name);
            END IF;
        END LOOP;
        
        -- Only log if there are actual changes
        IF array_length(changed_fields, 1) > 0 THEN
            INSERT INTO audit_trail (
                table_name, record_id, action, old_values, new_values, 
                changed_fields, timestamp, description
            ) VALUES (
                TG_TABLE_NAME, NEW.id, 'UPDATE', old_data, new_data, 
                changed_fields, CURRENT_TIMESTAMP, 
                'Record updated in ' || TG_TABLE_NAME || '. Changed fields: ' || array_to_string(changed_fields, ', ')
            );
        END IF;
        
        RETURN NEW;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create audit triggers for all main tables
CREATE TRIGGER audit_visits_trigger
    AFTER INSERT OR UPDATE OR DELETE ON visits
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_test_templates_trigger
    AFTER INSERT OR UPDATE OR DELETE ON test_templates
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_lab_tests_trigger
    AFTER INSERT OR UPDATE OR DELETE ON lab_tests
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_samples_trigger
    AFTER INSERT OR UPDATE OR DELETE ON samples
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_lab_reports_trigger
    AFTER INSERT OR UPDATE OR DELETE ON lab_reports
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_billing_trigger
    AFTER INSERT OR UPDATE OR DELETE ON billing
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_lab_equipment_trigger
    AFTER INSERT OR UPDATE OR DELETE ON lab_equipment
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_equipment_maintenance_trigger
    AFTER INSERT OR UPDATE OR DELETE ON equipment_maintenance
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_equipment_calibration_trigger
    AFTER INSERT OR UPDATE OR DELETE ON equipment_calibration
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_inventory_items_trigger
    AFTER INSERT OR UPDATE OR DELETE ON inventory_items
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

CREATE TRIGGER audit_inventory_transactions_trigger
    AFTER INSERT OR UPDATE OR DELETE ON inventory_transactions
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

-- Create function to clean old audit records (retention policy)
CREATE OR REPLACE FUNCTION cleanup_audit_trail(retention_days INTEGER DEFAULT 365)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM audit_trail 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '1 day' * retention_days;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    -- Log the cleanup operation
    INSERT INTO audit_trail (
        table_name, action, description, timestamp, severity
    ) VALUES (
        'audit_trail', 'DELETE', 
        'Cleaned up ' || deleted_count || ' old audit records older than ' || retention_days || ' days',
        CURRENT_TIMESTAMP, 'INFO'
    );
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create function to generate system alerts
CREATE OR REPLACE FUNCTION create_system_alert(
    p_alert_type VARCHAR(50),
    p_alert_code VARCHAR(20),
    p_title VARCHAR(255),
    p_description TEXT,
    p_severity VARCHAR(20) DEFAULT 'MEDIUM',
    p_source_module VARCHAR(100) DEFAULT NULL,
    p_source_table VARCHAR(100) DEFAULT NULL,
    p_source_record_id BIGINT DEFAULT NULL,
    p_alert_data JSONB DEFAULT NULL
)
RETURNS BIGINT AS $$
DECLARE
    alert_id BIGINT;
BEGIN
    INSERT INTO system_alerts (
        alert_type, alert_code, title, description, severity,
        source_module, source_table, source_record_id, alert_data
    ) VALUES (
        p_alert_type, p_alert_code, p_title, p_description, p_severity,
        p_source_module, p_source_table, p_source_record_id, p_alert_data
    ) RETURNING system_alerts.alert_id INTO alert_id;
    
    RETURN alert_id;
END;
$$ LANGUAGE plpgsql;

-- Create function to check for low stock and create alerts
CREATE OR REPLACE FUNCTION check_inventory_alerts()
RETURNS INTEGER AS $$
DECLARE
    item_record RECORD;
    alert_count INTEGER := 0;
BEGIN
    -- Check for low stock items
    FOR item_record IN 
        SELECT id, name, sku, current_stock, minimum_stock_level, reorder_point
        FROM inventory_items 
        WHERE status = 'ACTIVE' 
        AND current_stock <= COALESCE(reorder_point, minimum_stock_level)
    LOOP
        -- Create low stock alert
        PERFORM create_system_alert(
            'INVENTORY',
            'LOW_STOCK',
            'Low Stock Alert: ' || item_record.name,
            'Item ' || item_record.name || ' (SKU: ' || item_record.sku || ') has low stock. Current: ' || 
            item_record.current_stock || ', Minimum: ' || item_record.minimum_stock_level,
            'HIGH',
            'INVENTORY',
            'inventory_items',
            item_record.id,
            jsonb_build_object(
                'sku', item_record.sku,
                'current_stock', item_record.current_stock,
                'minimum_stock', item_record.minimum_stock_level,
                'reorder_point', item_record.reorder_point
            )
        );
        
        alert_count := alert_count + 1;
    END LOOP;
    
    -- Check for expired items
    FOR item_record IN 
        SELECT id, name, sku, expiry_date
        FROM inventory_items 
        WHERE status = 'ACTIVE' 
        AND expiry_date <= CURRENT_DATE + INTERVAL '30 days'
        AND expiry_date IS NOT NULL
    LOOP
        -- Create expiry alert
        PERFORM create_system_alert(
            'INVENTORY',
            'EXPIRY_WARNING',
            'Expiry Warning: ' || item_record.name,
            'Item ' || item_record.name || ' (SKU: ' || item_record.sku || ') will expire on ' || item_record.expiry_date,
            CASE 
                WHEN item_record.expiry_date <= CURRENT_DATE THEN 'CRITICAL'
                WHEN item_record.expiry_date <= CURRENT_DATE + INTERVAL '7 days' THEN 'HIGH'
                ELSE 'MEDIUM'
            END,
            'INVENTORY',
            'inventory_items',
            item_record.id,
            jsonb_build_object(
                'sku', item_record.sku,
                'expiry_date', item_record.expiry_date,
                'days_until_expiry', item_record.expiry_date - CURRENT_DATE
            )
        );
        
        alert_count := alert_count + 1;
    END LOOP;
    
    RETURN alert_count;
END;
$$ LANGUAGE plpgsql;

-- Insert some initial system notifications
INSERT INTO system_notifications (notification_type, title, message, severity, is_system_wide)
VALUES 
    ('SYSTEM', 'System Initialized', 'Lab Operations Management System has been successfully initialized with audit trail capabilities.', 'INFO', TRUE),
    ('MAINTENANCE', 'Scheduled Maintenance', 'System maintenance is scheduled for this weekend. Please save your work regularly.', 'MEDIUM', TRUE);

-- Create a view for active alerts
CREATE VIEW active_alerts AS
SELECT 
    alert_id,
    alert_type,
    alert_code,
    title,
    description,
    severity,
    source_module,
    triggered_at,
    triggered_by,
    alert_data
FROM system_alerts 
WHERE status = 'ACTIVE'
ORDER BY 
    CASE severity 
        WHEN 'CRITICAL' THEN 1 
        WHEN 'HIGH' THEN 2 
        WHEN 'MEDIUM' THEN 3 
        WHEN 'LOW' THEN 4 
        ELSE 5 
    END,
    triggered_at DESC;

-- Create a view for recent audit activities
CREATE VIEW recent_audit_activities AS
SELECT 
    audit_id,
    table_name,
    record_id,
    action,
    user_name,
    timestamp,
    description,
    changed_fields
FROM audit_trail 
WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
ORDER BY timestamp DESC
LIMIT 100;
