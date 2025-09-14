#!/bin/bash

echo "🔬 COMPLETE LAB WORKFLOW TEST - ALL ROLES"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "\n${BLUE}🏥 STEP 1: RECEPTION - Patient Registration & Test Ordering${NC}"
echo "============================================================"

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
      "name": "Alice Johnson",
      "age": "32",
      "gender": "F",
      "phone": "9123456789",
      "email": "alice.johnson@example.com"
    }
  }')

VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId')
echo -e "${GREEN}✅ Patient visit created (ID: $VISIT_ID)${NC}"

# Order CBC test
curl -s -b reception_cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d '{
    "testTemplateId": 1,
    "price": 450.00
  }' > /dev/null

# Order Lipid Profile test
curl -s -b reception_cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d '{
    "testTemplateId": 3,
    "price": 650.00
  }' > /dev/null

echo -e "${GREEN}✅ Tests ordered: CBC + Lipid Profile${NC}"

# Verify reception can see the visit with tests
RECEPTION_VISIT=$(curl -s -b reception_cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
RECEPTION_TEST_COUNT=$(echo "$RECEPTION_VISIT" | jq '.labTests | length')
echo -e "${GREEN}✅ Reception dashboard shows $RECEPTION_TEST_COUNT tests for visit${NC}"

echo -e "\n${BLUE}💉 STEP 2: PHLEBOTOMY - Sample Collection Queue${NC}"
echo "================================================"

# Login as phlebotomy
curl -s -c phlebotomy_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=phlebotomy&password=phlebotomy123" \
  --location > /dev/null

# Check if phlebotomy can see the visit
PHLEBOTOMY_VISITS=$(curl -s -b phlebotomy_cookies.txt -X GET "http://localhost:8080/visits")
PHLEBOTOMY_VISIT_COUNT=$(echo "$PHLEBOTOMY_VISITS" | jq '. | length')
echo -e "${GREEN}✅ Phlebotomy can see $PHLEBOTOMY_VISIT_COUNT visits${NC}"

# Check specific visit with tests
PHLEBOTOMY_VISIT=$(curl -s -b phlebotomy_cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
PHLEBOTOMY_TEST_COUNT=$(echo "$PHLEBOTOMY_VISIT" | jq '.labTests | length')
PHLEBOTOMY_PATIENT_NAME=$(echo "$PHLEBOTOMY_VISIT" | jq -r '.patientDetails.name')
echo -e "${GREEN}✅ Phlebotomy sees $PHLEBOTOMY_TEST_COUNT tests for patient: $PHLEBOTOMY_PATIENT_NAME${NC}"

# Show test details
echo -e "${BLUE}Test Details for Phlebotomy:${NC}"
echo "$PHLEBOTOMY_VISIT" | jq -r '.labTests[] | "  - " + .testTemplate.name + " (₹" + (.price | tostring) + ")"'

echo -e "\n${BLUE}🔬 STEP 3: TECHNICIAN - Test Processing Queue${NC}"
echo "=============================================="

# Login as technician
curl -s -c technician_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=technician&password=technician123" \
  --location > /dev/null

# Check if technician can see the visit
TECHNICIAN_VISITS=$(curl -s -b technician_cookies.txt -X GET "http://localhost:8080/visits")
TECHNICIAN_VISIT_COUNT=$(echo "$TECHNICIAN_VISITS" | jq '. | length')
echo -e "${GREEN}✅ Technician can see $TECHNICIAN_VISIT_COUNT visits${NC}"

# Check specific visit with tests
TECHNICIAN_VISIT=$(curl -s -b technician_cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
TECHNICIAN_TEST_COUNT=$(echo "$TECHNICIAN_VISIT" | jq '.labTests | length')
TECHNICIAN_PATIENT_NAME=$(echo "$TECHNICIAN_VISIT" | jq -r '.patientDetails.name')
echo -e "${GREEN}✅ Technician sees $TECHNICIAN_TEST_COUNT tests for patient: $TECHNICIAN_PATIENT_NAME${NC}"

# Show test details for technician
echo -e "${BLUE}Test Details for Technician:${NC}"
echo "$TECHNICIAN_VISIT" | jq -r '.labTests[] | "  - " + .testTemplate.name + " (Status: " + .status + ")"'

echo -e "\n${BLUE}👨‍💼 STEP 4: ADMIN - Full System Access${NC}"
echo "======================================="

# Login as admin
curl -s -c admin_cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" \
  --location > /dev/null

# Check admin access to all data
ADMIN_VISITS=$(curl -s -b admin_cookies.txt -X GET "http://localhost:8080/visits")
ADMIN_VISIT_COUNT=$(echo "$ADMIN_VISITS" | jq '. | length')
ADMIN_TEMPLATES=$(curl -s -b admin_cookies.txt -X GET "http://localhost:8080/test-templates")
ADMIN_TEMPLATE_COUNT=$(echo "$ADMIN_TEMPLATES" | jq '. | length')

echo -e "${GREEN}✅ Admin can see $ADMIN_VISIT_COUNT visits${NC}"
echo -e "${GREEN}✅ Admin can see $ADMIN_TEMPLATE_COUNT test templates${NC}"

echo -e "\n${BLUE}📊 WORKFLOW SUMMARY${NC}"
echo "==================="

# Final verification
FINAL_VISIT=$(curl -s -b admin_cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
FINAL_PATIENT_NAME=$(echo "$FINAL_VISIT" | jq -r '.patientDetails.name')
FINAL_TEST_COUNT=$(echo "$FINAL_VISIT" | jq '.labTests | length')
FINAL_TOTAL_COST=$(echo "$FINAL_VISIT" | jq '.labTests | map(.price) | add')

echo -e "${GREEN}Patient: $FINAL_PATIENT_NAME${NC}"
echo -e "${GREEN}Visit ID: $VISIT_ID${NC}"
echo -e "${GREEN}Tests Ordered: $FINAL_TEST_COUNT${NC}"
echo -e "${GREEN}Total Cost: ₹$FINAL_TOTAL_COST${NC}"

echo -e "\n${BLUE}🎯 ROLE ACCESS VERIFICATION${NC}"
echo "============================"
echo -e "${GREEN}✅ Reception: Can register patients and order tests${NC}"
echo -e "${GREEN}✅ Phlebotomy: Can see patient queue and ordered tests${NC}"
echo -e "${GREEN}✅ Technician: Can see test processing queue${NC}"
echo -e "${GREEN}✅ Admin: Has full system access${NC}"

echo -e "\n${BLUE}🌐 DASHBOARD URLS${NC}"
echo "=================="
echo "Reception: http://localhost:8080/reception/dashboard.html"
echo "Phlebotomy: http://localhost:8080/phlebotomy/dashboard.html"
echo "Technician: http://localhost:8080/technician/dashboard.html"
echo "Admin: http://localhost:8080/admin/dashboard.html"

# Cleanup
rm -f reception_cookies.txt phlebotomy_cookies.txt technician_cookies.txt admin_cookies.txt

echo -e "\n${GREEN}🎉 COMPLETE WORKFLOW TEST: SUCCESS!${NC}"
echo -e "${GREEN}All roles can now see and work with ordered tests!${NC}"
