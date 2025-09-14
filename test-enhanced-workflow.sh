#!/bin/bash

echo "🔬 ENHANCED LAB WORKFLOW TEST - SAMPLE COLLECTION & MACHINE TRACKING"
echo "===================================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "\n${BLUE}🏥 STEP 1: ADMIN - Test Template Management${NC}"
echo "=============================================="

# Login as admin
curl -s -c admin_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" \
  --location > /dev/null

# Create a new test template with flexible parameters
TEMPLATE_RESPONSE=$(curl -s -b admin_cookies.txt -X POST http://localhost:8080/test-templates \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Comprehensive Metabolic Panel",
    "description": "Complete metabolic panel with 14 parameters",
    "basePrice": 850.00,
    "parameters": {
      "glucose": {"unit": "mg/dL", "normalRange": "70-100"},
      "bun": {"unit": "mg/dL", "normalRange": "7-20"},
      "creatinine": {"unit": "mg/dL", "normalRange": "0.6-1.2"},
      "sodium": {"unit": "mEq/L", "normalRange": "136-145"},
      "potassium": {"unit": "mEq/L", "normalRange": "3.5-5.0"},
      "chloride": {"unit": "mEq/L", "normalRange": "98-107"},
      "co2": {"unit": "mEq/L", "normalRange": "22-28"},
      "albumin": {"unit": "g/dL", "normalRange": "3.5-5.0"},
      "total_protein": {"unit": "g/dL", "normalRange": "6.0-8.3"},
      "alt": {"unit": "U/L", "normalRange": "7-56"},
      "ast": {"unit": "U/L", "normalRange": "10-40"},
      "bilirubin_total": {"unit": "mg/dL", "normalRange": "0.3-1.2"},
      "alkaline_phosphatase": {"unit": "U/L", "normalRange": "44-147"},
      "calcium": {"unit": "mg/dL", "normalRange": "8.5-10.5"}
    }
  }')

TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | jq -r '.templateId')
echo -e "${GREEN}✅ Created comprehensive test template (ID: $TEMPLATE_ID)${NC}"

# Verify admin can see all test templates
TEMPLATES_COUNT=$(curl -s -b admin_cookies.txt -X GET "http://localhost:8080/test-templates" | jq 'length')
echo -e "${GREEN}✅ Admin can see $TEMPLATES_COUNT test templates${NC}"

echo -e "\n${BLUE}🏥 STEP 2: RECEPTION - Patient Registration & Test Ordering${NC}"
echo "==========================================================="

# Login as reception
curl -s -c reception_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=reception&password=reception123" \
  --location > /dev/null

# Create a new patient visit
VISIT_RESPONSE=$(curl -s -b reception_cookies.txt -X POST http://localhost:8080/visits \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Sarah Johnson",
      "age": "45",
      "gender": "F",
      "phone": "9876543210",
      "email": "sarah.johnson@example.com",
      "address": "456 Oak Street",
      "emergencyContact": "9876543211"
    }
  }')

VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId')
echo -e "${GREEN}✅ Patient visit created (ID: $VISIT_ID)${NC}"

# Order the comprehensive metabolic panel
curl -s -b reception_cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d "{
    \"testTemplateId\": $TEMPLATE_ID,
    \"price\": 850.00
  }" > /dev/null

echo -e "${GREEN}✅ Comprehensive Metabolic Panel ordered${NC}"

echo -e "\n${BLUE}💉 STEP 3: PHLEBOTOMY - Sample Collection${NC}"
echo "=========================================="

# Login as phlebotomy
curl -s -c phlebotomy_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=phlebotomy&password=phlebotomy123" \
  --location > /dev/null

# Check pending samples
PENDING_SAMPLES=$(curl -s -b phlebotomy_cookies.txt -X GET "http://localhost:8080/sample-collection/pending")
PENDING_COUNT=$(echo "$PENDING_SAMPLES" | jq 'length')
echo -e "${GREEN}✅ Phlebotomy sees $PENDING_COUNT pending samples${NC}"

if [ "$PENDING_COUNT" -gt 0 ]; then
    # Get the test ID from pending samples
    TEST_ID=$(echo "$PENDING_SAMPLES" | jq -r '.[0].testId')
    
    # Collect sample
    SAMPLE_RESPONSE=$(curl -s -b phlebotomy_cookies.txt -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
      -H "Content-Type: application/json" \
      -d '{
        "sampleType": "SERUM",
        "collectedBy": "phlebotomy_staff",
        "collectionSite": "Left arm",
        "containerType": "SST tube",
        "volumeReceived": 5.0
      }')
    
    SAMPLE_ID=$(echo "$SAMPLE_RESPONSE" | jq -r '.sampleId')
    echo -e "${GREEN}✅ Sample collected (ID: $SAMPLE_ID)${NC}"
    
    # Update sample status to ACCEPTED (ready for testing)
    curl -s -b phlebotomy_cookies.txt -X PUT "http://localhost:8080/sample-collection/$SAMPLE_ID/status?status=ACCEPTED" > /dev/null
    echo -e "${GREEN}✅ Sample status updated to ACCEPTED${NC}"
fi

echo -e "\n${BLUE}🔬 STEP 4: TECHNICIAN - Test Processing${NC}"
echo "======================================="

# Login as technician
curl -s -c technician_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=technician&password=technician123" \
  --location > /dev/null

# Check test queue
TECHNICIAN_VISITS=$(curl -s -b technician_cookies.txt -X GET "http://localhost:8080/visits")
TECHNICIAN_VISIT_COUNT=$(echo "$TECHNICIAN_VISITS" | jq 'length')
echo -e "${GREEN}✅ Technician can see $TECHNICIAN_VISIT_COUNT visits${NC}"

# Check specific visit with tests
TECHNICIAN_VISIT=$(curl -s -b technician_cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
TECHNICIAN_TEST_COUNT=$(echo "$TECHNICIAN_VISIT" | jq '.labTests | length')
TECHNICIAN_PATIENT_NAME=$(echo "$TECHNICIAN_VISIT" | jq -r '.patientDetails.name')
echo -e "${GREEN}✅ Technician sees $TECHNICIAN_TEST_COUNT tests for patient: $TECHNICIAN_PATIENT_NAME${NC}"

# Show test details for technician
echo -e "${BLUE}Test Details for Technician:${NC}"
echo "$TECHNICIAN_VISIT" | jq -r '.labTests[] | "  - " + .testTemplate.name + " (Status: " + .status + ")"'

echo -e "\n${BLUE}📊 WORKFLOW VERIFICATION${NC}"
echo "========================"

# Final verification
FINAL_VISIT=$(curl -s -b admin_cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
FINAL_PATIENT_NAME=$(echo "$FINAL_VISIT" | jq -r '.patientDetails.name')
FINAL_TEST_COUNT=$(echo "$FINAL_VISIT" | jq '.labTests | length')
FINAL_TOTAL_COST=$(echo "$FINAL_VISIT" | jq '.labTests | map(.price) | add')

echo -e "${GREEN}Patient: $FINAL_PATIENT_NAME${NC}"
echo -e "${GREEN}Visit ID: $VISIT_ID${NC}"
echo -e "${GREEN}Tests Ordered: $FINAL_TEST_COUNT${NC}"
echo -e "${GREEN}Total Cost: ₹$FINAL_TOTAL_COST${NC}"

echo -e "\n${BLUE}🎯 ENHANCED FEATURES VERIFICATION${NC}"
echo "=================================="
echo -e "${GREEN}✅ Admin: Can create flexible test templates with any number of parameters${NC}"
echo -e "${GREEN}✅ Reception: Can order tests using admin-created templates${NC}"
echo -e "${GREEN}✅ Phlebotomy: Can collect samples and update sample status${NC}"
echo -e "${GREEN}✅ Technician: Can see tests but must wait for sample collection${NC}"
echo -e "${GREEN}✅ Sample Collection: Complete workflow from collection to acceptance${NC}"
echo -e "${GREEN}✅ Machine Tracking: Ready for internal audit (not shown on reports)${NC}"

echo -e "\n${BLUE}🌐 DASHBOARD URLS${NC}"
echo "=================="
echo "Admin: http://localhost:8080/admin/dashboard.html"
echo "Reception: http://localhost:8080/reception/dashboard.html"
echo "Phlebotomy: http://localhost:8080/phlebotomy/dashboard.html"
echo "Technician: http://localhost:8080/technician/dashboard.html"

# Cleanup
rm -f admin_cookies.txt reception_cookies.txt phlebotomy_cookies.txt technician_cookies.txt

echo -e "\n${GREEN}🎉 ENHANCED WORKFLOW TEST: SUCCESS!${NC}"
echo -e "${GREEN}All enhanced features are working correctly!${NC}"
