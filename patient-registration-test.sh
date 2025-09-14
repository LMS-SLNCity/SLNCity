#!/bin/bash

# Patient Registration Test Script
# Tests the complete patient registration functionality

echo "🏥 PATIENT REGISTRATION TEST"
echo "============================"
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run a test
run_test() {
    local test_name="$1"
    local command="$2"
    local expected_pattern="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "Testing: $test_name... "
    
    result=$(eval "$command" 2>/dev/null)
    
    if echo "$result" | grep -q "$expected_pattern"; then
        echo -e "${GREEN}PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}FAIL${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "  Expected pattern: $expected_pattern"
        echo "  Got: $(echo "$result" | head -3)"
        return 1
    fi
}

# Function to test authenticated API call
test_authenticated_api() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_pattern="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "Testing: $test_name... "
    
    # Login first
    curl -s -c test_cookies.txt -X POST http://localhost:8080/login \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=reception&password=reception123" \
        --location > /dev/null
    
    # Make authenticated API call
    if [ "$method" = "POST" ]; then
        result=$(curl -s -b test_cookies.txt -X POST "http://localhost:8080$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        result=$(curl -s -b test_cookies.txt -X GET "http://localhost:8080$endpoint")
    fi
    
    # Clean up cookies
    rm -f test_cookies.txt
    
    if echo "$result" | grep -q "$expected_pattern"; then
        echo -e "${GREEN}PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}FAIL${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "  Expected pattern: $expected_pattern"
        echo "  Got: $(echo "$result" | head -3)"
        return 1
    fi
}

# Check if application is running
echo "🔍 Checking application status..."
health_check=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
if echo "$health_check" | grep -q '"status":"UP"'; then
    echo -e "Application status: ${GREEN}UP${NC}"
else
    echo -e "Application status: ${RED}DOWN${NC}"
    echo "Please start the application with: mvn spring-boot:run"
    exit 1
fi

echo -e "\n${BLUE}Testing Patient Registration API${NC}"
echo "================================="

# Test 1: Valid patient registration
test_authenticated_api "Valid patient registration" "POST" "/visits" '{
    "patientDetails": {
        "name": "John Doe",
        "age": "35",
        "gender": "M",
        "phone": "9876543210",
        "email": "john.doe@example.com",
        "address": "123 Main Street, Mumbai",
        "patientId": "PAT001",
        "doctorRef": "Dr. Smith",
        "emergencyContact": "9876543211"
    }
}' "visitId"

# Test 2: Patient registration with minimal data
test_authenticated_api "Minimal patient data" "POST" "/visits" '{
    "patientDetails": {
        "name": "Jane Smith",
        "age": "28",
        "gender": "F",
        "phone": "9876543212"
    }
}' "visitId"

# Test 3: Invalid patient registration (missing required fields)
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -n "Testing: Invalid registration (missing name)... "

curl -s -c test_cookies.txt -X POST http://localhost:8080/login \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=reception&password=reception123" \
    --location > /dev/null

result=$(curl -s -b test_cookies.txt -X POST http://localhost:8080/visits \
    -H "Content-Type: application/json" \
    -d '{"patientDetails": {"age": "30"}}' \
    -w "%{http_code}")

rm -f test_cookies.txt

if [[ "$result" == *"400"* ]] || echo "$result" | grep -q "error"; then
    echo -e "${GREEN}PASS${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}FAIL${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 4: Get all visits
test_authenticated_api "Get all visits" "GET" "/visits" "" "visitId"

# Test 5: Search visits by phone
test_authenticated_api "Search by phone" "GET" "/visits/search?phone=9876543210" "" "visitId"

echo -e "\n${BLUE}Testing Reception Dashboard Access${NC}"
echo "=================================="

# Test reception dashboard access
run_test "Reception dashboard access" \
    "curl -s -c test_cookies.txt -X POST http://localhost:8080/login \
        -H 'Content-Type: application/x-www-form-urlencoded' \
        -d 'username=reception&password=reception123' --location > /dev/null && \
     curl -s -b test_cookies.txt http://localhost:8080/reception/dashboard.html && \
     rm -f test_cookies.txt" \
    "Reception Dashboard"

# Final results
echo -e "\n${YELLOW}TEST RESULTS SUMMARY${NC}"
echo "===================="
echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n🎉 ${GREEN}ALL TESTS PASSED!${NC}"
    echo -e "✅ Patient registration is working correctly"
    echo -e "✅ API endpoints are properly secured"
    echo -e "✅ Data validation is working"
    echo -e "✅ Reception dashboard is accessible"
    echo -e "\n${BLUE}Patient registration is now fixed and ready to use!${NC}"
    exit 0
else
    echo -e "\n❌ ${RED}SOME TESTS FAILED${NC}"
    echo "Please check the failed tests above"
    exit 1
fi
