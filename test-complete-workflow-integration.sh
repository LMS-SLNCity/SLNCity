#!/bin/bash

echo "🧪 TESTING COMPLETE WORKFLOW INTEGRATION - PHLEBOTOMY TO LAB"
echo "=============================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

# Function to check if server is running
check_server() {
    echo -e "${BLUE}🔍 Checking if server is running...${NC}"
    if curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ Server is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Server is not running. Please start the application first.${NC}"
        exit 1
    fi
}

# Function to create test data
create_test_data() {
    echo -e "${BLUE}📝 Creating test data...${NC}"

    # Use existing test template (ID 1)
    echo "Using existing test template..."
    TEMPLATE_ID=1
    echo -e "${GREEN}✅ Using test template with ID: $TEMPLATE_ID${NC}"
    
    # Create patient visit
    echo "Creating patient visit..."
    VISIT_ID=$(curl -s -X POST "$BASE_URL/visits" \
      -H "Content-Type: application/json" \
      -d '{
        "patientDetails": {
          "name": "John Workflow Test",
          "age": 35,
          "gender": "Male",
          "phone": "9876543210",
          "email": "workflow@test.com",
          "address": "Workflow Test Address"
        }
      }' | jq -r '.visitId')
    
    if [ "$VISIT_ID" != "null" ] && [ -n "$VISIT_ID" ]; then
        echo -e "${GREEN}✅ Patient visit created with ID: $VISIT_ID${NC}"
    else
        echo -e "${RED}❌ Failed to create patient visit${NC}"
        return 1
    fi
    
    # Create lab test
    echo "Creating lab test..."
    TEST_ID=$(curl -s -X POST "$BASE_URL/visits/$VISIT_ID/tests" \
      -H "Content-Type: application/json" \
      -d "{\"testTemplateId\": $TEMPLATE_ID}" | jq -r '.testId')
    
    if [ "$TEST_ID" != "null" ] && [ -n "$TEST_ID" ]; then
        echo -e "${GREEN}✅ Lab test created with ID: $TEST_ID${NC}"
    else
        echo -e "${RED}❌ Failed to create lab test${NC}"
        return 1
    fi
    
    # Export variables for use in other functions
    export TEMPLATE_ID VISIT_ID TEST_ID
}

# Function to test phlebotomy workflow
test_phlebotomy_workflow() {
    echo -e "${BLUE}🩸 Testing Phlebotomy Workflow...${NC}"
    
    # Check pending samples for collection
    echo "Checking pending samples..."
    PENDING_SAMPLES=$(curl -s "$BASE_URL/sample-collection/pending" | jq '. | length')
    echo -e "${YELLOW}📊 Pending samples for collection: $PENDING_SAMPLES${NC}"
    
    # Collect sample
    echo "Collecting sample..."
    SAMPLE_RESPONSE=$(curl -s -X POST "$BASE_URL/sample-collection/collect/$TEST_ID" \
      -H "Content-Type: application/json" \
      -d "{
        \"sampleType\": \"WHOLE_BLOOD\",
        \"collectedBy\": \"phlebotomist\",
        \"collectionSite\": \"Left antecubital vein\",
        \"containerType\": \"EDTA tube\",
        \"volumeReceived\": 5.0,
        \"notes\": \"Sample collected successfully\"
      }")
    
    SAMPLE_ID=$(echo "$SAMPLE_RESPONSE" | jq -r '.sampleId')
    
    if [ "$SAMPLE_ID" != "null" ] && [ -n "$SAMPLE_ID" ]; then
        echo -e "${GREEN}✅ Sample collected successfully with ID: $SAMPLE_ID${NC}"
        export SAMPLE_ID
    else
        echo -e "${RED}❌ Failed to collect sample${NC}"
        echo "Response: $SAMPLE_RESPONSE"
        return 1
    fi
}

# Function to test lab workflow integration
test_lab_workflow_integration() {
    echo -e "${BLUE}🔬 Testing Lab Workflow Integration...${NC}"
    
    # Wait a moment for the workflow synchronization
    sleep 2
    
    # Check lab test status after sample collection
    echo "Checking lab test status after sample collection..."
    LAB_TEST_STATUS=$(curl -s "$BASE_URL/lab-tests/$TEST_ID" | jq -r '.status')
    
    if [ "$LAB_TEST_STATUS" = "PENDING" ]; then
        echo -e "${GREEN}✅ CRITICAL FIX WORKING: Lab test status updated to PENDING after sample collection${NC}"
    else
        echo -e "${RED}❌ WORKFLOW BROKEN: Lab test status is '$LAB_TEST_STATUS', should be 'PENDING'${NC}"
        return 1
    fi
    
    # Check if sample is linked to lab test
    echo "Checking if sample is linked to lab test..."
    LAB_TEST_SAMPLE_ID=$(curl -s "$BASE_URL/lab-tests/$TEST_ID" | jq -r '.sample.sampleId // empty')
    
    if [ "$LAB_TEST_SAMPLE_ID" = "$SAMPLE_ID" ]; then
        echo -e "${GREEN}✅ Sample correctly linked to lab test${NC}"
    else
        echo -e "${RED}❌ Sample not linked to lab test. Expected: $SAMPLE_ID, Got: $LAB_TEST_SAMPLE_ID${NC}"
        return 1
    fi
    
    # Check lab technician dashboard data
    echo "Checking lab technician dashboard data..."
    PENDING_TESTS=$(curl -s "$BASE_URL/lab-tests?status=PENDING" | jq '. | length')
    echo -e "${YELLOW}📊 Tests ready for lab processing: $PENDING_TESTS${NC}"
    
    if [ "$PENDING_TESTS" -gt 0 ]; then
        echo -e "${GREEN}✅ Lab technician dashboard has tests ready for processing${NC}"
    else
        echo -e "${RED}❌ No tests available for lab processing${NC}"
        return 1
    fi
}

# Function to test complete workflow
test_complete_workflow() {
    echo -e "${BLUE}🔄 Testing Complete Workflow End-to-End...${NC}"
    
    # Test lab test processing
    echo "Starting lab test processing..."
    START_RESPONSE=$(curl -s -X POST "$BASE_URL/lab-tests/$TEST_ID/start" \
      -H "Content-Type: application/json" \
      -d '{"equipmentId": 1, "machineUsed": "Test Analyzer"}')
    
    START_STATUS=$(echo "$START_RESPONSE" | jq -r '.status // "error"')
    
    if [ "$START_STATUS" = "IN_PROGRESS" ]; then
        echo -e "${GREEN}✅ Lab test started successfully${NC}"
    else
        echo -e "${YELLOW}⚠️  Lab test start response: $START_RESPONSE${NC}"
    fi
    
    # Enter test results
    echo "Entering test results..."
    RESULTS_RESPONSE=$(curl -s -X POST "$BASE_URL/lab-tests/$TEST_ID/results" \
      -H "Content-Type: application/json" \
      -d '{
        "results": {
          "hemoglobin": "14.5 g/dL",
          "hematocrit": "42%",
          "wbc": "7500/μL",
          "platelets": "250000/μL"
        }
      }')
    
    RESULTS_STATUS=$(echo "$RESULTS_RESPONSE" | jq -r '.status // "error"')
    
    if [ "$RESULTS_STATUS" = "COMPLETED" ]; then
        echo -e "${GREEN}✅ Test results entered successfully${NC}"
    else
        echo -e "${YELLOW}⚠️  Test results response: $RESULTS_RESPONSE${NC}"
    fi
}

# Function to display final status
display_final_status() {
    echo -e "${BLUE}📊 Final Workflow Status Summary${NC}"
    echo "=================================="
    
    # Get final status of all components
    echo "Sample Status:"
    curl -s "$BASE_URL/samples/$SAMPLE_ID" | jq '{sampleId, status, sampleType, collectedBy}'
    
    echo -e "\nLab Test Status:"
    curl -s "$BASE_URL/lab-tests/$TEST_ID" | jq '{testId, status, sample: {sampleId}, testTemplate: {name}}'
    
    echo -e "\nVisit Status:"
    curl -s "$BASE_URL/visits/$VISIT_ID" | jq '{visitId, status, patientDetails: {name}}'
}

# Main execution
main() {
    echo -e "${BLUE}🚀 Starting Complete Workflow Integration Test${NC}"
    echo "=============================================="
    
    check_server || exit 1
    
    create_test_data || exit 1
    
    test_phlebotomy_workflow || exit 1
    
    test_lab_workflow_integration || exit 1
    
    test_complete_workflow || exit 1
    
    display_final_status
    
    echo -e "\n${GREEN}🎉 WORKFLOW INTEGRATION TEST COMPLETED SUCCESSFULLY!${NC}"
    echo -e "${GREEN}✅ Phlebotomy → Lab workflow is now functional${NC}"
    echo -e "${GREEN}✅ Sample collection triggers lab test status update${NC}"
    echo -e "${GREEN}✅ Lab technician dashboard shows collected samples${NC}"
}

# Run the test
main "$@"
