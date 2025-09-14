#!/bin/bash

# UI Functionality Test Script
# Tests the Lab Operations Management System UI and API integration

echo "🧪 Lab Operations UI Functionality Test"
echo "========================================"

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to test API endpoint
test_api() {
    local endpoint=$1
    local description=$2
    local expected_status=${3:-200}
    
    echo -n "Testing $description... "
    
    response=$(curl -s -w "%{http_code}" -o /tmp/api_response.json "$BASE_URL$endpoint")
    status_code="${response: -3}"
    
    if [ "$status_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $status_code)"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $status_code, expected $expected_status)"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return 1
    fi
}

# Function to test API with data
test_api_with_data() {
    local endpoint=$1
    local description=$2
    local expected_count=$3
    
    echo -n "Testing $description... "
    
    response=$(curl -s "$BASE_URL$endpoint")
    count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
    
    if [ "$count" -ge "$expected_count" ]; then
        echo -e "${GREEN}✓ PASS${NC} ($count items found, expected >= $expected_count)"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} ($count items found, expected >= $expected_count)"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return 1
    fi
}

echo -e "\n${BLUE}1. Testing Core API Endpoints${NC}"
echo "--------------------------------"

# Test main UI
test_api "/" "Main UI page" 200

# Test API endpoints
test_api "/visits" "Visits API"
test_api "/billing" "Billing API"
test_api "/api/v1/equipment" "Equipment API"
test_api "/api/v1/network-connections" "Network Connections API"
test_api "/actuator/health" "Health Check API"

echo -e "\n${BLUE}2. Testing Data Availability${NC}"
echo "--------------------------------"

# Test data endpoints
test_api_with_data "/visits" "Visits data availability" 2
test_api_with_data "/api/v1/equipment" "Equipment data availability" 2

echo -e "\n${BLUE}3. Testing Static Resources${NC}"
echo "--------------------------------"

# Test static resources
test_api "/css/main.css" "CSS stylesheet" 200
test_api "/js/main.js" "JavaScript application" 200

echo -e "\n${BLUE}4. Testing System Health${NC}"
echo "--------------------------------"

# Test system health details
echo -n "Testing system health status... "
health_response=$(curl -s "$BASE_URL/actuator/health")
health_status=$(echo "$health_response" | jq -r '.status' 2>/dev/null)

if [ "$health_status" = "UP" ]; then
    echo -e "${GREEN}✓ PASS${NC} (System is UP)"
    TESTS_PASSED=$((TESTS_PASSED + 1))
    
    # Check database health
    db_status=$(echo "$health_response" | jq -r '.components.db.status' 2>/dev/null)
    echo -n "Testing database health... "
    if [ "$db_status" = "UP" ]; then
        echo -e "${GREEN}✓ PASS${NC} (Database is UP)"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}✗ FAIL${NC} (Database status: $db_status)"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
else
    echo -e "${RED}✗ FAIL${NC} (System status: $health_status)"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

echo -e "\n${BLUE}5. Testing UI Navigation Structure${NC}"
echo "------------------------------------"

# Test if main UI contains navigation elements
echo -n "Testing navigation structure... "
ui_content=$(curl -s "$BASE_URL/")

if echo "$ui_content" | grep -q "data-page=\"dashboard\"" && \
   echo "$ui_content" | grep -q "data-page=\"visits\"" && \
   echo "$ui_content" | grep -q "data-page=\"equipment\""; then
    echo -e "${GREEN}✓ PASS${NC} (Navigation structure found)"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ FAIL${NC} (Navigation structure missing)"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test if dashboard statistics elements exist
echo -n "Testing dashboard statistics elements... "
if echo "$ui_content" | grep -q "id=\"total-visits\"" && \
   echo "$ui_content" | grep -q "id=\"pending-tests\"" && \
   echo "$ui_content" | grep -q "id=\"active-equipment\""; then
    echo -e "${GREEN}✓ PASS${NC} (Dashboard statistics elements found)"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ FAIL${NC} (Dashboard statistics elements missing)"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

echo -e "\n${BLUE}6. Testing Data Tables Structure${NC}"
echo "-----------------------------------"

# Test visits table structure
echo -n "Testing visits table structure... "
if echo "$ui_content" | grep -q "id=\"visits-table-body\""; then
    echo -e "${GREEN}✓ PASS${NC} (Visits table structure found)"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ FAIL${NC} (Visits table structure missing)"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test equipment grid structure
echo -n "Testing equipment grid structure... "
if echo "$ui_content" | grep -q "id=\"equipment-grid\""; then
    echo -e "${GREEN}✓ PASS${NC} (Equipment grid structure found)"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ FAIL${NC} (Equipment grid structure missing)"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

echo -e "\n${YELLOW}========================================${NC}"
echo -e "${YELLOW}           TEST SUMMARY${NC}"
echo -e "${YELLOW}========================================${NC}"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
PASS_RATE=$(( (TESTS_PASSED * 100) / TOTAL_TESTS ))

echo -e "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo -e "Pass Rate: ${PASS_RATE}%"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! UI is fully functional.${NC}"
    echo -e "${GREEN}✅ The Lab Operations Management System UI is ready for use.${NC}"
    exit 0
else
    echo -e "\n${YELLOW}⚠️  Some tests failed. Please check the issues above.${NC}"
    exit 1
fi
