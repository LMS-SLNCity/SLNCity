-- =====================================================
-- Lab Operations Management System - Sample Data
-- =====================================================
-- This script inserts sample data for development and
-- testing purposes. Run this after the application
-- has created the database schema.
-- =====================================================

-- Note: This script will be executed automatically when
-- using Docker Compose, or can be run manually after
-- application startup.

-- =====================================================
-- Sample Test Templates
-- =====================================================

-- Only insert if tables exist (for Docker initialization)
DO $$
BEGIN
    -- Check if test_templates table exists
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'test_templates') THEN
        
        -- Insert sample test templates
        INSERT INTO test_templates (name, description, parameters, base_price, created_at) VALUES
        (
            'Complete Blood Count (CBC)',
            'Comprehensive blood analysis including RBC, WBC, platelets, and hemoglobin',
            '{"hemoglobin": {"unit": "g/dL", "normal_range": "12.0-15.5"}, "rbc": {"unit": "million/μL", "normal_range": "4.2-5.4"}, "wbc": {"unit": "thousand/μL", "normal_range": "4.5-11.0"}, "platelets": {"unit": "thousand/μL", "normal_range": "150-450"}}'::json,
            150.00,
            NOW()
        ),
        (
            'Lipid Profile',
            'Cholesterol and triglyceride levels assessment',
            '{"total_cholesterol": {"unit": "mg/dL", "normal_range": "<200"}, "ldl": {"unit": "mg/dL", "normal_range": "<100"}, "hdl": {"unit": "mg/dL", "normal_range": ">40"}, "triglycerides": {"unit": "mg/dL", "normal_range": "<150"}}'::json,
            120.00,
            NOW()
        ),
        (
            'Liver Function Test (LFT)',
            'Assessment of liver enzymes and function',
            '{"alt": {"unit": "U/L", "normal_range": "7-56"}, "ast": {"unit": "U/L", "normal_range": "10-40"}, "bilirubin_total": {"unit": "mg/dL", "normal_range": "0.3-1.2"}, "albumin": {"unit": "g/dL", "normal_range": "3.5-5.0"}}'::json,
            180.00,
            NOW()
        ),
        (
            'Kidney Function Test (KFT)',
            'Assessment of kidney function and electrolytes',
            '{"creatinine": {"unit": "mg/dL", "normal_range": "0.6-1.2"}, "urea": {"unit": "mg/dL", "normal_range": "15-40"}, "sodium": {"unit": "mEq/L", "normal_range": "136-145"}, "potassium": {"unit": "mEq/L", "normal_range": "3.5-5.0"}}'::json,
            160.00,
            NOW()
        ),
        (
            'Thyroid Function Test (TFT)',
            'Assessment of thyroid hormone levels',
            '{"tsh": {"unit": "mIU/L", "normal_range": "0.4-4.0"}, "t3": {"unit": "ng/dL", "normal_range": "80-200"}, "t4": {"unit": "μg/dL", "normal_range": "5.0-12.0"}}'::json,
            200.00,
            NOW()
        ),
        (
            'Blood Sugar (Fasting)',
            'Fasting blood glucose level measurement',
            '{"glucose": {"unit": "mg/dL", "normal_range": "70-100", "fasting": true}}'::json,
            80.00,
            NOW()
        ),
        (
            'HbA1c',
            'Average blood sugar levels over 2-3 months',
            '{"hba1c": {"unit": "%", "normal_range": "<5.7", "prediabetic_range": "5.7-6.4", "diabetic_range": ">=6.5"}}'::json,
            250.00,
            NOW()
        ),
        (
            'Urine Analysis',
            'Complete urine examination',
            '{"protein": {"unit": "mg/dL", "normal": "negative"}, "glucose": {"unit": "mg/dL", "normal": "negative"}, "ketones": {"normal": "negative"}, "blood": {"normal": "negative"}, "specific_gravity": {"normal_range": "1.005-1.030"}}'::json,
            100.00,
            NOW()
        );

        RAISE NOTICE 'Sample test templates inserted successfully';
    ELSE
        RAISE NOTICE 'test_templates table does not exist yet. Skipping sample data insertion.';
    END IF;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error inserting sample test templates: %', SQLERRM;
END
$$;

-- =====================================================
-- Sample Visits (for development/testing)
-- =====================================================

DO $$
BEGIN
    -- Check if visits table exists
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'visits') THEN
        
        -- Insert sample visits
        INSERT INTO visits (patient_details, status, created_at) VALUES
        (
            '{"name": "John Doe", "age": 35, "gender": "Male", "phone": "+1-555-0101", "email": "john.doe@email.com", "address": "123 Main St, City, State 12345", "emergency_contact": "+1-555-0102"}'::json,
            'PENDING',
            NOW() - INTERVAL '2 hours'
        ),
        (
            '{"name": "Jane Smith", "age": 28, "gender": "Female", "phone": "+1-555-0201", "email": "jane.smith@email.com", "address": "456 Oak Ave, City, State 12345", "emergency_contact": "+1-555-0202"}'::json,
            'IN_PROGRESS',
            NOW() - INTERVAL '1 hour'
        ),
        (
            '{"name": "Robert Johnson", "age": 45, "gender": "Male", "phone": "+1-555-0301", "email": "robert.j@email.com", "address": "789 Pine Rd, City, State 12345", "emergency_contact": "+1-555-0302"}'::json,
            'COMPLETED',
            NOW() - INTERVAL '3 days'
        );

        RAISE NOTICE 'Sample visits inserted successfully';
    ELSE
        RAISE NOTICE 'visits table does not exist yet. Skipping sample visits insertion.';
    END IF;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error inserting sample visits: %', SQLERRM;
END
$$;

-- =====================================================
-- Sample Lab Tests (for development/testing)
-- =====================================================

DO $$
DECLARE
    visit_id_1 BIGINT;
    visit_id_2 BIGINT;
    visit_id_3 BIGINT;
    template_id_cbc BIGINT;
    template_id_lipid BIGINT;
    template_id_lft BIGINT;
BEGIN
    -- Check if required tables exist
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'lab_tests') 
       AND EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'visits')
       AND EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'test_templates') THEN
        
        -- Get visit IDs
        SELECT visit_id INTO visit_id_1 FROM visits WHERE patient_details->>'name' = 'John Doe' LIMIT 1;
        SELECT visit_id INTO visit_id_2 FROM visits WHERE patient_details->>'name' = 'Jane Smith' LIMIT 1;
        SELECT visit_id INTO visit_id_3 FROM visits WHERE patient_details->>'name' = 'Robert Johnson' LIMIT 1;
        
        -- Get template IDs
        SELECT template_id INTO template_id_cbc FROM test_templates WHERE name = 'Complete Blood Count (CBC)' LIMIT 1;
        SELECT template_id INTO template_id_lipid FROM test_templates WHERE name = 'Lipid Profile' LIMIT 1;
        SELECT template_id INTO template_id_lft FROM test_templates WHERE name = 'Liver Function Test (LFT)' LIMIT 1;
        
        -- Insert sample lab tests if we have the required data
        IF visit_id_1 IS NOT NULL AND template_id_cbc IS NOT NULL THEN
            INSERT INTO lab_tests (visit_id, test_template_id, results, status, price, approved, approved_by, approved_at) VALUES
            (
                visit_id_1,
                template_id_cbc,
                NULL, -- No results yet
                'PENDING',
                150.00,
                FALSE,
                NULL,
                NULL
            );
        END IF;
        
        IF visit_id_2 IS NOT NULL AND template_id_lipid IS NOT NULL THEN
            INSERT INTO lab_tests (visit_id, test_template_id, results, status, price, approved, approved_by, approved_at) VALUES
            (
                visit_id_2,
                template_id_lipid,
                '{"total_cholesterol": {"value": 185, "unit": "mg/dL", "status": "normal"}, "ldl": {"value": 95, "unit": "mg/dL", "status": "normal"}, "hdl": {"value": 45, "unit": "mg/dL", "status": "normal"}, "triglycerides": {"value": 120, "unit": "mg/dL", "status": "normal"}}'::json,
                'COMPLETED',
                120.00,
                FALSE,
                NULL,
                NULL
            );
        END IF;
        
        IF visit_id_3 IS NOT NULL AND template_id_lft IS NOT NULL THEN
            INSERT INTO lab_tests (visit_id, test_template_id, results, status, price, approved, approved_by, approved_at) VALUES
            (
                visit_id_3,
                template_id_lft,
                '{"alt": {"value": 25, "unit": "U/L", "status": "normal"}, "ast": {"value": 22, "unit": "U/L", "status": "normal"}, "bilirubin_total": {"value": 0.8, "unit": "mg/dL", "status": "normal"}, "albumin": {"value": 4.2, "unit": "g/dL", "status": "normal"}}'::json,
                'APPROVED',
                180.00,
                TRUE,
                'Dr. Smith',
                NOW() - INTERVAL '1 day'
            );
        END IF;

        RAISE NOTICE 'Sample lab tests inserted successfully';
    ELSE
        RAISE NOTICE 'Required tables do not exist yet. Skipping sample lab tests insertion.';
    END IF;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error inserting sample lab tests: %', SQLERRM;
END
$$;

-- =====================================================
-- Sample Billing (for development/testing)
-- =====================================================

DO $$
DECLARE
    visit_id_3 BIGINT;
BEGIN
    -- Check if required tables exist
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'billing') 
       AND EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'visits') THEN
        
        -- Get visit ID for completed visit
        SELECT visit_id INTO visit_id_3 FROM visits WHERE patient_details->>'name' = 'Robert Johnson' LIMIT 1;
        
        -- Insert sample billing record
        IF visit_id_3 IS NOT NULL THEN
            INSERT INTO billing (visit_id, total_amount, paid, created_at) VALUES
            (
                visit_id_3,
                180.00,
                TRUE,
                NOW() - INTERVAL '1 day'
            );
        END IF;

        RAISE NOTICE 'Sample billing record inserted successfully';
    ELSE
        RAISE NOTICE 'Required tables do not exist yet. Skipping sample billing insertion.';
    END IF;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error inserting sample billing: %', SQLERRM;
END
$$;

-- =====================================================
-- Create Performance Indexes
-- =====================================================

-- Call the function to create performance indexes
SELECT create_performance_indexes();

-- =====================================================
-- Final Status
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Sample Data Initialization Complete!';
    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Sample data includes:';
    RAISE NOTICE '- 8 Test Templates (CBC, Lipid Profile, LFT, etc.)';
    RAISE NOTICE '- 3 Sample Visits with different statuses';
    RAISE NOTICE '- 3 Sample Lab Tests with various results';
    RAISE NOTICE '- 1 Sample Billing record';
    RAISE NOTICE '- Performance indexes created';
    RAISE NOTICE '=================================================';
END
$$;
