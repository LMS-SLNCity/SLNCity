-- Initialize database with sample data for lab operations

-- Sample test templates
INSERT INTO test_templates (name, description, parameters, base_price) VALUES
('Complete Blood Count (CBC)', 'Basic blood test measuring various blood components', 
 '{"fields": [
   {"name": "hemoglobin", "type": "number", "unit": "g/dL", "reference_range": {"min": 12.0, "max": 16.0}},
   {"name": "white_blood_cells", "type": "number", "unit": "cells/μL", "reference_range": {"min": 4000, "max": 11000}},
   {"name": "platelets", "type": "number", "unit": "cells/μL", "reference_range": {"min": 150000, "max": 450000}}
 ]}', 
 500.00),

('Lipid Profile', 'Cholesterol and triglyceride levels', 
 '{"fields": [
   {"name": "total_cholesterol", "type": "number", "unit": "mg/dL", "reference_range": {"max": 200}},
   {"name": "hdl_cholesterol", "type": "number", "unit": "mg/dL", "reference_range": {"min": 40}},
   {"name": "ldl_cholesterol", "type": "number", "unit": "mg/dL", "reference_range": {"max": 100}},
   {"name": "triglycerides", "type": "number", "unit": "mg/dL", "reference_range": {"max": 150}}
 ]}', 
 800.00),

('Blood Sugar (Fasting)', 'Fasting glucose level test', 
 '{"fields": [
   {"name": "glucose", "type": "number", "unit": "mg/dL", "reference_range": {"min": 70, "max": 100}}
 ]}', 
 200.00),

('Thyroid Function Test', 'TSH, T3, T4 levels', 
 '{"fields": [
   {"name": "tsh", "type": "number", "unit": "mIU/L", "reference_range": {"min": 0.4, "max": 4.0}},
   {"name": "t3", "type": "number", "unit": "ng/dL", "reference_range": {"min": 80, "max": 200}},
   {"name": "t4", "type": "number", "unit": "μg/dL", "reference_range": {"min": 5.0, "max": 12.0}}
 ]}', 
 1200.00);

-- Sample visit with patient details
INSERT INTO visits (patient_details, status) VALUES
('{"name": "Rajesh Kumar", "age": 45, "gender": "M", "phone": "9876543210", "address": "Hyderabad, Telangana"}', 'pending'),
('{"name": "Priya Sharma", "age": 32, "gender": "F", "phone": "9876543211", "address": "Mumbai, Maharashtra"}', 'in-progress'),
('{"name": "Amit Patel", "age": 28, "gender": "M", "phone": "9876543212", "address": "Ahmedabad, Gujarat"}', 'completed');

-- Sample lab tests
INSERT INTO lab_tests (visit_id, test_template_id, status, price, results, approved, approved_by, approved_at) VALUES
(1, 1, 'completed', 500.00, 
 '{"hemoglobin": 14.2, "white_blood_cells": 7500, "platelets": 250000}', 
 true, 'Dr. Smith', NOW()),
(1, 3, 'completed', 200.00, 
 '{"glucose": 95}', 
 true, 'Dr. Smith', NOW()),
(2, 2, 'in-progress', 800.00, null, false, null, null);

-- Sample billing
INSERT INTO billing (visit_id, total_amount, paid) VALUES
(1, 700.00, true),
(3, 1200.00, false);
