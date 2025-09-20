#!/bin/bash

echo "🔬 LAB TECHNICIAN WORKFLOW TEST"
echo "==============================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "\n${BLUE}Phase 1: Creating Equipment${NC}"

# Create Chemistry Analyzer
echo "Creating Chemistry Analyzer..."
curl -s -X POST "http://localhost:8080/api/v1/equipment" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Automated Chemistry Analyzer",
    "manufacturer": "Siemens",
    "model": "Dimension Vista 1500",
    "serialNumber": "SN001-VISTA-2024",
    "equipmentType": "ANALYZER",
    "status": "ACTIVE",
    "location": "Chemistry Lab - Section A",
    "purchaseDate": "2024-01-15T00:00:00",
    "warrantyExpiry": "2027-01-15T00:00:00",
    "nextMaintenance": "2024-12-01T00:00:00",
    "calibrationDue": "2024-11-15T00:00:00",
    "notes": "Primary chemistry analyzer for routine tests"
  }' > /dev/null && echo -e "${GREEN}✅ Chemistry Analyzer created${NC}"

# Create Hematology Analyzer
echo "Creating Hematology Analyzer..."
curl -s -X POST "http://localhost:8080/api/v1/equipment" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hematology Analyzer",
    "manufacturer": "Sysmex",
    "model": "XN-1000",
    "serialNumber": "SN002-XN-2024",
    "equipmentType": "ANALYZER",
    "status": "ACTIVE",
    "location": "Hematology Lab",
    "purchaseDate": "2024-02-01T00:00:00",
    "warrantyExpiry": "2027-02-01T00:00:00",
    "nextMaintenance": "2024-10-15T00:00:00",
    "calibrationDue": "2024-10-01T00:00:00",
    "notes": "Complete blood count analyzer"
  }' > /dev/null && echo -e "${GREEN}✅ Hematology Analyzer created${NC}"

# Create Microscope
echo "Creating Microscope..."
curl -s -X POST "http://localhost:8080/api/v1/equipment" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Microscope",
    "manufacturer": "Olympus",
    "model": "CX23",
    "serialNumber": "SN003-CX23-2024",
    "equipmentType": "MICROSCOPE",
    "status": "ACTIVE",
    "location": "Microbiology Lab",
    "purchaseDate": "2024-03-01T00:00:00",
    "warrantyExpiry": "2026-03-01T00:00:00",
    "nextMaintenance": "2025-03-01T00:00:00",
    "calibrationDue": "2025-01-01T00:00:00",
    "notes": "Binocular microscope for routine examinations"
  }' > /dev/null && echo -e "${GREEN}✅ Microscope created${NC}"

echo -e "\n${BLUE}Phase 2: Creating Test Templates${NC}"

# Create CBC Test Template
echo "Creating CBC Test Template..."
template1_id=$(curl -s -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Complete Blood Count",
    "description": "Full CBC with differential",
    "basePrice": 300.00,
    "parameters": {
      "sampleType": "WHOLE_BLOOD",
      "volumeRequired": 5.0,
      "containerType": "EDTA tube",
      "processingTime": "2 hours"
    }
  }' | jq -r '.templateId')
echo -e "${GREEN}✅ CBC Template created (ID: $template1_id)${NC}"

# Create Chemistry Panel Template
echo "Creating Chemistry Panel Template..."
template2_id=$(curl -s -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Basic Metabolic Panel",
    "description": "Glucose, BUN, Creatinine, Electrolytes",
    "basePrice": 450.00,
    "parameters": {
      "sampleType": "SERUM",
      "volumeRequired": 3.0,
      "containerType": "SST tube",
      "processingTime": "1 hour"
    }
  }' | jq -r '.templateId')
echo -e "${GREEN}✅ Chemistry Panel Template created (ID: $template2_id)${NC}"

# Create Lipid Profile Template
echo "Creating Lipid Profile Template..."
template3_id=$(curl -s -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lipid Profile",
    "description": "Total Cholesterol, HDL, LDL, Triglycerides",
    "basePrice": 350.00,
    "parameters": {
      "sampleType": "SERUM",
      "volumeRequired": 2.0,
      "containerType": "SST tube",
      "processingTime": "1.5 hours"
    }
  }' | jq -r '.templateId')
echo -e "${GREEN}✅ Lipid Profile Template created (ID: $template3_id)${NC}"

echo -e "\n${BLUE}Phase 3: Creating Patient Visits and Tests${NC}"

# Create Patient 1
echo "Creating Patient 1..."
visit1_id=$(curl -s -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "John Smith",
      "age": 45,
      "gender": "MALE",
      "phone": "1111111111",
      "email": "john.smith@test.com",
      "address": "123 Main St, City"
    }
  }' | jq -r '.visitId')
echo -e "${GREEN}✅ Patient 1 created (Visit ID: $visit1_id)${NC}"

# Create Patient 2
echo "Creating Patient 2..."
visit2_id=$(curl -s -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Sarah Johnson",
      "age": 32,
      "gender": "FEMALE",
      "phone": "2222222222",
      "email": "sarah.johnson@test.com",
      "address": "456 Oak Ave, City"
    }
  }' | jq -r '.visitId')
echo -e "${GREEN}✅ Patient 2 created (Visit ID: $visit2_id)${NC}"

# Create Patient 3
echo "Creating Patient 3..."
visit3_id=$(curl -s -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Michael Brown",
      "age": 28,
      "gender": "MALE",
      "phone": "3333333333",
      "email": "michael.brown@test.com",
      "address": "789 Pine St, City"
    }
  }' | jq -r '.visitId')
echo -e "${GREEN}✅ Patient 3 created (Visit ID: $visit3_id)${NC}"

echo -e "\n${BLUE}Phase 4: Ordering Lab Tests${NC}"

# Order tests for Patient 1
echo "Ordering CBC for Patient 1..."
test1_id=$(curl -s -X POST "http://localhost:8080/visits/$visit1_id/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $template1_id}" | jq -r '.testId')
echo -e "${GREEN}✅ CBC ordered for Patient 1 (Test ID: $test1_id)${NC}"

echo "Ordering Chemistry Panel for Patient 1..."
test2_id=$(curl -s -X POST "http://localhost:8080/visits/$visit1_id/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $template2_id}" | jq -r '.testId')
echo -e "${GREEN}✅ Chemistry Panel ordered for Patient 1 (Test ID: $test2_id)${NC}"

# Order tests for Patient 2
echo "Ordering Lipid Profile for Patient 2..."
test3_id=$(curl -s -X POST "http://localhost:8080/visits/$visit2_id/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $template3_id}" | jq -r '.testId')
echo -e "${GREEN}✅ Lipid Profile ordered for Patient 2 (Test ID: $test3_id)${NC}"

echo "Ordering CBC for Patient 2..."
test4_id=$(curl -s -X POST "http://localhost:8080/visits/$visit2_id/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $template1_id}" | jq -r '.testId')
echo -e "${GREEN}✅ CBC ordered for Patient 2 (Test ID: $test4_id)${NC}"

# Order test for Patient 3
echo "Ordering Chemistry Panel for Patient 3..."
test5_id=$(curl -s -X POST "http://localhost:8080/visits/$visit3_id/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $template2_id}" | jq -r '.testId')
echo -e "${GREEN}✅ Chemistry Panel ordered for Patient 3 (Test ID: $test5_id)${NC}"

echo -e "\n${BLUE}Phase 5: Collecting Samples${NC}"

# Collect samples for the tests
echo "Collecting sample for Test $test1_id..."
curl -s -X POST "http://localhost:8080/sample-collection/collect/$test1_id" \
  -H "Content-Type: application/json" \
  -d '{
    "sampleType": "WHOLE_BLOOD",
    "collectedBy": "phlebotomist_1",
    "collectionSite": "Left antecubital vein",
    "containerType": "EDTA tube",
    "volumeReceived": 5.0,
    "notes": "Sample collected successfully"
  }' > /dev/null && echo -e "${GREEN}✅ Sample collected for Test $test1_id${NC}"

echo "Collecting sample for Test $test2_id..."
curl -s -X POST "http://localhost:8080/sample-collection/collect/$test2_id" \
  -H "Content-Type: application/json" \
  -d '{
    "sampleType": "SERUM",
    "collectedBy": "phlebotomist_1",
    "collectionSite": "Right antecubital vein",
    "containerType": "SST tube",
    "volumeReceived": 3.0,
    "notes": "Sample collected successfully"
  }' > /dev/null && echo -e "${GREEN}✅ Sample collected for Test $test2_id${NC}"

echo "Collecting sample for Test $test3_id..."
curl -s -X POST "http://localhost:8080/sample-collection/collect/$test3_id" \
  -H "Content-Type: application/json" \
  -d '{
    "sampleType": "SERUM",
    "collectedBy": "phlebotomist_2",
    "collectionSite": "Left antecubital vein",
    "containerType": "SST tube",
    "volumeReceived": 2.0,
    "notes": "Sample collected successfully"
  }' > /dev/null && echo -e "${GREEN}✅ Sample collected for Test $test3_id${NC}"

echo -e "\n${BLUE}Phase 6: Verification${NC}"

# Check equipment count
equipment_count=$(curl -s "http://localhost:8080/api/v1/equipment" | jq 'length')
echo "Equipment count: $equipment_count"

# Check lab tests count
lab_tests_count=$(curl -s "http://localhost:8080/lab-tests" | jq 'length')
echo "Lab tests count: $lab_tests_count"

# Check samples count
samples_count=$(curl -s "http://localhost:8080/samples" | jq 'length')
echo "Samples count: $samples_count"

echo -e "\n${GREEN}=== LAB TECHNICIAN TEST DATA CREATED SUCCESSFULLY! ===${NC}"
echo ""
echo "Summary:"
echo "• Equipment: $equipment_count pieces"
echo "• Lab Tests: $lab_tests_count tests"
echo "• Samples: $samples_count samples"
echo ""
echo -e "${BLUE}🎯 Lab Technician Dashboard is ready for testing!${NC}"
echo ""
echo "Dashboard URL: http://localhost:8080/technician/dashboard.html"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Open the technician dashboard"
echo "2. Check that equipment is displayed"
echo "3. Check that lab tests are shown in the queue"
echo "4. Test sample processing functionality"
echo "5. Test equipment management features"
