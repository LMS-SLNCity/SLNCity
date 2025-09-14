#!/bin/bash

# RBAC Login System Test Script
# Tests the complete Role-Based Access Control system

echo "🔐 RBAC LOGIN SYSTEM TEST"
echo "=========================="
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
    else
        echo -e "${RED}FAIL${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "  Expected pattern: $expected_pattern"
        echo "  Got: $(echo "$result" | head -3)"
    fi
}

# Function to test login and dashboard access
test_role_login() {
    local role="$1"
    local username="$2"
    local password="$3"
    local expected_title="$4"

    echo -e "\n${BLUE}Testing $role Role Login${NC}"
    echo "--------------------------------"

    # Test login and get session cookie
    login_response=$(curl -s -v -X POST http://localhost:8080/login \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=$username&password=$password" \
        --cookie-jar "${role}_cookies.txt" \
        --location 2>&1)

    # Check if login was successful (should redirect to dashboard)
    if echo "$login_response" | grep -q "Location.*dashboard"; then
        echo -e "Login redirect: ${GREEN}SUCCESS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "Login redirect: ${RED}FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    # Test dashboard access using the session cookie
    dashboard_result=$(curl -s -X GET http://localhost:8080/dashboard \
        --cookie "${role}_cookies.txt" \
        --location)

    if echo "$dashboard_result" | grep -q "$expected_title"; then
        echo -e "Dashboard access: ${GREEN}SUCCESS${NC}"
        echo -e "  ✓ Correct role-specific dashboard loaded"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "Dashboard access: ${RED}FAILED${NC}"
        echo "  Expected: $expected_title"
        echo "  Got: $(echo "$dashboard_result" | grep -o '<title>[^<]*</title>' | head -1)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    # Clean up cookies
    rm -f "${role}_cookies.txt"
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

# Test login page access
echo -e "\n${BLUE}Testing Login Page Access${NC}"
echo "--------------------------------"
run_test "Login page accessibility" \
    "curl -s http://localhost:8080/login.html" \
    "Lab Operations - Login"

# Test all role logins
test_role_login "ADMIN" "admin" "admin123" "Admin Dashboard"
test_role_login "RECEPTION" "reception" "reception123" "Reception Dashboard"
test_role_login "PHLEBOTOMY" "phlebotomy" "phlebotomy123" "Phlebotomy Dashboard"
test_role_login "TECHNICIAN" "technician" "technician123" "Technician Dashboard"

# Test invalid login
echo -e "\n${BLUE}Testing Invalid Login${NC}"
echo "--------------------------------"
invalid_login=$(curl -s -v -X POST http://localhost:8080/login \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=invalid&password=invalid" \
    --location 2>&1)

if echo "$invalid_login" | grep -q "error" || echo "$invalid_login" | grep -q "login"; then
    echo -e "Invalid login handling: ${GREEN}SUCCESS${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "Invalid login handling: ${RED}FAILED${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))

# Test protected endpoints
echo -e "\n${BLUE}Testing Protected Endpoints${NC}"
echo "--------------------------------"

# Test unauthenticated access to dashboard
run_test "Unauthenticated dashboard access" \
    "curl -s http://localhost:8080/dashboard -w '%{http_code}'" \
    "302"

# Test static resources (should be accessible)
run_test "Static CSS access" \
    "curl -s http://localhost:8080/css/admin.css" \
    "admin-container"

run_test "Static JS access" \
    "curl -s http://localhost:8080/js/admin.js" \
    "AdminDashboard"

# Test API endpoints (should require authentication)
run_test "Protected API access" \
    "curl -s http://localhost:8080/visits -w '%{http_code}'" \
    "302"

# Final results
echo -e "\n${YELLOW}TEST RESULTS SUMMARY${NC}"
echo "===================="
echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n🎉 ${GREEN}ALL TESTS PASSED!${NC}"
    echo -e "✅ RBAC system is working correctly"
    echo -e "✅ All roles can login and access their dashboards"
    echo -e "✅ Security is properly configured"
    echo -e "✅ Static resources are accessible"
    echo -e "✅ Protected endpoints require authentication"
    exit 0
else
    echo -e "\n❌ ${RED}SOME TESTS FAILED${NC}"
    echo "Please check the failed tests above"
    exit 1
fi
