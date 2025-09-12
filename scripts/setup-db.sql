-- =====================================================
-- Lab Operations Management System - Database Setup
-- =====================================================
-- This script sets up the PostgreSQL database for the
-- Lab Operations Management System with proper users,
-- permissions, and initial configuration.
-- =====================================================

-- Connect as postgres superuser to run this script:
-- psql -U postgres -f scripts/setup-db.sql

-- =====================================================
-- 1. DATABASE CREATION
-- =====================================================

-- Create the main database
CREATE DATABASE lab_operations
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE = template0;

COMMENT ON DATABASE lab_operations IS 'Lab Operations Management System Database';

-- =====================================================
-- 2. USER MANAGEMENT
-- =====================================================

-- Create application user
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'lab_user') THEN
        CREATE USER lab_user WITH
            LOGIN
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            INHERIT
            NOREPLICATION
            CONNECTION LIMIT -1
            PASSWORD 'lab_password_change_in_production';
    END IF;
END
$$;

-- Create read-only user for reporting
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'lab_readonly') THEN
        CREATE USER lab_readonly WITH
            LOGIN
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            INHERIT
            NOREPLICATION
            CONNECTION LIMIT 10
            PASSWORD 'readonly_password_change_in_production';
    END IF;
END
$$;

-- =====================================================
-- 3. PERMISSIONS SETUP
-- =====================================================

-- Grant database access to application user
GRANT ALL PRIVILEGES ON DATABASE lab_operations TO lab_user;

-- Grant read-only access to reporting user
GRANT CONNECT ON DATABASE lab_operations TO lab_readonly;

-- Connect to the lab_operations database for schema permissions
\c lab_operations

-- Grant schema permissions to application user
GRANT ALL ON SCHEMA public TO lab_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO lab_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO lab_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO lab_user;

-- Grant future permissions to application user
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO lab_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO lab_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO lab_user;

-- Grant read-only permissions to reporting user
GRANT USAGE ON SCHEMA public TO lab_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO lab_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO lab_readonly;

-- =====================================================
-- 4. EXTENSIONS AND FUNCTIONS
-- =====================================================

-- Enable UUID extension (if needed for future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pgcrypto for password hashing (if needed)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =====================================================
-- 5. UTILITY FUNCTIONS
-- =====================================================

-- Function to validate patient details JSON
CREATE OR REPLACE FUNCTION validate_patient_details(patient_json JSON)
RETURNS BOOLEAN AS $$
BEGIN
    -- Check if required fields exist and are not empty
    IF patient_json IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Check for required name field
    IF patient_json->>'name' IS NULL OR trim(patient_json->>'name') = '' THEN
        RETURN FALSE;
    END IF;
    
    -- Check for valid age (if provided)
    IF patient_json->>'age' IS NOT NULL THEN
        BEGIN
            IF (patient_json->>'age')::INTEGER < 0 OR (patient_json->>'age')::INTEGER > 150 THEN
                RETURN FALSE;
            END IF;
        EXCEPTION WHEN OTHERS THEN
            RETURN FALSE;
        END;
    END IF;
    
    -- Check for valid email format (if provided)
    IF patient_json->>'email' IS NOT NULL AND patient_json->>'email' != '' THEN
        IF NOT (patient_json->>'email' ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$') THEN
            RETURN FALSE;
        END IF;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function to validate test parameters JSON
CREATE OR REPLACE FUNCTION validate_test_parameters(params_json JSON)
RETURNS BOOLEAN AS $$
BEGIN
    -- Check if parameters JSON is valid and not empty
    IF params_json IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Check if it's a valid JSON object (not array or primitive)
    IF json_typeof(params_json) != 'object' THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 6. PERFORMANCE INDEXES (Created after tables exist)
-- =====================================================

-- Note: These indexes will be created by the application
-- when Hibernate creates the tables. This section is for
-- additional performance indexes that may be needed.

-- Function to create performance indexes (run after application startup)
CREATE OR REPLACE FUNCTION create_performance_indexes()
RETURNS VOID AS $$
BEGIN
    -- Create indexes only if tables exist
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'visits') THEN
        -- Visits table indexes
        CREATE INDEX IF NOT EXISTS idx_visits_status ON visits(status);
        CREATE INDEX IF NOT EXISTS idx_visits_created_at ON visits(created_at);
        CREATE INDEX IF NOT EXISTS idx_visits_patient_name ON visits USING GIN ((patient_details->>'name'));
        
        -- Lab tests table indexes
        CREATE INDEX IF NOT EXISTS idx_lab_tests_status ON lab_tests(status);
        CREATE INDEX IF NOT EXISTS idx_lab_tests_visit_id ON lab_tests(visit_id);
        CREATE INDEX IF NOT EXISTS idx_lab_tests_template_id ON lab_tests(test_template_id);
        CREATE INDEX IF NOT EXISTS idx_lab_tests_approved ON lab_tests(approved);
        
        -- Test templates table indexes
        CREATE INDEX IF NOT EXISTS idx_test_templates_name ON test_templates(name);
        CREATE INDEX IF NOT EXISTS idx_test_templates_created_at ON test_templates(created_at);
        
        -- Billing table indexes
        CREATE INDEX IF NOT EXISTS idx_billing_paid ON billing(paid);
        CREATE INDEX IF NOT EXISTS idx_billing_visit_id ON billing(visit_id);
        CREATE INDEX IF NOT EXISTS idx_billing_created_at ON billing(created_at);
        
        RAISE NOTICE 'Performance indexes created successfully';
    ELSE
        RAISE NOTICE 'Tables do not exist yet. Run this function after application startup.';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 7. MONITORING VIEWS
-- =====================================================

-- View for database statistics
CREATE OR REPLACE VIEW db_stats AS
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as table_size,
    pg_stat_get_tuples_returned(c.oid) as tuples_read,
    pg_stat_get_tuples_fetched(c.oid) as tuples_fetched,
    pg_stat_get_tuples_inserted(c.oid) as tuples_inserted,
    pg_stat_get_tuples_updated(c.oid) as tuples_updated,
    pg_stat_get_tuples_deleted(c.oid) as tuples_deleted
FROM pg_tables pt
JOIN pg_class c ON c.relname = pt.tablename
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- View for active connections
CREATE OR REPLACE VIEW active_connections AS
SELECT 
    pid,
    usename,
    application_name,
    client_addr,
    client_port,
    backend_start,
    state,
    query_start,
    left(query, 100) as current_query
FROM pg_stat_activity 
WHERE datname = 'lab_operations' AND state = 'active';

-- =====================================================
-- 8. BACKUP FUNCTIONS
-- =====================================================

-- Function to get backup information
CREATE OR REPLACE FUNCTION get_backup_info()
RETURNS TABLE (
    database_name TEXT,
    database_size TEXT,
    total_tables INTEGER,
    backup_command TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        current_database()::TEXT,
        pg_size_pretty(pg_database_size(current_database()))::TEXT,
        (SELECT count(*)::INTEGER FROM information_schema.tables WHERE table_schema = 'public'),
        ('pg_dump -U lab_user -h localhost -d ' || current_database() || ' > backup_' || 
         to_char(now(), 'YYYY_MM_DD_HH24_MI_SS') || '.sql')::TEXT;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 9. SECURITY SETTINGS
-- =====================================================

-- Revoke public schema creation from public role
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

-- Grant create only to lab_user
GRANT CREATE ON SCHEMA public TO lab_user;

-- =====================================================
-- 10. COMPLETION MESSAGE
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Lab Operations Database Setup Complete!';
    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Database: lab_operations';
    RAISE NOTICE 'Application User: lab_user';
    RAISE NOTICE 'Read-only User: lab_readonly';
    RAISE NOTICE '';
    RAISE NOTICE 'Next Steps:';
    RAISE NOTICE '1. Change default passwords in production';
    RAISE NOTICE '2. Update application.yml with correct credentials';
    RAISE NOTICE '3. Start the Spring Boot application';
    RAISE NOTICE '4. Run: SELECT create_performance_indexes(); (after app startup)';
    RAISE NOTICE '=================================================';
END
$$;

-- Test the setup
SELECT 'Database setup completed successfully!' as status;
