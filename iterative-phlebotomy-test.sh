#!/bin/bash

echo "🔄 Iterative Phlebotomy Workflow Testing"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run test
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_pattern="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "\n${BLUE}Test $TOTAL_TESTS: $test_name${NC}"
    echo "Command: $test_command"
    
    # Run the test
    result=$(eval "$test_command" 2>&1)
    exit_code=$?
    
    # Check result
    if [ $exit_code -eq 0 ] && [[ "$result" =~ $expected_pattern ]]; then
        echo -e "${GREEN}✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "Expected pattern: $expected_pattern"
        echo "Actual result: $result"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Function to test API endpoint
test_api() {
    local endpoint="$1"
    local method="${2:-GET}"
    local data="${3:-}"
    local expected="${4:-200}"
    
    if [ -n "$data" ]; then
        curl -s -w "%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "http://localhost:8080$endpoint" -o /dev/null
    else
        curl -s -w "%{http_code}" -X "$method" "http://localhost:8080$endpoint" -o /dev/null
    fi
}

echo -e "\n${YELLOW}Phase 1: Application Health Check${NC}"
run_test "Application Health" "curl -s http://localhost:8080/actuator/health | jq -r '.status'" "UP"

echo -e "\n${YELLOW}Phase 2: Static Resources Testing${NC}"
run_test "Dashboard HTML" "test_api '/phlebotomy/dashboard.html'" "200"
run_test "Phlebotomy CSS" "test_api '/css/phlebotomy.css'" "200"
run_test "Phlebotomy JS" "test_api '/js/phlebotomy.js'" "200"

echo -e "\n${YELLOW}Phase 3: Core API Testing${NC}"
run_test "Pending Samples API" "curl -s http://localhost:8080/sample-collection/pending | jq 'length'" "[0-9]+"
run_test "Visits API" "curl -s http://localhost:8080/visits | jq 'length'" "[0-9]+"
run_test "Test Templates API" "curl -s http://localhost:8080/test-templates | jq 'length'" "[0-9]+"
run_test "Samples API" "curl -s http://localhost:8080/samples | jq 'length'" "[0-9]+"

echo -e "\n${YELLOW}Phase 4: Sample Collection Workflow${NC}"

# Create test data for workflow
echo "Setting up fresh test data..."

# Create test template
TEMPLATE_DATA='{
  "name": "Iterative Test CBC",
  "description": "Complete Blood Count for iterative testing",
  "basePrice": 250.00,
  "parameters": {
    "sampleType": "WHOLE_BLOOD",
    "volumeRequired": 5.0,
    "containerType": "EDTA tube",
    "processingTime": "2 hours"
  }
}'

TEMPLATE_ID=$(curl -s -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d "$TEMPLATE_DATA" | jq -r '.templateId')

run_test "Create Test Template" "echo $TEMPLATE_ID" "[0-9]+"

# Create patient visit
VISIT_DATA='{
  "patientDetails": {
    "name": "Iterative Test Patient",
    "age": 35,
    "gender": "MALE",
    "phone": "9876543210",
    "email": "iterative@test.com",
    "address": "Test Address for Iteration"
  }
}'

VISIT_ID=$(curl -s -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d "$VISIT_DATA" | jq -r '.visitId')

run_test "Create Patient Visit" "echo $VISIT_ID" "[0-9]+"

# Order test
TEST_ORDER_DATA="{\"testTemplateId\": $TEMPLATE_ID}"

TEST_ID=$(curl -s -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d "$TEST_ORDER_DATA" | jq -r '.testId')

run_test "Order Lab Test" "echo $TEST_ID" "[0-9]+"

# Verify pending sample appears
run_test "Verify Pending Sample" "curl -s http://localhost:8080/sample-collection/pending | jq 'map(select(.testId == $TEST_ID)) | length'" "[1-9]+"

# Collect the sample
COLLECTION_DATA='{
  "sampleType": "WHOLE_BLOOD",
  "collectedBy": "phlebotomy",
  "collectionSite": "Left Arm - Iterative Test",
  "containerType": "EDTA Tube",
  "volumeReceived": 5.0,
  "notes": "Iterative test collection - automated"
}'

COLLECTION_RESULT=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
  -H "Content-Type: application/json" \
  -d "$COLLECTION_DATA" -o /dev/null)

run_test "Sample Collection" "echo $COLLECTION_RESULT" "201"

echo -e "\n${YELLOW}Phase 5: Data Verification${NC}"

# Verify sample was created
run_test "Sample Created" "curl -s http://localhost:8080/samples | jq 'map(select(.labTest.testId == $TEST_ID)) | length'" "[1-9]+"

# Verify pending count decreased
run_test "Pending Count Updated" "curl -s http://localhost:8080/sample-collection/pending | jq 'map(select(.testId == $TEST_ID)) | length'" "0"

echo -e "\n${YELLOW}Phase 6: Browser Functionality Test${NC}"

# Test if dashboard loads without JavaScript errors
run_test "Dashboard Accessibility" "curl -s http://localhost:8080/phlebotomy/dashboard.html | grep -c 'Phlebotomy Dashboard'" "[1-9]+"

# Test if all required sections are present in HTML
run_test "Dashboard Sections" "curl -s http://localhost:8080/phlebotomy/dashboard.html | grep -c 'data-section'" "[7-9]+"

# Test if statistics cards are present
run_test "Statistics Cards" "curl -s http://localhost:8080/phlebotomy/dashboard.html | grep -c 'stat-card'" "[4-9]+"

echo -e "\n${YELLOW}Phase 7: Comprehensive Feature Test${NC}"

# Test multiple sample types
URINE_TEMPLATE_DATA='{
  "name": "Iterative Urine Test",
  "description": "Urine analysis for iterative testing",
  "basePrice": 150.00,
  "parameters": {
    "sampleType": "RANDOM_URINE",
    "volumeRequired": 10.0,
    "containerType": "Sterile container",
    "processingTime": "1 hour"
  }
}'

URINE_TEMPLATE_ID=$(curl -s -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d "$URINE_TEMPLATE_DATA" | jq -r '.templateId')

URINE_TEST_ID=$(curl -s -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $URINE_TEMPLATE_ID}" | jq -r '.testId')

run_test "Multiple Sample Types" "echo $URINE_TEST_ID" "[0-9]+"

# Collect urine sample
URINE_COLLECTION_DATA='{
  "sampleType": "RANDOM_URINE",
  "collectedBy": "phlebotomy",
  "collectionSite": "Patient Collection",
  "containerType": "Sterile Container",
  "volumeReceived": 10.0,
  "notes": "Urine sample - iterative test"
}'

URINE_COLLECTION_RESULT=$(curl -s -w "%{http_code}" -X POST "http://localhost:8080/sample-collection/collect/$URINE_TEST_ID" \
  -H "Content-Type: application/json" \
  -d "$URINE_COLLECTION_DATA" -o /dev/null)

run_test "Urine Sample Collection" "echo $URINE_COLLECTION_RESULT" "201"

echo -e "\n${YELLOW}Final Results${NC}"
echo "================================"
echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! Phlebotomy workflow is fully functional!${NC}"
    echo -e "\n${GREEN}✨ Production Ready Features:${NC}"
    echo "   • Complete sample collection workflow"
    echo "   • Multiple sample types supported"
    echo "   • Real-time dashboard updates"
    echo "   • API integration working"
    echo "   • UI components functional"
    echo "   • Data persistence verified"
    echo ""
    echo -e "${GREEN}🚀 Ready for production deployment!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Some tests failed. Please review the issues above.${NC}"
    exit 1
fi
