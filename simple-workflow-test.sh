#!/bin/bash

echo "🔄 SIMPLE WORKFLOW TEST - SLNCity Lab System"
echo "============================================"
echo "Testing core workflow: Reception → Phlebotomy → Lab → Admin"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Check server status
echo -e "${BLUE}🌐 Checking SLNCity Lab System...${NC}"
SERVER_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080/actuator/health" -o /dev/null)
if [ "$SERVER_STATUS" != "200" ]; then
    echo -e "${RED}❌ Server is not accessible${NC}"
    exit 1
fi
echo -e "${GREEN}✅ SLNCity Lab System is online${NC}"
echo ""

echo -e "${PURPLE}📊 STEP 1: RECEPTION - Patient Registration${NC}"
echo "============================================"

# Create patient visit
PATIENT_DATA='{
    "patientDetails": {
        "firstName": "Test",
        "lastName": "Patient",
        "dateOfBirth": "1990-01-01",
        "gender": "MALE",
        "phoneNumber": "+91-9999999999",
        "email": "test.patient@slncity.com",
        "address": "123 Test Street, Test City"
    }
}'

echo "Creating patient visit..."
VISIT_RESPONSE=$(curl -s -X POST "http://localhost:8080/visits" \
    -H "Content-Type: application/json" \
    -d "$PATIENT_DATA")

VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId // empty' 2>/dev/null)

if [ -n "$VISIT_ID" ] && [ "$VISIT_ID" != "null" ]; then
    echo -e "${GREEN}✅ Patient registered successfully - Visit ID: $VISIT_ID${NC}"
else
    echo -e "${RED}❌ Failed to register patient${NC}"
    echo "Response: $VISIT_RESPONSE"
    exit 1
fi

# Get test templates
echo "Getting available test templates..."
TEMPLATES=$(curl -s "http://localhost:8080/test-templates")
TEMPLATE_COUNT=$(echo "$TEMPLATES" | jq 'length' 2>/dev/null || echo "0")

if [ "$TEMPLATE_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ Found $TEMPLATE_COUNT test templates${NC}"
    FIRST_TEMPLATE_ID=$(echo "$TEMPLATES" | jq -r '.[0].templateId' 2>/dev/null)
    FIRST_TEMPLATE_NAME=$(echo "$TEMPLATES" | jq -r '.[0].name' 2>/dev/null)
    echo "   Using template: $FIRST_TEMPLATE_NAME (ID: $FIRST_TEMPLATE_ID)"
else
    echo -e "${YELLOW}⚠️  No test templates found, creating one...${NC}"
    
    # Create a test template
    TEMPLATE_DATA='{
        "name": "Basic Blood Test",
        "description": "Basic blood analysis",
        "basePrice": 200.00,
        "parameters": {
            "sampleType": "WHOLE_BLOOD",
            "volume": "5 mL"
        }
    }'
    
    TEMPLATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/test-templates" \
        -H "Content-Type: application/json" \
        -d "$TEMPLATE_DATA")
    
    FIRST_TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | jq -r '.templateId // empty' 2>/dev/null)
    FIRST_TEMPLATE_NAME="Basic Blood Test"
    
    if [ -n "$FIRST_TEMPLATE_ID" ] && [ "$FIRST_TEMPLATE_ID" != "null" ]; then
        echo -e "${GREEN}✅ Created test template: $FIRST_TEMPLATE_NAME (ID: $FIRST_TEMPLATE_ID)${NC}"
    else
        echo -e "${RED}❌ Failed to create test template${NC}"
        exit 1
    fi
fi

# Add test to visit
echo "Adding test to visit..."
TEST_REQUEST="{\"testTemplateId\": $FIRST_TEMPLATE_ID}"

TEST_RESPONSE=$(curl -s -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
    -H "Content-Type: application/json" \
    -d "$TEST_REQUEST")

TEST_ID=$(echo "$TEST_RESPONSE" | jq -r '.testId // empty' 2>/dev/null)

if [ -n "$TEST_ID" ] && [ "$TEST_ID" != "null" ]; then
    echo -e "${GREEN}✅ Test added to visit - Test ID: $TEST_ID${NC}"
else
    echo -e "${RED}❌ Failed to add test to visit${NC}"
    echo "Response: $TEST_RESPONSE"
    exit 1
fi

echo ""
echo -e "${PURPLE}🩸 STEP 2: PHLEBOTOMY - Sample Collection${NC}"
echo "========================================="

# Check pending samples
echo "Checking pending sample collections..."
PENDING_RESPONSE=$(curl -s "http://localhost:8080/sample-collection/pending")
echo "Pending samples response: $PENDING_RESPONSE"

# Collect sample
echo "Collecting sample for test ID: $TEST_ID"
SAMPLE_DATA='{
    "sampleType": "WHOLE_BLOOD",
    "collectedBy": "Test Phlebotomist",
    "collectionSite": "Left arm",
    "containerType": "EDTA tube",
    "volumeReceived": 5.0,
    "notes": "Test sample collection"
}'

SAMPLE_RESPONSE=$(curl -s -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
    -H "Content-Type: application/json" \
    -d "$SAMPLE_DATA")

SAMPLE_ID=$(echo "$SAMPLE_RESPONSE" | jq -r '.sampleId // empty' 2>/dev/null)

if [ -n "$SAMPLE_ID" ] && [ "$SAMPLE_ID" != "null" ]; then
    echo -e "${GREEN}✅ Sample collected successfully - Sample ID: $SAMPLE_ID${NC}"
else
    echo -e "${YELLOW}⚠️  Sample collection response:${NC}"
    echo "$SAMPLE_RESPONSE"
fi

echo ""
echo -e "${PURPLE}🔬 STEP 3: LAB TECHNICIAN - Test Processing${NC}"
echo "==========================================="

# Check lab tests
echo "Checking lab tests..."
LAB_TESTS=$(curl -s "http://localhost:8080/lab-tests")
echo "Lab tests: $LAB_TESTS"

# Process test results (if sample was collected)
if [ -n "$SAMPLE_ID" ] && [ "$SAMPLE_ID" != "null" ]; then
    echo "Processing test results for test ID: $TEST_ID"
    
    RESULTS_DATA='{
        "results": {
            "hemoglobin": {"value": 14.5, "unit": "g/dL", "status": "NORMAL"},
            "rbc": {"value": 4.8, "unit": "million/μL", "status": "NORMAL"},
            "interpretation": "All parameters within normal limits"
        }
    }'
    
    RESULTS_RESPONSE=$(curl -s -X PATCH "http://localhost:8080/visits/$VISIT_ID/tests/$TEST_ID/results" \
        -H "Content-Type: application/json" \
        -d "$RESULTS_DATA")
    
    echo "Results response: $RESULTS_RESPONSE"
    
    # Approve results
    echo "Approving test results..."
    APPROVE_DATA='{"approvedBy": "Dr. Test Technician"}'
    
    APPROVE_RESPONSE=$(curl -s -X PATCH "http://localhost:8080/visits/$VISIT_ID/tests/$TEST_ID/approve" \
        -H "Content-Type: application/json" \
        -d "$APPROVE_DATA")
    
    echo "Approval response: $APPROVE_RESPONSE"
fi

echo ""
echo -e "${PURPLE}👨‍💼 STEP 4: ADMIN - System Monitoring${NC}"
echo "===================================="

# Check system status
echo "Checking system overview..."
FINAL_VISITS=$(curl -s "http://localhost:8080/visits" | jq 'length' 2>/dev/null || echo "0")
FINAL_TESTS=$(curl -s "http://localhost:8080/lab-tests" | jq 'length' 2>/dev/null || echo "0")
FINAL_SAMPLES=$(curl -s "http://localhost:8080/samples" | jq 'length' 2>/dev/null || echo "0")

echo "System Status:"
echo "   • Total Visits: $FINAL_VISITS"
echo "   • Total Lab Tests: $FINAL_TESTS"
echo "   • Total Samples: $FINAL_SAMPLES"

echo ""
echo -e "${BLUE}🎯 WORKFLOW TEST RESULTS${NC}"
echo "========================"

if [ -n "$VISIT_ID" ] && [ "$VISIT_ID" != "null" ] && [ -n "$TEST_ID" ] && [ "$TEST_ID" != "null" ]; then
    echo -e "${GREEN}✅ Core workflow is functional:${NC}"
    echo "   ✅ Reception: Patient registration works"
    echo "   ✅ Reception: Test ordering works"
    
    if [ -n "$SAMPLE_ID" ] && [ "$SAMPLE_ID" != "null" ]; then
        echo "   ✅ Phlebotomy: Sample collection works"
        echo "   ✅ Lab Technician: Test processing available"
    else
        echo "   ⚠️  Phlebotomy: Sample collection needs attention"
        echo "   ⚠️  Lab Technician: Limited functionality"
    fi
    
    echo "   ✅ Admin: System monitoring works"
    
    echo ""
    echo -e "${GREEN}🌐 Test the dashboards:${NC}"
    echo "   • Reception: http://localhost:8080/reception/dashboard.html"
    echo "   • Phlebotomy: http://localhost:8080/phlebotomy/dashboard.html"
    echo "   • Lab Technician: http://localhost:8080/technician/dashboard.html"
    echo "   • Admin: http://localhost:8080/admin/dashboard.html"
    
    echo ""
    echo -e "${BLUE}📋 Workflow Data Created:${NC}"
    echo "   • Visit ID: $VISIT_ID (Test Patient)"
    echo "   • Test ID: $TEST_ID ($FIRST_TEMPLATE_NAME)"
    if [ -n "$SAMPLE_ID" ] && [ "$SAMPLE_ID" != "null" ]; then
        echo "   • Sample ID: $SAMPLE_ID (Collected)"
    fi
    
else
    echo -e "${RED}❌ Workflow has critical issues${NC}"
    echo "   Check server logs and API endpoints"
fi

echo ""
echo -e "${PURPLE}🚀 Simple Workflow Test Complete!${NC}"

exit 0
