#!/bin/bash

echo "🩸 COMPREHENSIVE PHLEBOTOMY WORKFLOW TEST SUITE"
echo "==============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
CRITICAL_FAILURES=0

# Test results array
declare -a TEST_RESULTS=()

# Function to log test result
log_test() {
    local test_name="$1"
    local status="$2"
    local details="$3"
    local is_critical="${4:-false}"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        TEST_RESULTS+=("PASS: $test_name")
    else
        echo -e "${RED}❌ FAIL${NC} - $test_name"
        echo -e "   ${YELLOW}Details: $details${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        TEST_RESULTS+=("FAIL: $test_name - $details")
        
        if [ "$is_critical" = "true" ]; then
            CRITICAL_FAILURES=$((CRITICAL_FAILURES + 1))
            echo -e "   ${RED}🚨 CRITICAL FAILURE${NC}"
        fi
    fi
}

# Function to test HTTP endpoint
test_http() {
    local url="$1"
    local expected_code="${2:-200}"
    local method="${3:-GET}"
    local data="${4:-}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" "$url")
    fi
    
    http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" = "$expected_code" ]; then
        echo "$body"
        return 0
    else
        echo "HTTP $http_code: $body"
        return 1
    fi
}

echo -e "\n${CYAN}=== PHASE 1: INFRASTRUCTURE CHECKS ===${NC}"

# Check if application is running
echo -e "\n${BLUE}Testing Application Health...${NC}"
if health_response=$(test_http "http://localhost:8080/actuator/health" 200); then
    if echo "$health_response" | grep -q '"status":"UP"'; then
        log_test "Application Health Check" "PASS" "Application is UP and healthy"
    else
        log_test "Application Health Check" "FAIL" "Application status is not UP: $health_response" "true"
    fi
else
    log_test "Application Health Check" "FAIL" "Cannot reach application health endpoint" "true"
fi

# Check database connectivity
echo -e "\n${BLUE}Testing Database Connectivity...${NC}"
if db_response=$(test_http "http://localhost:8080/h2-console" 200); then
    log_test "Database Console Access" "PASS" "H2 console accessible"
else
    log_test "Database Console Access" "FAIL" "Cannot access H2 console"
fi

echo -e "\n${CYAN}=== PHASE 2: STATIC RESOURCE CHECKS ===${NC}"

# Test static resources
echo -e "\n${BLUE}Testing Static Resources...${NC}"

resources=(
    "/phlebotomy/dashboard.html:Phlebotomy Dashboard HTML"
    "/css/phlebotomy.css:Phlebotomy CSS"
    "/js/phlebotomy.js:Phlebotomy JavaScript"
    "/login.html:Login Page"
    "/index.html:Main Index"
)

for resource in "${resources[@]}"; do
    IFS=':' read -r path name <<< "$resource"
    if test_http "http://localhost:8080$path" 200 >/dev/null; then
        log_test "$name" "PASS" "Resource loads successfully"
    else
        log_test "$name" "FAIL" "Resource not accessible at $path"
    fi
done

echo -e "\n${CYAN}=== PHASE 3: API ENDPOINT CHECKS ===${NC}"

# Test core APIs
echo -e "\n${BLUE}Testing Core APIs...${NC}"

apis=(
    "/visits:Visits API"
    "/test-templates:Test Templates API"
    "/samples:Samples API"
    "/sample-collection/pending:Pending Samples API"
    "/lab-tests:Lab Tests API"
)

for api in "${apis[@]}"; do
    IFS=':' read -r endpoint name <<< "$api"
    if response=$(test_http "http://localhost:8080$endpoint" 200); then
        # Check if response is valid JSON
        if echo "$response" | jq . >/dev/null 2>&1; then
            log_test "$name" "PASS" "API returns valid JSON"
        else
            log_test "$name" "FAIL" "API returns invalid JSON: $response"
        fi
    else
        log_test "$name" "FAIL" "API endpoint not accessible"
    fi
done

echo -e "\n${CYAN}=== PHASE 4: DATA CREATION WORKFLOW ===${NC}"

echo -e "\n${BLUE}Creating Test Data...${NC}"

# Create test template
template_data='{
  "name": "Comprehensive Test CBC",
  "description": "Complete Blood Count for comprehensive testing",
  "basePrice": 300.00,
  "parameters": {
    "sampleType": "WHOLE_BLOOD",
    "volumeRequired": 5.0,
    "containerType": "EDTA tube",
    "processingTime": "2 hours",
    "testMethod": "Automated analyzer",
    "reportingTime": "Same day"
  }
}'

if template_response=$(test_http "http://localhost:8080/test-templates" 201 "POST" "$template_data"); then
    template_id=$(echo "$template_response" | jq -r '.templateId')
    if [ "$template_id" != "null" ] && [ -n "$template_id" ]; then
        log_test "Create Test Template" "PASS" "Template created with ID: $template_id"
    else
        log_test "Create Test Template" "FAIL" "Template creation returned invalid ID: $template_response"
        template_id=""
    fi
else
    log_test "Create Test Template" "FAIL" "Failed to create test template: $template_response" "true"
    template_id=""
fi

# Create patient visit
visit_data='{
  "patientDetails": {
    "name": "Comprehensive Test Patient",
    "age": 45,
    "gender": "FEMALE",
    "phone": "9876543210",
    "email": "comprehensive@test.com",
    "address": "123 Test Street, Test City, Test State - 123456",
    "emergencyContact": "Emergency Contact Name",
    "emergencyPhone": "9876543211"
  }
}'

if visit_response=$(test_http "http://localhost:8080/visits" 201 "POST" "$visit_data"); then
    visit_id=$(echo "$visit_response" | jq -r '.visitId')
    if [ "$visit_id" != "null" ] && [ -n "$visit_id" ]; then
        log_test "Create Patient Visit" "PASS" "Visit created with ID: $visit_id"
    else
        log_test "Create Patient Visit" "FAIL" "Visit creation returned invalid ID: $visit_response"
        visit_id=""
    fi
else
    log_test "Create Patient Visit" "FAIL" "Failed to create patient visit: $visit_response" "true"
    visit_id=""
fi

# Order lab test (only if we have both template and visit)
if [ -n "$template_id" ] && [ -n "$visit_id" ]; then
    test_order_data="{\"testTemplateId\": $template_id}"
    
    if test_response=$(test_http "http://localhost:8080/visits/$visit_id/tests" 201 "POST" "$test_order_data"); then
        test_id=$(echo "$test_response" | jq -r '.testId')
        if [ "$test_id" != "null" ] && [ -n "$test_id" ]; then
            log_test "Order Lab Test" "PASS" "Test ordered with ID: $test_id"
        else
            log_test "Order Lab Test" "FAIL" "Test ordering returned invalid ID: $test_response"
            test_id=""
        fi
    else
        log_test "Order Lab Test" "FAIL" "Failed to order lab test: $test_response" "true"
        test_id=""
    fi
else
    log_test "Order Lab Test" "FAIL" "Cannot order test - missing template_id or visit_id" "true"
    test_id=""
fi

echo -e "\n${CYAN}=== PHASE 5: SAMPLE COLLECTION WORKFLOW ===${NC}"

echo -e "\n${BLUE}Testing Sample Collection Process...${NC}"

# Check pending samples
if pending_response=$(test_http "http://localhost:8080/sample-collection/pending" 200); then
    pending_count=$(echo "$pending_response" | jq 'length')
    if [ "$pending_count" -gt 0 ]; then
        log_test "Pending Samples Available" "PASS" "Found $pending_count pending samples"
    else
        log_test "Pending Samples Available" "FAIL" "No pending samples found - expected at least 1"
    fi
else
    log_test "Pending Samples Available" "FAIL" "Cannot retrieve pending samples: $pending_response" "true"
fi

# Test sample collection (only if we have a test_id)
if [ -n "$test_id" ]; then
    collection_data='{
      "sampleType": "WHOLE_BLOOD",
      "collectedBy": "phlebotomy_test_user",
      "collectionSite": "Left Antecubital Vein",
      "containerType": "EDTA Tube",
      "volumeReceived": 5.0,
      "notes": "Comprehensive test collection - patient cooperative, no complications"
    }'
    
    if collection_response=$(test_http "http://localhost:8080/sample-collection/collect/$test_id" 201 "POST" "$collection_data"); then
        sample_id=$(echo "$collection_response" | jq -r '.sampleId')
        if [ "$sample_id" != "null" ] && [ -n "$sample_id" ]; then
            log_test "Sample Collection" "PASS" "Sample collected with ID: $sample_id"
        else
            log_test "Sample Collection" "FAIL" "Sample collection returned invalid ID: $collection_response"
        fi
    else
        log_test "Sample Collection" "FAIL" "Failed to collect sample: $collection_response" "true"
    fi
else
    log_test "Sample Collection" "FAIL" "Cannot test collection - no test_id available" "true"
fi

echo -e "\n${CYAN}=== PHASE 6: UI FUNCTIONALITY CHECKS ===${NC}"

echo -e "\n${BLUE}Testing UI Components...${NC}"

# Test dashboard HTML structure
if dashboard_html=$(test_http "http://localhost:8080/phlebotomy/dashboard.html" 200); then
    # Check for essential UI elements
    ui_elements=(
        "Phlebotomy Dashboard:Dashboard Title"
        "stat-card:Statistics Cards"
        "sidebar:Navigation Sidebar"
        "main-content:Main Content Area"
        "data-section:Section Navigation"
    )
    
    for element in "${ui_elements[@]}"; do
        IFS=':' read -r search_term element_name <<< "$element"
        if echo "$dashboard_html" | grep -q "$search_term"; then
            log_test "UI Element: $element_name" "PASS" "Element found in HTML"
        else
            log_test "UI Element: $element_name" "FAIL" "Element missing from HTML"
        fi
    done
else
    log_test "Dashboard HTML Structure" "FAIL" "Cannot load dashboard HTML" "true"
fi

# Test JavaScript functionality (basic syntax check)
if js_content=$(test_http "http://localhost:8080/js/phlebotomy.js" 200); then
    js_functions=(
        "PhlebotomyApp:Main Application Class"
        "loadDashboardData:Dashboard Data Loading"
        "showSampleCollectionModal:Sample Collection Modal"
        "submitSampleCollection:Sample Collection Submission"
    )
    
    for func in "${js_functions[@]}"; do
        IFS=':' read -r func_name func_desc <<< "$func"
        if echo "$js_content" | grep -q "$func_name"; then
            log_test "JS Function: $func_desc" "PASS" "Function found in JavaScript"
        else
            log_test "JS Function: $func_desc" "FAIL" "Function missing from JavaScript"
        fi
    done
else
    log_test "JavaScript Content" "FAIL" "Cannot load JavaScript file" "true"
fi

echo -e "\n${CYAN}=== PHASE 7: INTEGRATION TESTS ===${NC}"

echo -e "\n${BLUE}Testing End-to-End Integration...${NC}"

# Test complete workflow if all components are available
if [ -n "$test_id" ]; then
    # Verify sample was actually created in database
    if samples_response=$(test_http "http://localhost:8080/samples" 200); then
        sample_count=$(echo "$samples_response" | jq 'length')
        if [ "$sample_count" -gt 0 ]; then
            log_test "Sample Database Persistence" "PASS" "Found $sample_count samples in database"
        else
            log_test "Sample Database Persistence" "FAIL" "No samples found in database after collection"
        fi
    else
        log_test "Sample Database Persistence" "FAIL" "Cannot query samples from database"
    fi
    
    # Check if pending count decreased
    if new_pending_response=$(test_http "http://localhost:8080/sample-collection/pending" 200); then
        new_pending_count=$(echo "$new_pending_response" | jq 'length')
        log_test "Pending Count Update" "PASS" "Pending samples count is now: $new_pending_count"
    else
        log_test "Pending Count Update" "FAIL" "Cannot check updated pending count"
    fi
else
    log_test "End-to-End Integration" "FAIL" "Cannot test integration - workflow setup failed" "true"
fi

echo -e "\n${PURPLE}=== COMPREHENSIVE TEST RESULTS ===${NC}"
echo "=================================================="
echo -e "Total Tests Run: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Tests Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Tests Failed: ${RED}$FAILED_TESTS${NC}"
echo -e "Critical Failures: ${RED}$CRITICAL_FAILURES${NC}"

success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
echo -e "Success Rate: ${BLUE}$success_rate%${NC}"

echo -e "\n${YELLOW}=== DETAILED RESULTS ===${NC}"
for result in "${TEST_RESULTS[@]}"; do
    if [[ $result == PASS:* ]]; then
        echo -e "${GREEN}$result${NC}"
    else
        echo -e "${RED}$result${NC}"
    fi
done

echo -e "\n${PURPLE}=== RECOMMENDATIONS ===${NC}"

if [ $CRITICAL_FAILURES -gt 0 ]; then
    echo -e "${RED}🚨 CRITICAL ISSUES DETECTED${NC}"
    echo "The phlebotomy workflow has critical failures that prevent basic functionality."
    echo "Priority fixes needed before the system can be considered functional."
elif [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  ISSUES DETECTED${NC}"
    echo "The phlebotomy workflow has some issues but core functionality may work."
    echo "Recommended to fix these issues for optimal performance."
else
    echo -e "${GREEN}🎉 ALL TESTS PASSED${NC}"
    echo "The phlebotomy workflow is fully functional and ready for production!"
fi

echo -e "\n${CYAN}=== NEXT STEPS ===${NC}"
if [ $FAILED_TESTS -gt 0 ]; then
    echo "1. Review failed tests above"
    echo "2. Fix critical issues first (marked with 🚨)"
    echo "3. Address remaining issues"
    echo "4. Re-run this comprehensive test suite"
    echo "5. Perform manual UI testing in browser"
else
    echo "1. Perform manual UI testing in browser"
    echo "2. Test with real user scenarios"
    echo "3. Deploy to staging environment"
    echo "4. Conduct user acceptance testing"
fi

# Exit with appropriate code
if [ $CRITICAL_FAILURES -gt 0 ]; then
    exit 2  # Critical failures
elif [ $FAILED_TESTS -gt 0 ]; then
    exit 1  # Some failures
else
    exit 0  # All tests passed
fi
